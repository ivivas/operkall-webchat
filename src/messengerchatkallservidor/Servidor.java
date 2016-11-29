/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messengerchatkallservidor;

import cl.adportas.bd.Conexion;
import cl.adportas.controller.Contactos;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author mario
 */
public final class Servidor {

    ServerSocket servidor = null;
    private static final int PORT = 9000;
    public static HashMap mapContactos = new HashMap();
    public static HashMap mapMensajes = new HashMap();
    private static final Logger logger = LogManager.getLogger(Servidor.class);

    public Servidor() {
        Thread inicio = new Thread(() -> {
            try {
                servidor = new ServerSocket(PORT);
                logger.info("Iniciando " + servidor.toString());
                while (true) {
                    logger.info("Esperando peticiones de conexion al socket ");
                    Socket socketAux = servidor.accept();
                    logger.info("Peticion de cliente recibida");
                    String direccion_ip = socketAux.getInetAddress().getHostAddress() != null ? socketAux.getInetAddress().getHostAddress().trim() : "";
                    String puertoRemoto = socketAux.getPort() + "";
                    String id_final_usuario = direccion_ip + "_" + puertoRemoto;
                    
                    logger.info("verificando si el cliente existe previamente... " + id_final_usuario);
                    if (!mapContactos.containsKey(id_final_usuario)) {
                        Contactos contactos = new Contactos(socketAux, direccion_ip, id_final_usuario, puertoRemoto);
                        mapContactos.put(id_final_usuario, contactos);
                        insertarSesionUsuario(direccion_ip, puertoRemoto);
                        logger.info("Nuevo cliente agregado: " + id_final_usuario);
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.toString());
            }
        });
        inicio.start();
    }

    public void buscarSocketInactivosPorSessionesBD() {
        Conexion con = new Conexion();
        try {
            con.conectar();
            con.contruirSQL("SELECT * FROM sesiones_chat WHERE tipo_sesion = 3;");
            con.ejecutarSQLBusqueda();
            while(con.getRs().next()){
                String id_contraparte_final = con.getRs().getString("direccion_ip") + "_" + con.getRs().getString("puerto");
//                if(mapContactos){}
            }

        } catch (Exception ex) {
            logger.error(ex.toString());
        } finally {
            con.cerrarConexiones();
        }
    }

    public void insertarSesionUsuario(String direccion_ip_usuario, String puerto_usuario) {
        logger.debug("Insertando session: " + direccion_ip_usuario + ", " + puerto_usuario);
        Conexion con = new Conexion();
        try {
            con.conectar();
            con.contruirSQL("INSERT INTO sesiones_chat (estado, direccion_ip, puerto, fecha_activo) VALUES (?, ?, ?, 'now()');");
            con.getPst().setInt(1, 0);
            con.getPst().setString(2, direccion_ip_usuario);
            con.getPst().setString(3, puerto_usuario);
            con.ejecutarSQL();

        } catch (Exception ex) {
            logger.error(ex.toString());
        } finally {
            con.cerrarConexiones();
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //Esta clase nos servira para pre-configurar los parametros del xat
        new Servidor();
    }

}
