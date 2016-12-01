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
public class Grupo {

    private int id_grupo;
    private String nombre_grupo;
    private String descripcion;
    private Timestamp fecha_creacion;
    private int fk_id_usuario_owner;

    public int getId_grupo() {
        return id_grupo;
    }

    public void setId_grupo(int id_grupo) {
        this.id_grupo = id_grupo;
    }

    public String getNombre_grupo() {
        return nombre_grupo;
    }

    public void setNombre_grupo(String nombre_grupo) {
        this.nombre_grupo = nombre_grupo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(Timestamp fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public int getFk_id_usuario_owner() {
        return fk_id_usuario_owner;
    }

    public void setFk_id_usuario_owner(int fk_id_usuario_owner) {
        this.fk_id_usuario_owner = fk_id_usuario_owner;
    }

}
