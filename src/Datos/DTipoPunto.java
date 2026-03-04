
package Datos;

import Conexion.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class DTipoPunto {

    private DBConnection conn;
    
    
    public DTipoPunto() {
         conn = new DBConnection();
        
    }
    
    public void guardar(String nombre, String descripcion) throws SQLException, ParseException {
        String query = "INSERT INTO tipo_puntos(nombre,descripcion)"
                + " values(?,?)";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, nombre);
        ps.setString(2, descripcion);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DTipoPunto.java dice: "
                    + "Ocurrio un error al agregar un tipo punto de atencion");
            throw new SQLException();
        }
    }

    public void modificar(int id, String nombre, String descripcion) throws SQLException, ParseException {
        String query = "UPDATE tipo_puntos SET nombre=?, descripcion=? "
                + " WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, nombre);
        ps.setString(2, descripcion);
        ps.setInt(3, id);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DTipoPunto.java dice: "
                    + "Ocurrio un error al modificar un tipo punto de atencion");
            throw new SQLException();
        }      
    }
    
    
    public void eliminar(int id) throws SQLException {
        String query = "DELETE FROM tipo_puntos WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DTipoPunto.java dice: "
                    + "Ocurrio un error al eliminar un tipo punto de atencion");
            throw new SQLException();
        }
        
    }
    public List<String[]> listar() throws SQLException {
        List<String[]> tipoPuntos = new ArrayList<>();
        String query = "SELECT * FROM tipo_puntos";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        
        ResultSet set = ps.executeQuery();
        while(set.next()) {
            tipoPuntos.add(new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("nombre"),
                set.getString("descripcion")
            });
        }
        
        return tipoPuntos;
    }
   
    
    public String[] ver(int id) throws SQLException {
        String[] tipoPuntos = null;
        String query = "SELECT * FROM tipo_puntos WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            tipoPuntos = new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("nombre"),
                set.getString("descripcion")
            };
        }        
        return tipoPuntos;
    }
    
    
    public int existe(int id) throws SQLException {
        int idx = -1;
        String query = "SELECT * FROM tipo_puntos WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            idx = set.getInt("id");
        } 
        return idx;
    }
    
    
    public void desconectar(){
        if(conn != null) {
           conn.desconectar();
        }
    }
}
