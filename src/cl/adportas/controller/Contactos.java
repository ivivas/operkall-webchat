/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.controller;

import cl.adportas.bd.Conexion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import messengerchatkallservidor.Servidor;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author mario
 */
public final class Contactos {

    private Socket socket;
    private String direccion_ip;
    private String puerto;
    private String id_usuario;
    private String usuario = "";
    private HashMap mapMensaje;
    private HashMap mapMensajeGrupal;
    BufferedReader brLectura = null;
    PrintWriter pwEscritura = null;
    boolean flag;
    private String id_ejecutivoACD = "-1";
    private String nombre_ejecutivoACD = "S/A";
    private HashMap mapPrimerMensajeEjecutivo;
    private Timer timer;
    private Queue ejecutivosDisponibles;
    private int algoritmoReconexion;
    private int segundosDeEsperaMaximo;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Contactos.class);

    public Contactos(Socket socket, String direccion_ip, String id_usuario, String puerto) {
        try {
            this.flag = true;
            this.id_usuario = id_usuario;
            this.socket = socket;
            this.direccion_ip = direccion_ip;
            this.puerto = puerto;
            leerMensajes();
            pwEscritura = new PrintWriter(this.socket.getOutputStream(), true);
            this.mapMensaje = new HashMap();
            this.mapMensajeGrupal = new HashMap();
            this.mapPrimerMensajeEjecutivo = new HashMap();
            this.timer = new Timer();
            this.ejecutivosDisponibles = new LinkedList();
            this.algoritmoReconexion = 2;
            this.segundosDeEsperaMaximo = 30;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
    
    public void leerMensajes() {
        Thread leerXat = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    brLectura = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    while (flag) {
                        try {
                            // Se leen primero los bytes y luego se hace la conversión a string para que garantizar el despliegue de acentos y caracteres especiales
                            byte[] bytesRecibidos = brLectura.readLine().getBytes(StandardCharsets.UTF_8);
                            String textoRecibido = new String(bytesRecibidos);
                            logger.debug("Texto recibido: " + textoRecibido);
                            if (textoRecibido != null) {
                                if (textoRecibido.startsWith("textogrupal")) {
                                    String[] cadena = textoRecibido.split("_#_");
                                    String id_grupo_receptor = cadena[1] != null ? cadena[1].trim() : "-1";
                                    String id_usuario_emisor = cadena[2] != null ? cadena[2].trim() : "-1";
                                    if (cadena != null && cadena.length > 2) {
                                        Conexion conAux = new Conexion();
                                        try {
                                            conAux.conectar();
                                            conAux.contruirSQL("select * from sesiones_chat where fk_id_usuario in ( "
                                                    + "select id_usuario from grupo_usuario_chat where id_grupo = " + id_grupo_receptor + " "
                                                    + ") and fk_id_usuario != " + id_usuario_emisor + " and tipo_sesion = 1" + ";");
                                            conAux.ejecutarSQLBusqueda();
                                            while (conAux.getRs().next()) {
                                                String direccion_ipAUX = conAux.getRs().getString("direccion_ip") != null ? conAux.getRs().getString("direccion_ip").trim() : "";
                                                String puertoAUX = conAux.getRs().getString("puerto") != null ? conAux.getRs().getString("puerto").trim() : "";
                                                String id_usuarioAux = direccion_ipAUX + "_" + puertoAUX;
                                                if (Servidor.mapContactos.containsKey(id_usuarioAux)) {
                                                    Contactos contacts = (Contactos) Servidor.mapContactos.get(id_usuarioAux);
                                                    buscarMensajeAEscribir(id_grupo_receptor, cadena[3], contacts, 2);
//                                                    Mensaje mensajeAux;
//                                                    if (mapMensajeGrupal.containsKey(id_grupo_receptor)) {
//                                                        mensajeAux = (Mensaje) mapMensajeGrupal.get(id_grupo_receptor);
//                                                        mensajeAux.escribir(cadena[3]);
//                                                    } else if (contacts.getMapMensajeGrupal().containsKey(id_grupo_receptor)) {
//                                                        mensajeAux = (Mensaje) contacts.getMapMensajeGrupal().get(id_grupo_receptor);
//                                                        mensajeAux.escribir(cadena[3]);
//                                                    } else {
//                                                        mensajeAux = new Mensaje(id_grupo_receptor);
//                                                        mensajeAux.escribir(cadena[3]);
//                                                        mapMensajeGrupal.put(id_grupo_receptor, mensajeAux);
//                                                    }
                                                    contacts.enviarMensajes("textogrupal_#_" + id_grupo_receptor + "_#_" + id_usuario + "_#_" + cadena[3]);
                                                    insertarRegistroMensajeChat(cadena[3], id_usuario_emisor, "", "", "", id_grupo_receptor, "");
                                                }
                                            }
                                        } catch (Exception e) {
                                            logger.error(e.toString());
                                        } finally {
                                            conAux.cerrarConexiones();
                                        }
                                    }
                                }
                                else if (textoRecibido.startsWith("conexionNuevaClienteWeb")) {
                                    String[] cadena = textoRecibido.split("_#_");
                                    String nombreClienteWeb = cadena[1] != null ? cadena[1].trim() : "-1";
                                    String telefonoClienteWeb = cadena[2] != null ? cadena[2].replaceAll("\\s", "") : "-1";
                                    String correoClienteWeb = cadena[3] != null ? cadena[3].trim() : "-1";
                                    String ciudadClienteWeb = cadena[4] != null ? cadena[4].trim() : "-1";
                                    String asuntoClienteWeb = cadena[5] != null ? cadena[5].trim() : "-1";
                                    
                                    llenarListaEjecutivosDisponibles();
                                    conectarClientewebConEjecutivo(nombreClienteWeb);
                                    
                                    // Se inserta datos del cliente en tabla clientes_chat
                                    Conexion con = new Conexion();
                                    try {
                                        logger.info("Insertando cliente web en tabla clientes_chat: " + nombreClienteWeb);
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                                        
                                        // Fecha y hora actual
                                        Date date = new Date();
                                        Timestamp tsActual = new Timestamp(date.getTime());
                                        //System.out.println(sdf.format(cal1.getTime()) );
                                        
                                        con.conectar();
                                        con.contruirSQL("insert into clientes_chat (nombre, telefono, correo, ciudad, asunto, fecha_conexion, ip, puerto) values (?, ?, ?, ?, ?, ?, ?, ?)");
                                        con.getPst().setString(1, nombreClienteWeb);
                                        con.getPst().setString(2, telefonoClienteWeb);
                                        con.getPst().setString(3, correoClienteWeb);
                                        con.getPst().setString(4, ciudadClienteWeb);
                                        con.getPst().setString(5, asuntoClienteWeb);
                                        con.getPst().setTimestamp(6, tsActual);
                                        con.getPst().setString(7, direccion_ip);
                                        con.getPst().setString(8, puerto);
                                        con.ejecutarSQL();
                                    } catch (Exception e) {
                                        logger.error(e.toString());
                                    } finally {
                                        con.cerrarConexiones();
                                    }
                                    
                                    /**
                                     * Este timer chequea cada 30 segundos (esto debe ser configurable) si el ejecutivo respondió al cliente web, si no respondió, busca otro ejecutivo. 
                                     * Cuando el ejecutivo responde, la tabla clientes_chat se actualiza y se insertan el id del ejecutivo 
                                     * de contraparte y el tiempo de espera del cliente web.
                                     */
                                    TimerTask task = new TimerTask() {
                                        String ipCliente = direccion_ip;
                                        String puertoCliente = puerto;
                                        String id_usuario_receptor = ipCliente + "_" + puertoCliente;
                                        @Override
                                        public void run() {
                                            try {
                                                con.conectar();
                                                con.contruirSQL("select id_ejecutivo_contraparte from clientes_chat where ip = ? and puerto = ?");
                                                con.getPst().setString(1, ipCliente);
                                                con.getPst().setString(2, puertoCliente);
                                                con.ejecutarSQLBusqueda();
                                                con.getRs().next();
                                                String idEjecutivo = con.getRs().getString("id_ejecutivo_contraparte") == null ? "" : con.getRs().getString("id_ejecutivo_contraparte").trim();
                                                con.cerrarConexiones();
                                                if (idEjecutivo.equals("")) {
                                                    logger.info("Ejecutivo no responde. Reconectando con otro agente");
                                                    enviarMensajes("Ejecutivo no responde. Reconectando con otro agente");
                                                    //ejecutivosDisponibles.remove(id_ejecutivoACD);
                                                    actualizarUsuarioBD("1", "");
                                                    id_ejecutivoACD = "-1";
                                                    nombre_ejecutivoACD = "S/A";
                                                    conectarClientewebConEjecutivo(nombreClienteWeb);
                                                }
                                                else {
                                                    logger.info("Ejecutivo respondió. Se cancela el timer");
                                                    this.cancel();
                                                }
                                            }
                                            catch (SQLException | NumberFormatException e) {
                                                logger.error(e.getMessage());
                                            } catch (Throwable ex) {
                                                Logger.getLogger(Contactos.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                         }
                                    };
                                    timer.schedule(task, segundosDeEsperaMaximo*1000, segundosDeEsperaMaximo*1000);
                                    
                                    
                                }
                                // Mensaje de un ejecutivo a otro ejecutivo
                                else if (textoRecibido.startsWith("textoejecutivo")) {                      
                                    String[] cadena = textoRecibido.split("_#_");
                                    String prefijo = cadena[0] != null ? cadena[0].trim() : "";
                                    String id_receptor_socket = cadena[1] != null ? cadena[1].trim() : "";
                                    String id_receptor = cadena[2] != null ? cadena[2].trim() : "";
                                    String mensaje = cadena[3] != null ? cadena[3].trim() : "";
                                    
                                    if (cadena != null && cadena.length > 1) {
                                        if (Servidor.mapContactos.containsKey(id_receptor_socket)) {
                                            Contactos contacts = (Contactos) Servidor.mapContactos.get(id_receptor_socket);
                                            buscarMensajeAEscribir(id_receptor_socket, mensaje, contacts, 1);
                                            insertarRegistroMensajeChat(mensaje, id_usuario, "", "", id_receptor_socket, "", "");
                                            mensaje = "textoejecutivo_#_" + id_usuario + "_#_" + mensaje;
                                            contacts.enviarMensajes(mensaje);
                                        }
                                    }
                                }
                                // Mensaje del ejecutivo al cliente web
                                else if (textoRecibido.startsWith("textoclientewebejecutivo")) {
                                    String[] cadena = textoRecibido.split("_#_");
                                    String id_usuario_receptor = cadena[1] != null ? cadena[1].trim() : "-1";
                                    String mensaje = cadena[2];
                                    if (Servidor.mapContactos.containsKey(id_usuario_receptor)) {
                                        Contactos contacts = (Contactos) Servidor.mapContactos.get(id_usuario_receptor);
                                        buscarMensajeAEscribir(id_usuario_receptor, mensaje, contacts, 3);
                                        contacts.enviarMensajes(mensaje);
                                        insertarRegistroMensajeChat(mensaje, contacts.id_ejecutivoACD, "", "", "", "", id_usuario_receptor);

                                        // Se busca en el hashmap si el ejecutivo ya respondió al cliente web por primera vez
                                        if (mapPrimerMensajeEjecutivo.get(id_usuario_receptor) == null) {
                                            timer.cancel();
                                            actualizarTablaClientesChat(id_usuario_receptor, contacts.id_ejecutivoACD);
                                        }
                                    }
                                }
                                // Mensaje del cliente web al ejecutivo
                                else if (textoRecibido.startsWith("textoclienteweb")) {
                                    String[] cadena = textoRecibido.split("_#_");
                                    String id_usuario_receptor = id_ejecutivoACD;
                                    if (cadena != null && cadena.length > 1) {
                                        String mensaje = cadena[1];
                                        enviarMensaje(id_usuario_receptor, "textoclienteweb", mensaje);
                                    }
                                }
                                // Si el chat es finalizado por parte del ejecutivo:
                                else if (textoRecibido.startsWith("finalizartextoclienteweb"))  {
                                    logger.debug("Finalizando chat por parte Ejecutivo.");
                                    String[] cadena = textoRecibido.split("_#_");
                                    String[] cadenaAux = null;
                                    String id_usuario_final_socket = "";
                                    String ipClienteWeb = "";
                                    String puertoClienteWeb = "";
                                    Contactos contacts = null;
                                    String prefijo = cadena[0];
                                    String mensaje = "";
                                    if (cadena != null && cadena.length > 1) {
                                        id_usuario_final_socket = cadena[1] != null ? cadena[1].trim() : "-1";
                                        cadenaAux = id_usuario_final_socket.split("_");
                                        ipClienteWeb = cadenaAux[0];
                                        puertoClienteWeb = cadenaAux[1];
                                                                              
                                        mensaje = cadena[2];
                                        if (Servidor.mapContactos.containsKey(id_usuario_final_socket)) {
                                            contacts = (Contactos) Servidor.mapContactos.get(id_usuario_final_socket);
                                            contacts.enviarMensajes(mensaje);
                                            
                                            actualizarClientesChat(ipClienteWeb, puertoClienteWeb);
                                            contacts.actualizarUsuarioBD("1", "");
                                            contacts.eliminarSocketDeServidor();
                                        }
                                        id_ejecutivoACD = "-1";
                                        nombre_ejecutivoACD = "S/A";
                                    }
                                }
                                // Si el chat es finalizado por parte del cliente web:
                                else if (textoRecibido.startsWith("finConexionClienteWeb")) {
                                    logger.debug("Finalizando chat por parte del cliente web");
                                    String[] cadena = textoRecibido.split("_#_");
                                    String id_usuario_receptor = "";
                                    String nombreClienteWeb = "";
                                    
                                    id_usuario_receptor = id_ejecutivoACD;
                                    nombreClienteWeb = cadena[1];
                                    enviarMensaje(id_usuario_receptor, "finConexionClienteWeb", nombreClienteWeb);

                                    actualizarClientesChat(direccion_ip, puerto);
                                    actualizarUsuarioBD("1", "");
                                    eliminarSocketDeServidor();
                                    id_ejecutivoACD = "-1";
                                    nombre_ejecutivoACD = "S/A";
                                }
                                // Se sustituyó por textoejecutivo (eliminar despues de las pruebas)
                                else if (textoRecibido.startsWith("textousuario")) {
                                    /*
                                    //enviarMensajes("Respuesta desde el servidor: " + direccion_ip + "_" + puerto);
                                    String[] cadena = textoRecibido.split("_#_");
                                    String id_usuario_receptor = cadena[1] != null ? cadena[1].trim() : "-1";
                                    if (cadena != null && cadena.length > 1) {
                                        Contactos contacts = (Contactos) Servidor.mapContactos.get(id_usuario_receptor);
                                        buscarMensajeAEscribir(id_usuario_receptor, cadena[2], contacts, 1);
                                        contacts.enviarMensajes("textousuario_#_" + id_usuario + "_#_" + cadena[2]);
                                        insertarRegistroMensajeChat(cadena[2], id_usuario, "", "", id_usuario_receptor, "", "");
                                    }
                                            */
                                }
                            } 
                            else {
                                try {
                                    eliminarSocketDeServidor();
                                } catch (Exception e) {
                                    logger.error(e.toString());
                                } finally {
                                    try {
                                        this.finalize();
                                    } catch (Throwable e) {
                                        logger.error(e.toString());
                                    }
                                }
                            }
                        } 
                        catch (IOException e) {
                            logger.error(e.toString());
                            try {
                                eliminarSocketDeServidor();
                                try {
                                    this.finalize();
                                } catch (Throwable ex) {
                                    logger.error(ex.toString());
                                }
                            } catch (Exception ex) {
                                logger.error(ex.toString());
                            }
                        }
                    }
                } 
                catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        });
        leerXat.start();
    }
    
    /**
     * Método para llenar la lista de ejecutivos disponibles (ordenados por ultima_conexion_chat).
     * Se utiliza en los algoritmos de reconexión cuando un ejecutivo no responde al cliente web en el tiempo establecido
     */
    public void llenarListaEjecutivosDisponibles() {
        Conexion conexion = new Conexion();
        int idAux = -1;
        try {
            conexion.conectar();
            conexion.contruirSQL("select id from secretaria where estado_chat = 1 order by ultima_conexion_chat asc");
            conexion.ejecutarSQLBusqueda();
            ejecutivosDisponibles.clear();
            while (conexion.getRs().next()) {
                idAux = conexion.getRs().getInt("id");
                ejecutivosDisponibles.add(String.valueOf(idAux));
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        finally {
            conexion.cerrarConexiones();
        }
    }
    
    /**
     * Funcion solo para debug
     */
    public void imprimirListaEjecutivosDisponibles() {
        if (ejecutivosDisponibles.size() > 0) {
            for (Object ejecutivo : ejecutivosDisponibles) {
                System.out.println("ejecutivo: " + ejecutivo.toString());
            }
        }
        else {
            System.out.println("Lista vacia");
        }
        
    }
    
    /**
     * Método para conectar el cliente web con un ejecutivo disponible.
     * 
     * @param nombreClienteWeb
     */
    public void conectarClientewebConEjecutivo(String nombreClienteWeb) {
        String ejecutivoActual = "";
        /**
        * Algoritmo Nº1 Top-Down:
        * Se obtiene (y se extrae) el primer id de la lista y se conecta con ese ejecutivo. 
        * Si la lista queda vacía, se envía un mensaje al cliente web notificando que no hay ejecutivos.
        *
        * Algoritmo Nº2 Circular:
        * reconecta con el siguiente de la lista hasta llegar al final. 
        * Luego, vuelve a empezar ya que los ids son insertados de nuevo en la cola
        */
       if ((algoritmoReconexion == 1) || (algoritmoReconexion == 2)) {            
            if (algoritmoReconexion == 1) {
                ejecutivoActual = (String) ejecutivosDisponibles.poll();
            }
            else if (algoritmoReconexion == 2) {
                ejecutivoActual = (String) ejecutivosDisponibles.poll();
                ejecutivosDisponibles.add(ejecutivoActual);
            }
            enviarMensajes("Buscando ejecutivo disponible. Por favor espere.");
            Conexion conexion1 = new Conexion();
            try {
                conexion1.conectar();
                conexion1.contruirSQL("update secretaria set estado_chat = 2 where id = " + ejecutivoActual + " returning id, nombre");
                conexion1.ejecutarSQLBusqueda();
                conexion1.getRs().next();
                id_ejecutivoACD = conexion1.getRs().getInt("id") + "";
                nombre_ejecutivoACD = conexion1.getRs().getString("nombre") != null ? conexion1.getRs().getString("nombre").trim() : "S/A";

            }
            catch (NumberFormatException | SQLException e) {
                logger.error(e.getMessage());
            }
            finally {
                conexion1.cerrarConexiones();
            }
            
            if (ejecutivoActual != null) {
                enviarMensajes("Gracias por esperar.");
                enviarMensajes("Su ejecutivo es: " + nombre_ejecutivoACD);

                // Se actualiza el estado = 1 en la tabla sesiones_chat para el ejecutivo
                Conexion conexion2 = new Conexion();
                try {
                    conexion2.conectar();
                    conexion2.contruirSQL("update sesiones_chat set estado = 1, fk_id_usuario = " + id_ejecutivoACD + ", usuario = ?, tipo_sesion = ? where direccion_ip = ? and puerto = ?;");
                    conexion2.getPst().setString(1, nombreClienteWeb);
                    conexion2.getPst().setInt(2, 3);
                    conexion2.getPst().setString(3, direccion_ip);
                    conexion2.getPst().setString(4, puerto);
                    conexion2.ejecutarSQL();
                } catch (Exception e) {
                    logger.error(e.toString());
                } finally {
                    conexion2.cerrarConexiones();
                }
            } 
            else {
                enviarMensajes("No hay ejecutivos disponibles, int&eacute;ntelo m&aacute;s tarde.");
                
                actualizarClientesChat(direccion_ip, puerto);
                actualizarUsuarioBD("1", "");
                eliminarSocketDeServidor();
                id_ejecutivoACD = "-1";
                nombre_ejecutivoACD = "S/A";
            }
       }
       else if (algoritmoReconexion == 2) {

       }
       else if (algoritmoReconexion == 3) {

       }
    }
    

    public void buscarMensajeAEscribir(String id_usuario_receptor, String mensaje, Contactos contacts, int tipo) {
        Mensaje mensajeAux = null;
        try {
            if (tipo == 1 || tipo == 3) {
                if (mapMensaje.containsKey(id_usuario_receptor)) {
                    mensajeAux = (Mensaje) mapMensaje.get(id_usuario_receptor);
                    mensajeAux.escribir(mensaje);
                } else if (contacts.getMapMensaje().containsKey(id_usuario)) {
                    mensajeAux = (Mensaje) contacts.getMapMensaje().get(id_usuario);
                    mensajeAux.escribir(mensaje);
                } else {
                    mensajeAux = new Mensaje(id_usuario_receptor);
                    mensajeAux.escribir(mensaje);
                    mapMensaje.put(id_usuario_receptor, mensajeAux);
                }
            } else if (tipo == 2) {
                if (mapMensajeGrupal.containsKey(id_usuario_receptor)) {
                    mensajeAux = (Mensaje) mapMensajeGrupal.get(id_usuario_receptor);
                    mensajeAux.escribir(mensaje);
                } else if (contacts.getMapMensajeGrupal().containsKey(id_usuario_receptor)) {
                    mensajeAux = (Mensaje) contacts.getMapMensajeGrupal().get(id_usuario_receptor);
                    mensajeAux.escribir(mensaje);
                } else {
                    mensajeAux = new Mensaje(id_usuario_receptor);
                    mensajeAux.escribir(mensaje);
                    mapMensajeGrupal.put(id_usuario_receptor, mensajeAux);
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
//            return mensajeAux;
        }
    }

    /**
     * Método para actualizar la tabla clientes_chat cuando el ejecutivo responde por primera vez al cliente web.
     * Calcula el tiempo de espera del cliente y lo inserta en la tabla. 
     * También inserta el id del ejecutivo con el cual el cliente está chateando.
     * 
     * @param id_clienteWeb
     * @param idEjecutivo
     */
    public void actualizarTablaClientesChat(String id_clienteWeb, String idEjecutivo) {
        logger.debug("Actualizando tabla clientes_chat");
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String[] cadena = id_clienteWeb.split("_");
            String ipAux = cadena[0];
            String puertoAux = cadena[1];
            
            // Se obtiene la fecha de conexion del cliente web para calcular el tiempo de espera.
            Conexion con = new Conexion();
            con.conectar();
            con.contruirSQL("select fecha_conexion from clientes_chat where ip = '" + ipAux + "' and puerto = '" + puertoAux + "';");
            con.ejecutarSQLBusqueda();
            con.getRs().next();
            String fechaConexion = con.getRs().getString("fecha_conexion");
            
            Date parsedDate = dateFormat.parse(fechaConexion);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            long startTimeMilisec = timestamp.getTime();
            
            Calendar endTime = Calendar.getInstance();
            long endTimeMilisec = endTime.getTimeInMillis();
            
            double tiempoEsperaMiliseg  = endTimeMilisec - startTimeMilisec;
            int tiempoEsperaSeg = (int) Math.round(tiempoEsperaMiliseg/1000);
            
            // Se actualiza el tiempo de espera y el id del ejecutivo de contraparte para el cliente web correspondiente
            con.contruirSQL("update clientes_chat set tiempo_espera = " + tiempoEsperaSeg + " , id_ejecutivo_contraparte = '" + idEjecutivo + "' " + "where ip = '" + ipAux + "' and puerto = '" + puertoAux + "';");
            con.ejecutarSQL();
            con.cerrarConexiones();
            
            mapPrimerMensajeEjecutivo.put(id_clienteWeb, true);
        }
        catch (SQLException | ParseException e) {
            logger.error(e.getMessage());
        }
    }
    
    /**
     * Método para actualizar la fecha de desconexion en la tabla clientes_chat.
     * 
     * @param ip
     * @param puerto
     */
    public void actualizarClientesChat(String ip, String puerto) {
        
        // Se actualiza tabla clientes_chat y se agrega tiempo de desconexion
        logger.info("Actualizando fecha de desconexión de cliente web en tabla clientes_chat: " );

        Conexion con = new Conexion();
        try {
            con.conectar();
            con.contruirSQL("update clientes_chat set fecha_desconexion = now() where ip = ? and puerto = ?");
            con.getPst().setString(1, ip);
            con.getPst().setString(2, puerto);
            con.ejecutarSQLUpdate();
        } 
        catch (Exception e) {
            logger.error(e.toString());
        } 
        finally {
            con.cerrarConexiones();
        }
                                                
    }

    public void eliminarSocketDeServidor() {
        id_ejecutivoACD = "-1";
        nombre_ejecutivoACD = "S/A";
        flag = false;
        if (brLectura != null) {
            try {
                logger.debug("Cerrando BufferedReader (brLectura)");
                brLectura.close();
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        if (pwEscritura != null) {
            try {
                logger.debug("Cerrando PrintWriter (pwEscritura)");
                pwEscritura.close();
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        if (socket != null) {
            try {
                logger.debug("Cerrando " + socket.toString());
                socket.close();
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        try {
            if (Servidor.mapContactos.containsKey(id_usuario)) {
                logger.debug("Eliminando " + id_usuario + " del mapContactos");
                Servidor.mapContactos.remove(id_usuario);
                eliminarRegistroSession();
//                actualizarEstadoUsuario(); //
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void actualizarEstadoUsuario() {
        try {
            Conexion con = new Conexion();
            con.conectar();
            con.contruirSQL("update secretaria WHERE direccion_ip = '" + (this.direccion_ip != null ? this.direccion_ip.trim() : "") + "' and puerto = '" + (this.puerto != null ? this.puerto.trim() : "") + "' ;");
            con.ejecutarSQL();
            logger.debug("SQL: " + con.getPst().toString());
            con.cerrarConexiones();
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            //System.out.println("UPDAATE");
        }
    }

    public void insertarRegistroMensajeChat(String mensaje, String id_usuario_emisor, String id_grupo_emisor, String id_cliente_emisor, String id_usuario_receptor, String id_grupo_receptor, String id_cliente_receptor) {
        logger.debug("Insertando mensaje en tabla mensaje: " + mensaje);
        
        Conexion con = new Conexion();
        try {
            con.conectar();
            con.contruirSQL("insert into mensaje_chat (mensaje, fecha_registro, id_usuario_emisor, id_grupo_emisor, id_cliente_emisor, id_usuario_receptor, id_grupo_receptor, id_cliente_receptor) values (?,now(),?,?,?,?,?,?);");
            con.getPst().setString(1, mensaje);
            con.getPst().setString(2, id_usuario_emisor);
            con.getPst().setString(3, id_grupo_emisor);
            con.getPst().setString(4, id_cliente_emisor);
            con.getPst().setString(5, id_usuario_receptor);
            con.getPst().setString(6, id_grupo_receptor);
            con.getPst().setString(7, id_cliente_receptor);
            con.ejecutarSQL();
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            con.cerrarConexiones();
        }
    }

    public void enviarMensajes(String mensaje) {
        try {
            if (pwEscritura != null) {
                logger.debug("Enviando mensaje: " + mensaje);
                pwEscritura.println(mensaje);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }
    
    public void enviarMensaje(String idReceptor, String idMensaje, String mensaje) {
        Conexion conAux = new Conexion();
        try {
            conAux.conectar();
            conAux.contruirSQL(" select * from sesiones_chat where fk_id_usuario = " + idReceptor + " and (tipo_sesion != 3 or tipo_sesion is null );");
            conAux.ejecutarSQLBusqueda();
            while (conAux.getRs().next()) {
                String direccion_ipAUX = conAux.getRs().getString("direccion_ip") != null ? conAux.getRs().getString("direccion_ip").trim() : "";
                String puertoAUX = conAux.getRs().getString("puerto") != null ? conAux.getRs().getString("puerto").trim() : "";
                String id_usuarioAux = direccion_ipAUX + "_" + puertoAUX;
                if (Servidor.mapContactos.containsKey(id_usuarioAux)) {
                    Contactos contacts = (Contactos) Servidor.mapContactos.get(id_usuarioAux);

                    buscarMensajeAEscribir(idReceptor, mensaje, contacts, 3);

                    // Asi se envia mensaje al receptor
                    contacts.enviarMensajes(idMensaje + "_#_" + id_usuario + "_#_" + mensaje);
                    // Asi se envia mensaje al emisor
                    //enviarMensajes("textoclienteweb_#_" + id_usuario + "_#_" + mensaje + " 2");

                    insertarRegistroMensajeChat(mensaje, "", "", id_usuario, idReceptor, "", "");
                }
            }
            //Mensaje sera guardado en base de datos
            //insertarRegistroMensajeChat(cliente, estado, direccion_ip, puerto, fk_id_usuario, rut, tipo_sesion, mensaje);
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            conAux.cerrarConexiones();
        }
    }

    public String buscarEjecutivoDisponibleParaChat() {
        int id_usuarioEjecutivo = -1;
        Conexion con = new Conexion();
        try {
            con.conectar();
            con.contruirSQL("update secretaria set estado_chat = 2 where id in (select id from secretaria where estado_chat = 1 order by ultima_conexion_chat asc limit 1) returning id");
//            con.contruirSQL("select id_usuario from usuarios where estado = 1 order by ultima_conexion asc limit 1");
            con.ejecutarSQLBusqueda();
            while (con.getRs().next()) {
                id_usuarioEjecutivo = con.getRs().getInt("id");
            }
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            con.cerrarConexiones();
        }
        logger.debug("Ejecutivodisponible para chat: " + id_usuarioEjecutivo);
        return String.valueOf(id_usuarioEjecutivo);
    }

    public void eliminarRegistroSession() {
        try {
            Conexion con = new Conexion();
            con.conectar();
            con.contruirSQL("delete from sesiones_chat where direccion_ip = '" + (this.direccion_ip != null ? this.direccion_ip.trim() : "") + "' and puerto = '" + (this.puerto != null ? this.puerto.trim() : "") + "' ;");
            con.ejecutarSQL();
            //logger.debug("SQL: " + con.getPst().toString());
            con.cerrarConexiones();
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            logger.debug("Sesion: " + this.direccion_ip + "_" + this.puerto + " eliminada de la tabla sesiones");
        }
    }

    public void actualizarUsuarioBD(String estado, String mensaje) {
        logger.debug("Actualizando tabla secretaria por desconexion. Estado: " + estado + ", IdUsuario: " + id_ejecutivoACD);
        Conexion con = new Conexion();
        con.conectar();
        con.contruirSQL("update secretaria set estado_chat = " + estado + " , motivo_desconexion_chat = '" + mensaje + "' "
                + "where id = " + id_ejecutivoACD + " ;");
        con.ejecutarSQL();
        con.cerrarConexiones();
    }

    public HashMap getMapMensaje() {
        return mapMensaje;
    }

    public void setMapMensaje(HashMap mapMensaje) {
        this.mapMensaje = mapMensaje;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public HashMap getMapMensajeGrupal() {
        return mapMensajeGrupal;
    }

    public void setMapMensajeGrupal(HashMap mapMensajeGrupal) {
        this.mapMensajeGrupal = mapMensajeGrupal;
    }

}
