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
public class Usuario {

    private int id_usuario;
    private String usuario;
    private String clave;
    private Timestamp fecha_creacion;
    private int estado;
    private String motivo_desconexion;

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Timestamp getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(Timestamp fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getMotivo_desconexion() {
        return motivo_desconexion;
    }

    public void setMotivo_desconexion(String motivo_desconexion) {
        this.motivo_desconexion = motivo_desconexion;
    }

    
}
