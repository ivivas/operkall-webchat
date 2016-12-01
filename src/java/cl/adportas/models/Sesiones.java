/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.models;

import java.sql.Timestamp;

/**
 *
 * @author mario
 */
public class Sesiones {
    
    private int id_sesion;
    private String usuario;
    private String nickname;
    private String estado;
    private String direccion_ip;
    private String puerto;
    private int fk_id_usuario;
    private Timestamp fecha_activo;

    public int getId_sesion() {
        return id_sesion;
    }

    public void setId_sesion(int id_sesion) {
        this.id_sesion = id_sesion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDireccion_ip() {
        return direccion_ip;
    }

    public void setDireccion_ip(String direccion_ip) {
        this.direccion_ip = direccion_ip;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }

    public int getFk_id_usuario() {
        return fk_id_usuario;
    }

    public void setFk_id_usuario(int fk_id_usuario) {
        this.fk_id_usuario = fk_id_usuario;
    }

    public Timestamp getFecha_activo() {
        return fecha_activo;
    }

    public void setFecha_activo(Timestamp fecha_activo) {
        this.fecha_activo = fecha_activo;
    }
    
     
}
