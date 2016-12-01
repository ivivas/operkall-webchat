/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author mario
 */
public class MensajeController extends HttpServlet {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(MensajeController.class);

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
        response.setContentType("text/html;charset=UTF-8");
//        HttpSession sesion = request.getSession();
        SocketUsuarioWeb socketUsuarioWeb = null;
        PrintWriter out = response.getWriter();
        boolean flag = true;
        try {
            if (request.getSession().getAttribute("socketUsuarioWeb") != null) {
                //logger.debug("socketUsuarioWeb not null");
                socketUsuarioWeb = (SocketUsuarioWeb) request.getSession().getAttribute("socketUsuarioWeb");
            } else if (request.getParameter("nuevaConexion") != null) {//Recordar cambiar el tipo de solicitud, ya que por sesion a cada rato enviaria una nueva conexion y creara una diferente....
                logger.debug("Nueva conexion: " + request.getParameter("nombreClienteWeb"));
                socketUsuarioWeb = new SocketUsuarioWeb(request.getSession().getId());
                socketUsuarioWeb.enviarMensajes("conexionNuevaClienteWeb_#_" + request.getParameter("nombreClienteWeb"));
            }
            else {
                //logger.debug("Else...");
                //Aqui deberia cerrar la sesion
                request.getSession().invalidate();
                flag = false;
//                if (suw != null) {
//                    suw.cerrarSession();
//                }
//                response.sendRedirect("index.jsp");
//                out.println();
//                out.println("<script type=\"text/javascript\">");
//                out.println("alert('Su session ha Expirado...');");
//                out.println("</script>");
//                response.sendRedirect("inicio");
                //request.getRequestDispatcher("inicio").forward(request, response);
            }

//            if (suw != null && suw.getMensaje() != null && suw.getMensaje().getSb() != null && suw.getMensaje().getSb().toString().contains("no hay ejecutivos disponibles, intentelo mas tarde")) {
//                flag = false;
//                out.println();
//                out.println("<script type=\"text/javascript\">");
//                out.println("alert('No hay ejecutivos Disponibles\nIntente mas tarde...');");
//                out.println("</script>");
//                response.sendRedirect("inicio");
//                //request.getRequestDispatcher("inicio").forward(request, response);
//            }
            if (flag) {
                if (request.getParameter("areaMensajeChatWeb") != null && !request.getParameter("areaMensajeChatWeb").trim().equalsIgnoreCase("")) {
                    String textoNuevoMensaje = request.getParameter("nombreClienteWeb") + ": " + request.getParameter("areaMensajeChatWeb").trim();
                    socketUsuarioWeb.enviarMensajes("textoclienteweb_#_" + textoNuevoMensaje);
                    socketUsuarioWeb.getMensaje().escribir(textoNuevoMensaje);
                }
                request.getSession().setAttribute("socketUsuarioWeb", socketUsuarioWeb);
                out.println(socketUsuarioWeb.getMensaje().getSb().toString());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            out.close();
        }
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
