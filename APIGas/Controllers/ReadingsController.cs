using APIGas.Models;
using Microsoft.AspNetCore.Mvc;
using System.Text;
using System.Text.Json;

namespace APIGas.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReadingsController : ControllerBase
    {
        private readonly HttpClient _httpClient;
        private readonly string _firebaseUrl =
            "https://gasmonkey-4d52f-default-rtdb.firebaseio.com/devices/DEVICE001/readings"; 

        public ReadingsController(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        // POST: /api/readings -> store new reading
        [HttpPost]
        public async Task<IActionResult> PostReading([FromBody] SensorReading reading)
        {
            if (reading == null)
                return BadRequest("Invalid sensor reading.");

            // Generate numeric + readable timestamps
            var timestampLong = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            reading.Timestamp = timestampLong.ToString();
            reading.TimestampLong = timestampLong; //  store numeric timestamp in Firebase
            reading.ReadableTimestamp = DateTimeOffset.UtcNow
                .ToOffset(TimeSpan.FromHours(2))
                .ToString("yyyy-MM-dd HH:mm:ss");

            var json = JsonSerializer.Serialize(reading);
            var content = new StringContent(json, Encoding.UTF8, "application/json");

         
            var response = await _httpClient.PostAsync($"{_firebaseUrl}.json", content);

            if (response.IsSuccessStatusCode)
                return Ok(new { message = "Reading stored successfully", reading });

            return StatusCode((int)response.StatusCode, "Failed to store reading in Firebase.");
        }

        // GET: /api/readings/all -> get all readings
        [HttpGet("all")]
        public async Task<IActionResult> GetAll()
        {
            var firebaseResponse = await _httpClient.GetFromJsonAsync<Dictionary<string, SensorReading>>($"{_firebaseUrl}.json");

            if (firebaseResponse == null || firebaseResponse.Count == 0)
                return NotFound("No readings found.");

            // Just order by TimestampLong directly
            var readings = firebaseResponse.Values
                .OrderByDescending(r => r.TimestampLong)
                .ToList();

            return Ok(readings);
        }

        [HttpGet("latest")]
        public async Task<IActionResult> GetLatest()
        {
            try
            {
                // Fetch all readings from Firebase
                var firebaseResponse = await _httpClient
                    .GetFromJsonAsync<Dictionary<string, SensorReading>>(
                        "https://gasmonkey-4d52f-default-rtdb.firebaseio.com/devices/DEVICE001/readings.json");

                if (firebaseResponse == null || firebaseResponse.Count == 0)
                    return NotFound("No readings found.");

                // Parse timestamp string and sort descending by datetime
                var latest = firebaseResponse.Values
                    .Select(r =>
                    {
                        if (DateTime.TryParse(r.Timestamp, out var dt))
                        {
                            r.TimestampLong = new DateTimeOffset(dt).ToUnixTimeMilliseconds();
                        }
                        return r;
                    })
                    .OrderByDescending(r => r.TimestampLong)
                    .FirstOrDefault();

                if (latest == null)
                    return NotFound("No valid timestamps found.");

                return Ok(latest);
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"Error retrieving latest reading: {ex.Message}");
            }
        }

    }
}
