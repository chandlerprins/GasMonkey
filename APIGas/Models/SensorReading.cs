namespace APIGas.Models
{
    public class SensorReading
    {
        public string Timestamp { get; set; }  // stored as string in Firebase
        public long TimestampLong { get; set; } // numeric (for sorting/graph)
        public string ReadableTimestamp { get; set; } 
        public double Temp_c { get; set; }
        public double Humidity { get; set; }
        public double Lpg_ppm { get; set; }
        public double Co2_ppm { get; set; }
        public double Nh3_ppm { get; set; }
        public double Aqi { get; set; }
    }
}
