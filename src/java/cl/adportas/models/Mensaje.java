/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.models;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author mario
 */
public class Mensaje {

    private String id_usuario_contraparte;
    private StringBuilder sb;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Mensaje.class);

    public Mensaje(String id_usuario_contraparte) {
        this.id_usuario_contraparte = id_usuario_contraparte;
        this.sb = new StringBuilder();
    }

    public StringBuilder getSb() {
        return sb;
    }

    public void setSb(StringBuilder sb) {
        this.sb = sb;
    }

    public String getId_usuario_contraparte() {
        return id_usuario_contraparte;
    }

    public void setId_usuario_contraparte(String id_usuario_contraparte) {
        this.id_usuario_contraparte = id_usuario_contraparte;
    }

    public void escribir(String mensaje) {
        try {
            this.sb.append(mensaje).append("\n");
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
