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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
                                    //enviarMensajes("Intentando una nueva conexion...");
                                    //generar enlace cliente / ejecutivo Update a la tabla sesion de tipo usuario web
                                    String[] cadena = textoRecibido.split("_#_");
                                    String nombreClienteWeb = cadena[1] != null ? cadena[1].trim() : "-1";
                                    String telefonoClienteWeb = cadena[2] != null ? cadena[2].replaceAll("\\s", "") : "-1";
                                    String correoClienteWeb = cadena[3] != null ? cadena[3].trim() : "-1";
                                    String ciudadClienteWeb = cadena[4] != null ? cadena[4].trim() : "-1";
                                    String asuntoClienteWeb = cadena[5] != null ? cadena[5].trim() : "-1";
                                    
                                    int cont = 0;
                                    while (id_ejecutivoACD.equals("-1") && cont < 30) {
                                        cont++;
                                        enviarMensajes("Por favor, espere mientras un ejecutivo est&eacute; disponible.");
//                                            id_usuarioACD = buscarEjecutivoDisponibleParaChat();
                                        Conexion conAuxACD = new Conexion();
                                        try {
                                            conAuxACD.conectar();
                                            conAuxACD.contruirSQL("update secretaria set estado_chat = 2 where id in (select id from secretaria where estado_chat = 1 order by ultima_conexion_chat asc limit 1) returning id, nombre");
                                            conAuxACD.ejecutarSQLBusqueda();
                                            while (conAuxACD.getRs().next()) {
                                                id_ejecutivoACD = conAuxACD.getRs().getInt("id") + "";
                                                nombre_ejecutivoACD = conAuxACD.getRs().getString("nombre") != null ? conAuxACD.getRs().getString("nombre").trim() : "S/A";
                                            }
                                        } catch (Exception e) {
                                            logger.error(e.toString());
                                        } finally {
                                            conAuxACD.cerrarConexiones();
                                            Thread.sleep(5000);
                                        }

                                    }
                                    if (!id_ejecutivoACD.equals("-1") && !nombre_ejecutivoACD.equals("S/A")) {
                                        Conexion con = new Conexion();
//                                    String id_usuarioACD = "-1";
                                        try {
                                            con.conectar();
                                            con.contruirSQL("update sesiones_chat set estado = 1, fk_id_usuario = " + id_ejecutivoACD + ", usuario = ?, tipo_sesion = ? where direccion_ip = ? and puerto = ?;");
                                            con.getPst().setString(1, nombreClienteWeb);
                                            con.getPst().setInt(2, 3);
                                            con.getPst().setString(3, direccion_ip);
                                            con.getPst().setString(4, puerto);
                                            con.ejecutarSQL();
                                            //enviarMensajes("Conectado: " + direccion_ip + "_" + puerto);
                                            enviarMensajes("Gracias por esperar.");
                                            enviarMensajes("Su ejecutivo es: " + nombre_ejecutivoACD);
                                        } catch (Exception e) {
                                            logger.error(e.toString());
                                        } finally {
                                            con.cerrarConexiones();
                                        }
                                    } else {
                                        enviarMensajes("No hay ejecutivos disponibles, int&eacute;ntelo m&aacute;s tarde.");
                                        //Cerrar session
                                        eliminarSocketDeServidor();
                                    }
                                    
                                    // Se inserta datos del cliente en tabla clientes_chat
                                    Conexion con = new Conexion();
                                    try {
                                        logger.info("Insertando cliente web en tabla clientes_chat: " + nombreClienteWeb);
                                        con.conectar();
                                        con.contruirSQL("insert into clientes_chat (nombre, telefono, correo, ciudad, asunto, fecha_conexion, ip, puerto) values (?, ?, ?, ?, ?, now(), ?, ?)");
                                        con.getPst().setString(1, nombreClienteWeb);
                                        con.getPst().setString(2, telefonoClienteWeb);
                                        con.getPst().setString(3, correoClienteWeb);
                                        con.getPst().setString(4, ciudadClienteWeb);
                                        con.getPst().setString(5, asuntoClienteWeb);
                                        con.getPst().setString(6, direccion_ip);
                                        con.getPst().setString(7, puerto);
                                        con.ejecutarSQL();
                                    } catch (Exception e) {
                                        logger.error(e.toString());
                                    } finally {
                                        con.cerrarConexiones();
                                    }
                                }
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
                                    if (cadena != null && cadena.length > 1) {
                                        if (Servidor.mapContactos.containsKey(id_usuario_receptor)) {
                                            Contactos contacts = (Contactos) Servidor.mapContactos.get(id_usuario_receptor);
                                            buscarMensajeAEscribir(id_usuario_receptor, cadena[2], contacts, 3);
                                            contacts.enviarMensajes(cadena[2]);
                                            insertarRegistroMensajeChat(cadena[2], contacts.id_ejecutivoACD, "", "", "", "", id_usuario_receptor);
                                        }
                                    }
                                }
                                // Mensaje del cliente web al ejecutivo
                                else if (textoRecibido.startsWith("textoclienteweb")) {
                                    String[] cadena = textoRecibido.split("_#_");
                                    String id_usuario_receptor = id_ejecutivoACD;
                                    logger.debug("id_usuario_receptor: " + id_usuario_receptor);
                                    if (cadena != null && cadena.length > 1) {
                                        String mensaje = cadena[1];
                                        //byte[] byteText = mensaje.getBytes(Charset.forName("ISO-8859-1"));
                                        //String mensajeUtf8 = new String(byteText, UTF_8); 
                                        //mensaje = java.net.URLDecoder.decode(mensaje, "UTF-8");
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
                                    
                                    if (cadena != null && cadena.length > 1) {
                                        id_usuario_receptor = id_ejecutivoACD;
                                        nombreClienteWeb = cadena[1];
                                        enviarMensaje(id_usuario_receptor, "finConexionClienteWeb", nombreClienteWeb);
                                        
                                        actualizarClientesChat(direccion_ip, puerto);
                                        actualizarUsuarioBD("1", "");
                                        eliminarSocketDeServidor();
                                        
                                        id_ejecutivoACD = "-1";
                                        nombre_ejecutivoACD = "S/A";
                                    }
                                }
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
                        catch (IOException | InterruptedException e) {
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
    
    public void actualizarClientesChat(String ip, String puerto) {
        
        // Se actualiza tabla clientes_chat y se agrega tiempo de desconexion
        logger.info("Actualizando fecha de desconexión de cliente web en tabla clientes_chat: " );
        //String[] cadenaAux = ipPuertoClienteWeb.split("_");
        //String ipClienteweb = cadenaAux[0];
        //String puertoClienteweb = cadenaAux[1];

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
