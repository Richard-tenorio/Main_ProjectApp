<?php
header("Content-Type: application/json");

// Database connection
$conn = new mysqli("localhost", "root", "", "userdb");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Database connection failed."]);
    exit();
}

// Get and sanitize input
$username = isset($_POST['username']) ? trim($_POST['username']) : '';
$password = isset($_POST['password']) ? trim($_POST['password']) : '';

if (empty($username) || empty($password)) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Username and password are required."]);
    exit();
}

// Prepare query to fetch hashed password
$stmt = $conn->prepare("SELECT password FROM users WHERE username = ?");
if (!$stmt) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Database error during prepare."]);
     exit();
}

$stmt->bind_param("s", $username);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows === 1) {
    $stmt->bind_result($storedHashedPassword);
    $stmt->fetch();

    // Verify password
    if (password_verify($password, $storedHashedPassword)) {
        echo json_encode(["status" => "success", "message" => "Login successful"]);
    } else {
        http_response_code(401);
        echo json_encode(["status" => "error", "message" => "Invalid password"]);
    }
} else {
    http_response_code(404);
    echo json_encode(["status" => "error", "message" => "User not found"]);
}

$stmt->close();
$conn->close();
?>
