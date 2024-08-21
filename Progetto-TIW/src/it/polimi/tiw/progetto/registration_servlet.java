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
public class registration_servlet extends HttpServlet {
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
            request.setAttribute("errorMessage", "Le password non coincidono. Riprova.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("registration.html");
            dispatcher.forward(request, response);
//            out.println("<html><body>");
//            out.println("Le password non coincidono. Riprova.");
//            out.println("<a href='registration.html'>Clicca qui per tornare alla pagina di registrazione</a>");
//            out.println("</body></html>");
            return;
        }
    }



}