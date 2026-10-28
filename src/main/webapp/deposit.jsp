<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
	<title>Deposit</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
    <h2>Deposit Money</h2>
    <form action="deposit" method="post">
        <label>Amount:</label><br>
        <input type="number" step="0.01" name="amount" required><br><br>
        <button type="submit">Deposit</button>
    </form>
    <a href="dashboard">Back to Dashboard</a>
</div>
</body>
</html>