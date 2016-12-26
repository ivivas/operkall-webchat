/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.controllers;

import cl.adportas.db.Conexion;
import cl.adportas.models.ConfiguracionChat;
import cl.adportas.models.Mensaje;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author mario
 */
public final class SocketUsuarioWeb {

    private Socket socketEscucha = null;
    private BufferedReader brLectura = null;
    private PrintWriter pwEscritura = null;
    private boolean flag = true;
    private String sessionID;
    private Mensaje mensaje;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(SocketUsuarioWeb.class);

    public SocketUsuarioWeb(String sessionID) {
        this.flag = true;
        this.sessionID = sessionID;
        this.mensaje = new Mensaje(sessionID);
        nuevaConexion();
        initLeerMensajes();
        initEnviarMensajes();
    }

    public void nuevaConexion() {
        try {
            ConfiguracionChat cc = obtenerConfiguracionServidorChat();
            this.socketEscucha = new Socket(cc.getIp_servidor(), Integer.parseInt(cc.getPuerto_servidor()));
            logger.info("Iniciando nueva conexion socket para chat: " + this.socketEscucha.toString());

        } catch (NumberFormatException | IOException e) {
            logger.error(e.toString());
        }
    }

    public ConfiguracionChat obtenerConfiguracionServidorChat() {
        ConfiguracionChat respuesta = new ConfiguracionChat();
        Conexion con = new Conexion();

        String query = "select * from configuracion_chat ;";
        try {
            con.conectar();
            con.contruirSQL(query);
            con.ejecutarSQLBusqueda();
            if (con.getRs().next()) {
                respuesta.setIp_servidor(con.getRs().getString("ip_servidor").trim());
                respuesta.setPuerto_servidor(con.getRs().getString("puerto_servidor").trim());
            }

        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            con.cerrarConexiones();
        }
        return respuesta;
    }

    public void initLeerMensajes() {
        logger.info("Iniciando hilo para socket de lectura de mensajes.");
        Thread leerXat = new Thread(() -> {
            try {
                brLectura = new BufferedReader(new InputStreamReader(socketEscucha.getInputStream(), "UTF-8"));
                while (flag) {
                    String textoRecibido = brLectura.readLine();
                    if (textoRecibido != null) {
                        mensaje.escribir(textoRecibido);
                        logger.debug("Mensaje recibido: " + textoRecibido);
                    }
                }
            } catch (Exception e) {
                logger.error("Cerrando session web");
                logger.error(e.toString());
            }
        });
        leerXat.start();
    }

    public void initEnviarMensajes() {
        logger.info("Iniciando canal para socket de envío de mensajes.");
        try {
            this.pwEscritura = new PrintWriter(this.socketEscucha.getOutputStream(), true);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
    

    public void enviarMensajes(String mensaje) {
        try {
            this.pwEscritura.println(mensaje);
            logger.debug("Mensaje enviado: " + mensaje);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void cerrarSession() {
        logger.debug("Cerrando sesion.");
        this.flag = false;
        try {
            if (this.brLectura != null) {
                this.brLectura = null;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        if (this.pwEscritura != null) {
            try {
                this.pwEscritura = null;
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        if (this.socketEscucha != null) {
            try {
                this.socketEscucha.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public Mensaje getMensaje() {
        return mensaje;
    }

    public void setMensaje(Mensaje mensaje) {
        this.mensaje = mensaje;
    }

}
