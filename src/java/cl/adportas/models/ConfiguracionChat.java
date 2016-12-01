/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.models;

/**
 *
 * @author Mario
 */
public class ConfiguracionChat {

    private String ip_servidor;
    private String puerto_servidor;

    public String getIp_servidor() {
        return ip_servidor;
    }

    public void setIp_servidor(String ip_servidor) {
        this.ip_servidor = ip_servidor;
    }

    public String getPuerto_servidor() {
        return puerto_servidor;
    }

    public void setPuerto_servidor(String puerto_servidor) {
        this.puerto_servidor = puerto_servidor;
    }

}
