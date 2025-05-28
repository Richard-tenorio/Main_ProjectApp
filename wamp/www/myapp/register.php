<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

error_reporting(E_ALL);
ini_set('display_errors', 0); // Don't display, only log

// Function to send JSON response and log
function respond($statusCode, $status, $message) {
    http_response_code($statusCode);
    $response = ["status" => $status, "message" => $message];
    error_log("Response: " . json_encode($response));
    echo json_encode($response);
    exit();
}

// Handle only POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(405, "error", "Method not allowed. Use POST.");
}

// DB connection
$conn = new mysqli("localhost", "root", "", "userdb");
if ($conn->connect_error) {
    respond(500, "error", "Database connection failed: " . $conn->connect_error);
}

// Required fields
$fields = ['lastname', 'firstname', 'middlename', 'address', 'city', 'username', 'email', 'password', 'birthdate'];
$data = [];

// Validate and sanitize inputs
foreach ($fields as $field) {
    $data[$field] = isset($_POST[$field]) ? trim($_POST[$field]) : '';
    if (empty($data[$field]) && $field !== 'middlename') {
        respond(400, "error", "Missing required field: $field");
    }
}

// Log received data without password
$logData = $data;
unset($logData['password']);
error_log("Received data: " . json_encode($logData));

// Username format check
if (!preg_match('/^[AKak]\d{8}$/', $data['username'])) {
    respond(422, "error", "Username must start with A or K followed by 8 digits.");
}

// Email format validation
if (!filter_var($data['email'], FILTER_VALIDATE_EMAIL)) {
    respond(422, "error", "Invalid email format.");
}

// Birthdate format check
if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $data['birthdate'])) {
    respond(422, "error", "Invalid birthdate format. Use YYYY-MM-DD.");
}

// Future date check
$birthTimestamp = strtotime($data['birthdate']);
if ($birthTimestamp === false || $birthTimestamp > time()) {
    respond(422, "error", "Birthdate cannot be in the future.");
}

// Check if username or email already exists
$stmt = $conn->prepare("SELECT id FROM users WHERE username = ? OR email = ?");
if (!$stmt) {
    respond(500, "error", "Database error: " . $conn->error);
}
$stmt->bind_param("ss", $data['username'], $data['email']);
$stmt->execute();
$stmt->store_result();
if ($stmt->num_rows > 0) {
    $stmt->close();
    respond(409, "error", "Username or email already exists.");
}
$stmt->close();

// Hash the password
$hashedPassword = password_hash($data['password'], PASSWORD_DEFAULT);

// Insert into database
$insert = $conn->prepare("INSERT INTO users (lastname, firstname, middlename, address, city, username, email, password, birthdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
if (!$insert) {
    respond(500, "error", "Insert failed: " . $conn->error);
}
$insert->bind_param(
    "sssssssss",
    $data['lastname'],
    $data['firstname'],
    $data['middlename'],
    $data['address'],
    $data['city'],
    $data['username'],
    $data['email'],
    $hashedPassword,
    $data['birthdate']
);

if ($insert->execute()) {
    respond(201, "success", "Registration successful.");
} else {
    respond(500, "error", "Registration failed. " . $insert->error);
}

$insert->close();
$conn->close();
?>
