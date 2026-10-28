package com.bank;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/statement")
public class StatementServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        List<Map<String, Object>> transactions = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps1 = con.prepareStatement(
                "SELECT account_id FROM accounts WHERE user_id = ?");
            ps1.setInt(1, userId);
            ResultSet rsAcc = ps1.executeQuery();

            if (rsAcc.next()) {
                int accountId = rsAcc.getInt("account_id");

                PreparedStatement ps2 = con.prepareStatement(
                    "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC");
                ps2.setInt(1, accountId);
                ResultSet rs = ps2.executeQuery();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("type", rs.getString("transaction_type"));
                    row.put("amount", rs.getDouble("amount"));
                    row.put("balanceAfter", rs.getDouble("balance_after"));
                    row.put("date", rs.getTimestamp("transaction_date"));
                    transactions.add(row);
                }
            }

            req.setAttribute("transactions", transactions);
            req.getRequestDispatcher("statement.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            req.getRequestDispatcher("error.jsp").forward(req, resp);
        }
    }
}