<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
	<title>Mini Statement</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
    <h2>Transaction History</h2>

    <table border="1" cellpadding="8">
        <tr>
            <th>Type</th>
            <th>Amount</th>
            <th>Balance After</th>
            <th>Date</th>
        </tr>
        <%
            List<Map<String, Object>> transactions = (List<Map<String, Object>>) request.getAttribute("transactions");
            if (transactions != null) {
                for (Map<String, Object> t : transactions) {
        %>
        <tr>
            <td><%= t.get("type") %></td>
            <td><%= t.get("amount") %></td>
            <td><%= t.get("balanceAfter") %></td>
            <td><%= t.get("date") %></td>
        </tr>
        <%
                }
            }
        %>
    </table>

    <br>
    <a href="dashboard">Back to Dashboard</a>
</div>
</body>
</html>