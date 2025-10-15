package com.example.airmonitorble;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONObject;
import java.io.*;
import java.net.*;

public class SensorDataActivity extends AppCompatActivity {
    TextView gasDataText;
    Handler handler = new Handler();
    String esp32Ip = "192.168.1.102"; // TODO: replace dynamically later

    // Launcher for runtime notification permission (Android 13+)
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data);

        gasDataText = findViewById(R.id.gasDataText);
        createNotificationChannel();

        // ✅ Register launcher to request notification permission
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Notifications disabled.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // ✅ Ask for permission if Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        handler.post(fetchRunnable);
    }

    Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {
            fetchSensorData();
            handler.postDelayed(this, 5000);
        }
    };

    private void fetchSensorData() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + esp32Ip + "/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                double aqi = json.getDouble("aqi");
                String data = "LPG: " + json.getDouble("lpg") +
                        "\nCO2: " + json.getDouble("co2") +
                        "\nNH3: " + json.getDouble("nh3") +
                        "\nTemp: " + json.getDouble("temperature") + "°C" +
                        "\nHumidity: " + json.getDouble("humidity") + "%" +
                        "\nAQI: " + aqi;

                runOnUiThread(() -> gasDataText.setText(data));

                if (aqi > 150.0) sendAlert(aqi);

            } catch (Exception e) {
                runOnUiThread(() -> gasDataText.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void sendAlert(double aqi) {
        // ✅ Check permission before notifying
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "air_alert")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ Dangerous Air Quality!")
                .setContentText("AQI reached " + aqi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "air_alert", "Air Alerts", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
