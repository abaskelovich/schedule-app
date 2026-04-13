# Test PUT
$body = @{
    title = "Updated Meeting"
    description = "Updated description"
    time = "15:00"
    date = "2026-04-15"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:3000/api/schedules?id=2" -Method PUT -Body $body -ContentType "application/json"
$response | ConvertTo-Json
