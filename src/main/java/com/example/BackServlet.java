package com.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



@WebServlet("/BackServlet")
public class BackServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Recupera la cronologia dalla sessione (salvata nell'attributo pageHistory)
        HttpSession session = request.getSession();
        LinkedList<String> history = (LinkedList<String>) session.getAttribute("pageHistory");
        
        if (history == null) {
            System.out.println ("LISTA NULLA !!!");
        }
        
      
        
        if (history == null || history.size() <= 1) {
            // Se non ci sono pagine precedenti, reindirizza alla pagina home
            response.sendRedirect("http://localhost:8080/tiw_project/HomeServlet");
            return;
        }

        // Rimuovi la pagina corrente dalla cronologia
        history.removeLast();

        // Ottieni l'URL della pagina precedente
        String previousPage = history.getLast();

        // Aggiorna la cronologia nella sessione
        session.setAttribute("pageHistory", history);

        // Reindirizza alla pagina precedente
        response.sendRedirect(previousPage);
               
    
    }
}


