/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.adportas.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author mario
 */
public class Conexion {

    private Connection conexion;
    private PreparedStatement pst;
    private ResultSet rs;
    private static final Logger logger = LogManager.getLogger(Conexion.class);

    public void conectar() {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
            conexion = DriverManager.getConnection("jdbc:postgresql://192.168.100.91:5432/messenger_chat_kall", "postgres", "adp2011");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            logger.error(e.toString());
        }
    }

    public void contruirSQL(String sql) {
        try {
            this.pst = this.conexion.prepareStatement(sql);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void ejecutarSQLBusqueda() {
        try {
            this.rs = this.pst.executeQuery();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void ejecutarSQL() {
        try {
            this.pst.execute();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
    
    public void ejecutarSQLUpdate() {
        try {
            this.pst.executeUpdate();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void cerrarConexiones() {
        try {
            if (this.rs != null) {
                this.rs.close();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        try {
            if (this.pst != null) {
                this.pst.close();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        try {
            if (this.conexion != null) {
                this.conexion.close();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public Connection getConexion() {
        return conexion;
    }

    public void setConexion(Connection conexion) {
        this.conexion = conexion;
    }

    public PreparedStatement getPst() {
        return pst;
    }

    public void setPst(PreparedStatement pst) {
        this.pst = pst;
    }

    public ResultSet getRs() {
        return rs;
    }

    public void setRs(ResultSet rs) {
        this.rs = rs;
    }

}
