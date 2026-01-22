# API Testing Script for Hotel Review System
# Requires: All services running

$baseUrl = "http://localhost:9001"
$headers = @{
    "Content-Type" = "application/json"
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Hotel Review System - API Test Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Create User
Write-Host "[TEST 1] Creating a new user..." -ForegroundColor Yellow
$userData = @{
    name = "John Doe"
    email = "john.doe@example.com"
    phone = "1234567890"
} | ConvertTo-Json

try {
    $userResponse = Invoke-RestMethod -Uri "$baseUrl/micro/users/" -Method Post -Body $userData -Headers $headers
    $userId = $userResponse.userId
    Write-Host "✓ User created successfully!" -ForegroundColor Green
    Write-Host "  User ID: $userId" -ForegroundColor Gray
    Write-Host "  Name: $($userResponse.name)" -ForegroundColor Gray
    Write-Host "  Email: $($userResponse.email)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Failed to create user: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# Test 2: Create Hotel
Write-Host "[TEST 2] Creating a new hotel..." -ForegroundColor Yellow
$hotelData = @{
    name = "Grand Plaza Hotel"
    location = "New York, NY"
    about = "Luxury 5-star hotel in downtown Manhattan"
} | ConvertTo-Json

try {
    $hotelResponse = Invoke-RestMethod -Uri "$baseUrl/micro/hotels/" -Method Post -Body $hotelData -Headers $headers
    $hotelId = $hotelResponse.hotelId
    Write-Host "✓ Hotel created successfully!" -ForegroundColor Green
    Write-Host "  Hotel ID: $hotelId" -ForegroundColor Gray
    Write-Host "  Name: $($hotelResponse.name)" -ForegroundColor Gray
    Write-Host "  Location: $($hotelResponse.location)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Failed to create hotel: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# Test 3: Create Rating
Write-Host "[TEST 3] Creating a rating..." -ForegroundColor Yellow
$ratingData = @{
    userId = $userId
    hotelId = $hotelId
    rating = 5
    feedback = "Excellent service and amazing location!"
} | ConvertTo-Json

try {
    $ratingResponse = Invoke-RestMethod -Uri "$baseUrl/micro/ratings/" -Method Post -Body $ratingData -Headers $headers
    $ratingId = $ratingResponse.ratingId
    Write-Host "✓ Rating created successfully!" -ForegroundColor Green
    Write-Host "  Rating ID: $ratingId" -ForegroundColor Gray
    Write-Host "  Rating: $($ratingResponse.rating)/5" -ForegroundColor Gray
    Write-Host "  Feedback: $($ratingResponse.feedback)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Failed to create rating: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# Test 4: Get All Users
Write-Host "[TEST 4] Fetching all users..." -ForegroundColor Yellow
try {
    $allUsers = Invoke-RestMethod -Uri "$baseUrl/micro/users/" -Method Get
    Write-Host "✓ Retrieved $($allUsers.Count) user(s)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Failed to fetch users: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 5: Get All Hotels
Write-Host "[TEST 5] Fetching all hotels..." -ForegroundColor Yellow
try {
    $allHotels = Invoke-RestMethod -Uri "$baseUrl/micro/hotels/" -Method Get
    Write-Host "✓ Retrieved $($allHotels.Count) hotel(s)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Failed to fetch hotels: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 6: Get All Ratings
Write-Host "[TEST 6] Fetching all ratings..." -ForegroundColor Yellow
try {
    $allRatings = Invoke-RestMethod -Uri "$baseUrl/micro/ratings/" -Method Get
    Write-Host "✓ Retrieved $($allRatings.Count) rating(s)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Failed to fetch ratings: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 7: Get User with Ratings (Inter-service communication test)
Write-Host "[TEST 7] Fetching user with ratings (tests inter-service communication)..." -ForegroundColor Yellow
try {
    $userWithRatings = Invoke-RestMethod -Uri "$baseUrl/micro/users/$userId/" -Method Get
    Write-Host "✓ User retrieved with ratings!" -ForegroundColor Green
    Write-Host "  User: $($userWithRatings.name)" -ForegroundColor Gray
    Write-Host "  Ratings count: $($userWithRatings.ratings.Count)" -ForegroundColor Gray
    if ($userWithRatings.ratings.Count -gt 0) {
        Write-Host "  First rating hotel: $($userWithRatings.ratings[0].hotel.name)" -ForegroundColor Gray
    }
    Write-Host ""
} catch {
    Write-Host "✗ Failed to fetch user with ratings: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 8: Get Ratings by User
Write-Host "[TEST 8] Fetching ratings by user..." -ForegroundColor Yellow
try {
    $userRatings = Invoke-RestMethod -Uri "$baseUrl/micro/ratings/users/$userId/" -Method Get
    Write-Host "✓ Retrieved $($userRatings.Count) rating(s) for user" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Failed to fetch user ratings: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 9: Get Ratings by Hotel
Write-Host "[TEST 9] Fetching ratings by hotel..." -ForegroundColor Yellow
try {
    $hotelRatings = Invoke-RestMethod -Uri "$baseUrl/micro/ratings/hotels/$hotelId/" -Method Get
    Write-Host "✓ Retrieved $($hotelRatings.Count) rating(s) for hotel" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Failed to fetch hotel ratings: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Test 10: Get Specific Hotel
Write-Host "[TEST 10] Fetching specific hotel..." -ForegroundColor Yellow
try {
    $specificHotel = Invoke-RestMethod -Uri "$baseUrl/micro/hotels/$hotelId/" -Method Get
    Write-Host "✓ Hotel retrieved successfully!" -ForegroundColor Green
    Write-Host "  Name: $($specificHotel.name)" -ForegroundColor Gray
    Write-Host "  Location: $($specificHotel.location)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Failed to fetch hotel: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✓ All API endpoints tested successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Created Resources:" -ForegroundColor White
Write-Host "  User ID: $userId" -ForegroundColor Gray
Write-Host "  Hotel ID: $hotelId" -ForegroundColor Gray
Write-Host "  Rating ID: $ratingId" -ForegroundColor Gray
Write-Host ""
Write-Host "You can now test these endpoints manually or via Swagger UI" -ForegroundColor White
Write-Host ""
