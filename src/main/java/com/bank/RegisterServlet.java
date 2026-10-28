package com.bank;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.Random;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try (Connection con = DBConnection.getConnection()) {
            // 1. Create user (CRUD - Create)
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(full_name, username, email, password) VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) userId = rs.getInt(1);

            // 2. Auto-create bank account with 0 balance
            Random rand = new Random();
            long num = 1000000000L + (long)(rand.nextDouble() * 9000000000L);
            String accNumber = String.valueOf(num);

            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO accounts(user_id, account_number, balance) VALUES(?,?,0)");
            ps2.setInt(1, userId);
            ps2.setString(2, accNumber);
            ps2.executeUpdate();

            resp.sendRedirect("login.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("message", "Registration failed: " + e.getMessage());
            req.getRequestDispatcher("error.jsp").forward(req, resp);
        }
    }
}