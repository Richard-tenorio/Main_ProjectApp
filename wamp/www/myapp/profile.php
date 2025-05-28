<?php
header('Content-Type: application/json');
$host = 'localhost';
$db = 'userdb';
$user = 'root';
$pass = '';
$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    echo json_encode(['error' => 'Database connection failed']);
    exit();
}

if (!isset($_GET['username']) || empty($_GET['username'])) {
    echo json_encode(['error' => 'Username not provided']);
    exit();
}

$username = $conn->real_escape_string($_GET['username']);
$sql = "SELECT lastname, firstname, middlename, address, city, username, email, birthdate FROM users WHERE username = '$username'";
$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
    echo json_encode($result->fetch_assoc());
} else {
    echo json_encode(['error' => 'User not found']);
}

$conn->close();
?>
