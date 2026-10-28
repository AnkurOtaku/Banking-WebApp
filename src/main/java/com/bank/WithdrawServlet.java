package com.bank;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/withdraw")
public class WithdrawServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        double amount = Double.parseDouble(req.getParameter("amount"));
        if (amount <= 0) {
            req.setAttribute("message", "Amount must be greater than zero");
            req.getRequestDispatcher("error.jsp").forward(req, resp);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            // 1. Check current balance first (Consistency check)
            PreparedStatement ps1 = con.prepareStatement(
                "SELECT account_id, balance FROM accounts WHERE user_id = ?");
            ps1.setInt(1, userId);
            ResultSet rs = ps1.executeQuery();

            if (rs.next()) {
                int accountId = rs.getInt("account_id");
                double currentBalance = rs.getDouble("balance");

                if (currentBalance < amount) {
                	req.setAttribute("message", "Insufficient balance!");
                	req.getRequestDispatcher("error.jsp").forward(req, resp);
                    return;
                }

                // 2. Deduct balance (CRUD - Update)
                PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
                ps2.setDouble(1, amount);
                ps2.setInt(2, accountId);
                ps2.executeUpdate();

                double newBalance = currentBalance - amount;

                // 3. Log transaction
                PreparedStatement ps3 = con.prepareStatement(
                    "INSERT INTO transactions(account_id, transaction_type, amount, balance_after) VALUES(?,?,?,?)");
                ps3.setInt(1, accountId);
                ps3.setString(2, "WITHDRAW");
                ps3.setDouble(3, amount);
                ps3.setDouble(4, newBalance);
                ps3.executeUpdate();
            }

            resp.sendRedirect("dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", e.getMessage());
            req.getRequestDispatcher("error.jsp").forward(req, resp);
        }
    }
}