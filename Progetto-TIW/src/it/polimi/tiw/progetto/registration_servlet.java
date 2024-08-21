import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/RegistrationServlet")
public class RegistrationServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // getting the parameters written by the user
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("password_conf");

        // non si capisce cosa sia ???
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();


        // if confirmation password is different from the password
        if(!password.equals(confirmPassword)){
          String errorMessage = "Le due password non coincidono. Riprova";
            // Reindirizza di nuovo alla pagina HTML con il messaggio di errore nella query string
            response.sendRedirect("/registration.html?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
        } else {
            // Se non ci sono errori, procediamo in home page
            response.sendRedirect("/home_page.html");
        }
        }
    }



}