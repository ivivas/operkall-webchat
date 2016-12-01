/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.interfaces;

import java.util.List;

/**
 *
 * @author mario
 */
public interface UsuarioInterface {

    public int insertarUsuario(String query);

    public int actualizarUsuario(String query);

    public int eliminarUsuario(String query);

    public List buscarUsuario(String query);
}
