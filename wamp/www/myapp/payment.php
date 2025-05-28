<?php
// Clean output and set plain text response
if (ob_get_length()) ob_clean(); // Safely clean any existing buffer
header('Content-Type: text/plain');
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Database connection settings
$host = "localhost";
$user = "root"; // WAMP/XAMPP default
$password = "";
$dbname = "userdb";

// Connect to database
$conn = new mysqli($host, $user, $password, $dbname);
if ($conn->connect_error) {
    echo "error: " . $conn->connect_error;
    exit;
}

// Get POST values
$plan = $_POST['plan'] ?? '';
$name = $_POST['name'] ?? '';
$reference_no = $_POST['reference_no'] ?? '';
$amount = isset($_POST['amount']) ? floatval($_POST['amount']) : 0.0;

// Validate required fields
if (empty($plan) || empty($name) || empty($reference_no) || $amount <= 0) {
    echo "error: invalid input";
    exit;
}

// Prepare SQL insert
$sql = "INSERT INTO payments (plan, name, reference_no, amount) VALUES (?, ?, ?, ?)";
$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo "error: " . $conn->error;
    exit;
}

$stmt->bind_param("sssd", $plan, $name, $reference_no, $amount);

// Execute and return result
if ($stmt->execute()) {
    echo $stmt->insert_id; // Return the inserted ID for Android
} else {
    echo "error: " . $stmt->error;
}

$stmt->close();
$conn->close();
?>
