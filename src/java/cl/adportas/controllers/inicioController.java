/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.controllers;


import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;



/**
 *
 * @author mario
 */
public class inicioController extends HttpServlet {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(inicioController.class);
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        HttpSession sesion = request.getSession();
        //nombreClienteWeb
        //request.getParameter("nuevaConexion")
        String nombreClienteWeb = request.getParameter("nombreClienteWeb");
        SocketUsuarioWeb socketUsuarioWeb;
        socketUsuarioWeb = request.getSession().getAttribute("socketUsuarioWeb") != null ? (SocketUsuarioWeb) request.getSession().getAttribute("socketUsuarioWeb") : null;
        if (socketUsuarioWeb != null) {
            logger.debug("Cerrando sesion usuario web: " + nombreClienteWeb);
            socketUsuarioWeb.enviarMensajes("finConexionClienteWeb_#_" + nombreClienteWeb);
            socketUsuarioWeb.cerrarSession();
        }
        request.getSession().invalidate();
        request.getRequestDispatcher("index.jsp").forward(request, response);

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
