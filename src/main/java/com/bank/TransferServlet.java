package com.bank;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/transfer")
public class TransferServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String toAccountNumber = req.getParameter("toAccount");
        double amount = Double.parseDouble(req.getParameter("amount"));
        if (amount <= 0) {
            req.setAttribute("message", "Amount must be greater than zero");
            req.getRequestDispatcher("error.jsp").forward(req, resp);
            return;
        }

        Connection con = null;

        try {
            con = DBConnection.getConnection();

            // ---- ACID DEMO: Start Transaction ----
            con.setAutoCommit(false); // turn off auto-commit, we control it manually

            // 1. Get sender's account
            PreparedStatement ps1 = con.prepareStatement(
                "SELECT account_id, account_number, balance FROM accounts WHERE user_id = ?");
            ps1.setInt(1, userId);
            ResultSet rsSender = ps1.executeQuery();

            if (!rsSender.next()) {
                throw new Exception("Sender account not found");
            }

            int senderAccId = rsSender.getInt("account_id");
            String senderAccNum = rsSender.getString("account_number");
            double senderBalance = rsSender.getDouble("balance");

            if (senderAccNum.equals(toAccountNumber)) {
                throw new Exception("Cannot transfer to your own account");
            }

            if (senderBalance < amount) {
                throw new Exception("Insufficient balance");
            }

            // 2. Get receiver's account
            PreparedStatement ps2 = con.prepareStatement(
                "SELECT account_id, balance FROM accounts WHERE account_number = ?");
            ps2.setString(1, toAccountNumber);
            ResultSet rsReceiver = ps2.executeQuery();

            if (!rsReceiver.next()) {
                throw new Exception("Receiver account not found");
            }

            int receiverAccId = rsReceiver.getInt("account_id");

            // 3. Debit sender
            PreparedStatement ps3 = con.prepareStatement(
                "UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
            ps3.setDouble(1, amount);
            ps3.setInt(2, senderAccId);
            ps3.executeUpdate();

            // 4. Credit receiver
            PreparedStatement ps4 = con.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE account_id = ?");
            ps4.setDouble(1, amount);
            ps4.setInt(2, receiverAccId);
            ps4.executeUpdate();

            // 5. Log both transactions
            double senderNewBalance = senderBalance - amount;
            PreparedStatement ps5 = con.prepareStatement(
                "INSERT INTO transactions(account_id, transaction_type, amount, balance_after) VALUES(?,?,?,?)");
            ps5.setInt(1, senderAccId);
            ps5.setString(2, "TRANSFER_OUT");
            ps5.setDouble(3, amount);
            ps5.setDouble(4, senderNewBalance);
            ps5.executeUpdate();

            PreparedStatement ps6 = con.prepareStatement(
                "SELECT balance FROM accounts WHERE account_id = ?");
            ps6.setInt(1, receiverAccId);
            ResultSet rsNewBal = ps6.executeQuery();
            double receiverNewBalance = 0;
            if (rsNewBal.next()) receiverNewBalance = rsNewBal.getDouble("balance");

            PreparedStatement ps7 = con.prepareStatement(
                "INSERT INTO transactions(account_id, transaction_type, amount, balance_after) VALUES(?,?,?,?)");
            ps7.setInt(1, receiverAccId);
            ps7.setString(2, "TRANSFER_IN");
            ps7.setDouble(3, amount);
            ps7.setDouble(4, receiverNewBalance);
            ps7.executeUpdate();

            // ---- COMMIT: All steps succeeded, save permanently ----
            con.commit();
            resp.sendRedirect("dashboard");

        } catch (Exception e) {
            // ---- ROLLBACK: Something failed, undo everything ----
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            req.setAttribute("message", "Error: " + e.getMessage());
            req.getRequestDispatcher("error.jsp").forward(req, resp);
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}