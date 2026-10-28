package com.bank;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/deposit")
public class DepositServlet extends HttpServlet {
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

            // 1. Update balance (CRUD - Update)
            PreparedStatement ps = con.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE user_id = ?");
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();

            // 2. Get account_id and new balance to log transaction
            PreparedStatement ps2 = con.prepareStatement(
                "SELECT account_id, balance FROM accounts WHERE user_id = ?");
            ps2.setInt(1, userId);
            ResultSet rs = ps2.executeQuery();

            if (rs.next()) {
                int accountId = rs.getInt("account_id");
                double newBalance = rs.getDouble("balance");

                PreparedStatement ps3 = con.prepareStatement(
                    "INSERT INTO transactions(account_id, transaction_type, amount, balance_after) VALUES(?,?,?,?)");
                ps3.setInt(1, accountId);
                ps3.setString(2, "DEPOSIT");
                ps3.setDouble(3, amount);
                ps3.setDouble(4, newBalance);
                ps3.executeUpdate();
            }

            resp.sendRedirect("dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}