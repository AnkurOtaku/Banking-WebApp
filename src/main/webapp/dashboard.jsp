<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
    <h2>Welcome, ${sessionScope.fullName}</h2>

    <div class="balance-box">
        <p><b>Account Number:</b> ${account}</p>
        <p><b>Balance:</b> ₹ ${balance}</p>
    </div>

    <div class="nav-links">
        <a href="statement">Statement</a>
        <a href="deposit.jsp">Deposit</a>
        <a href="withdraw.jsp">Withdraw</a>
        <a href="transfer.jsp">Transfer</a>
        <a href="logout">Logout</a>
    </div>

    <p id="sessionTimer" style="color:gray; font-size:14px;"></p>

    <script>
        var timeLeft = 5 * 60;

        function updateTimer() {
            var minutes = Math.floor(timeLeft / 60);
            var seconds = timeLeft % 60;
            if (seconds < 10) seconds = "0" + seconds;
            document.getElementById("sessionTimer").innerText =
                "Session expires in: " + minutes + ":" + seconds;

            if (timeLeft <= 0) {
                alert("Session expired. Redirecting to login.");
                window.location.href = "logout";
            }
            timeLeft--;
        }

        setInterval(updateTimer, 1000);
    </script>
</div>
</body>
</html>