/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import static java.nio.charset.StandardCharsets.UTF_8;
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
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        //HttpSession sesion = request.getSession();
        SocketUsuarioWeb socketUsuarioWeb = null;
        PrintWriter out = response.getWriter();
        boolean flag = true;
        
        try {
            if (request.getSession().getAttribute("socketUsuarioWeb") != null) {
                socketUsuarioWeb = (SocketUsuarioWeb) request.getSession().getAttribute("socketUsuarioWeb");
            } 
            else if (request.getParameter("nuevaConexion") != null) {//Recordar cambiar el tipo de solicitud, ya que por sesion a cada rato enviaria una nueva conexion y creara una diferente....
                logger.debug("Nueva conexion: " + request.getParameter("nombreClienteWeb"));
                
                // Se hace codificacion a UTF-8 para poder enviar acentos y caracteres especiales a través del socket y que se guarden en BD de forma correcta
                byte[] bytesNombreClienteWeb = (request.getParameter("nombreClienteWeb") == null ? "".getBytes(UTF_8) : request.getParameter("nombreClienteWeb").trim().getBytes(UTF_8));
                String nombreClienteWeb = new String(bytesNombreClienteWeb);
                byte[] bytesTelefonoClienteWeb = (request.getParameter("telefonoClienteWeb") == null ? "".getBytes(UTF_8) : request.getParameter("telefonoClienteWeb").trim().getBytes(UTF_8));
                String telefonoClienteWeb = new String(bytesTelefonoClienteWeb);
                byte[] bytesCorreoClienteWeb = (request.getParameter("correoClienteWeb") == null ? "".getBytes(UTF_8) : request.getParameter("correoClienteWeb").trim().getBytes(UTF_8));
                String correoClienteWeb = new String(bytesCorreoClienteWeb);
                byte[] bytesCiudadClienteWeb = (request.getParameter("ciudadClienteWeb") == null ? "".getBytes(UTF_8) : request.getParameter("ciudadClienteWeb").trim().getBytes(UTF_8));
                String ciudadClienteWeb = new String(bytesCiudadClienteWeb);
                byte[] bytesAsuntoClienteWeb = (request.getParameter("asuntoClienteWeb") == null ? "".getBytes(UTF_8) : request.getParameter("asuntoClienteWeb").trim().getBytes(UTF_8));
                String asuntoClienteWeb = new String(bytesAsuntoClienteWeb);
                
                socketUsuarioWeb = new SocketUsuarioWeb(request.getSession().getId());
                socketUsuarioWeb.enviarMensajes("conexionNuevaClienteWeb_#_" + nombreClienteWeb + "_#_" + telefonoClienteWeb + "_#_" + correoClienteWeb + "_#_" + ciudadClienteWeb + "_#_" + asuntoClienteWeb);
            }
            else {
                request.getSession().invalidate();
                flag = false;
            }
            if (flag) {
                if (request.getParameter("areaMensajeChatWeb") != null && !request.getParameter("areaMensajeChatWeb").trim().equalsIgnoreCase("")) {
                    // Se hace codificacion a UTF-8 para poder enviar acentos y caracteres especiales a través del socket
                    byte[] bytesMensaje = request.getParameter("areaMensajeChatWeb").getBytes(UTF_8);
                    String mensaje = new String(bytesMensaje);
                    byte[] bytesNombreCliente = request.getParameter("nombreClienteWeb").getBytes(UTF_8);
                    String nombreCliente = new String(bytesNombreCliente);
                    String textoNuevoMensaje = nombreCliente + ": " + mensaje;
                    socketUsuarioWeb.enviarMensajes("textoclienteweb_#_" + textoNuevoMensaje);
                    
                    // El mensaje que se despliega en el area de chat del html no requiere la codificacion anterior, por lo tanto no se utilizas las variables anteriores
                    socketUsuarioWeb.getMensaje().escribir(request.getParameter("nombreClienteWeb") + ": " + request.getParameter("areaMensajeChatWeb"));
                }
                request.getSession().setAttribute("socketUsuarioWeb", socketUsuarioWeb);
                out.println(socketUsuarioWeb.getMensaje().getSb());
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
