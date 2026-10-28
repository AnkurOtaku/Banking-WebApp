<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
	<title>Transfer</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
    <h2>Transfer Money</h2>
    <form action="transfer" method="post">
        <label>Receiver's Account Number:</label><br>
        <input type="text" name="toAccount" required><br><br>

        <label>Amount:</label><br>
        <input type="number" step="0.01" name="amount" required><br><br>

        <button type="submit">Transfer</button>
    </form>
    <a href="dashboard">Back to Dashboard</a>
</div>
</body>
</html>