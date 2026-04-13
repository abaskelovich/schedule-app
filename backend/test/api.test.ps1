Write-Host "Running API tests..." -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:3000"
$passed = 0
$failed = 0
$createdId = $null

# Test 1: Create schedule
Write-Host "Test 1: Create schedule" -ForegroundColor Yellow
try {
    $body = @{
        title = "Test Meeting"
        description = "Test description"
        time = "14:00"
        date = "2026-04-14"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/api/schedules" -Method POST -Body $body -ContentType "application/json"
    Write-Host "  Status: 201" -ForegroundColor Green
    Write-Host "  Created ID: $($response.id)"
    $createdId = $response.id
    Write-Host "  PASSED" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
    $failed++
}
Write-Host ""

# Test 2: Get all schedules
Write-Host "Test 2: Get all schedules" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/schedules" -Method GET
    Write-Host "  Status: 200" -ForegroundColor Green
    Write-Host "  Found $($response.Count) schedules"
    Write-Host "  PASSED" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
    $failed++
}
Write-Host ""

# Test 3: Get single schedule
if ($createdId) {
    Write-Host "Test 3: Get single schedule" -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/schedules?id=$createdId" -Method GET
        Write-Host "  Status: 200" -ForegroundColor Green
        if ($response.title -eq "Test Meeting") {
            Write-Host "  PASSED" -ForegroundColor Green
            $passed++
        } else {
            Write-Host "  FAILED - Wrong title" -ForegroundColor Red
            $failed++
        }
    } catch {
        Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
    Write-Host ""
}

# Test 4: Update schedule
if ($createdId) {
    Write-Host "Test 4: Update schedule" -ForegroundColor Yellow
    try {
        $body = @{
            title = "Updated Meeting"
            description = "Updated description"
            time = "15:00"
            date = "2026-04-15"
        } | ConvertTo-Json

        $response = Invoke-RestMethod -Uri "$baseUrl/api/schedules?id=$createdId" -Method PUT -Body $body -ContentType "application/json"
        Write-Host "  Status: 200" -ForegroundColor Green
        if ($response.title -eq "Updated Meeting") {
            Write-Host "  PASSED" -ForegroundColor Green
            $passed++
        } else {
            Write-Host "  FAILED - Title not updated" -ForegroundColor Red
            $failed++
        }
    } catch {
        Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
    Write-Host ""
}

# Test 5: Delete schedule
if ($createdId) {
    Write-Host "Test 5: Delete schedule" -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/schedules?id=$createdId" -Method DELETE
        Write-Host "  Status: 200" -ForegroundColor Green
        Write-Host "  PASSED" -ForegroundColor Green
        $passed++
    } catch {
        Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
    Write-Host ""
}

# Test 6: Verify deletion
if ($createdId) {
    Write-Host "Test 6: Verify deletion" -ForegroundColor Yellow
    try {
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/api/schedules?id=$createdId" -Method GET
            Write-Host "  Status: Found (should be 404)" -ForegroundColor Red
            Write-Host "  FAILED - Schedule still exists" -ForegroundColor Red
            $failed++
        } catch {
            if ($_.Exception.Response.StatusCode -eq 404) {
                Write-Host "  Status: 404" -ForegroundColor Green
                Write-Host "  PASSED" -ForegroundColor Green
                $passed++
            } else {
                throw $_
            }
        }
    } catch {
        Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
    Write-Host ""
}

Write-Host "=================================" -ForegroundColor Cyan
Write-Host "Results: $passed passed, $failed failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
Write-Host "=================================" -ForegroundColor Cyan
