package com.example.servlets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.WebServlet;


import java.io.IOException;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Invalidiamo la sessione, in modo da eliminare gli attributi salvati nella sessione ed effettuare il logout
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }

        // Reindirizziamo l'utente alla pagina contenente la form di login
        response.sendRedirect("login.html");
    }
}

