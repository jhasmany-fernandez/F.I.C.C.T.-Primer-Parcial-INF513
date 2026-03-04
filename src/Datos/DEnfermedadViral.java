
package Datos;

import Conexion.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class DEnfermedadViral {
     private DBConnection conn;
    
    
    public DEnfermedadViral() {
         this.conn = new DBConnection();
        
    }
    
    public void guardar(String nombre, String descripcion) throws SQLException, ParseException {
        String query = "INSERT INTO enfermedad_virals(nombre,descripcion)"
                + " values(?,?)";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, nombre);
        ps.setString(2, descripcion);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DEnfermedadViral.java dice: "
                    + "Ocurrio un error al agregar una enfermedad viral");
            throw new SQLException();
        }
    }

    public void modificar(int id, String nombre, String descripcion) throws SQLException, ParseException {
        String query = "UPDATE enfermedad_virals SET nombre=?, descripcion=? "
                + " WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, nombre);
        ps.setString(2, descripcion);
        ps.setInt(3, id);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DEnfermedadViral.java dice: "
                    + "Ocurrio un error al modificar una enfermedad viral");
            throw new SQLException();
        }      
    }
    
    
    public void eliminar(int id) throws SQLException {
        String query = "DELETE FROM enfermedad_virals WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DEnfermedadViral.java dice: "
                    + "Ocurrio un error al eliminar una enfermedad viral");
            throw new SQLException();
        }
        
    }
    public List<String[]> listar() throws SQLException {
        List<String[]> enfermedad = new ArrayList<>();
        String query = "SELECT * FROM enfermedad_virals";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        
        ResultSet set = ps.executeQuery();
        while(set.next()) {
            enfermedad.add(new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("nombre"),
                set.getString("descripcion")
            });
        }
        
        return enfermedad;
    }
   
    
    public String[] ver(int id) throws SQLException {
        String[] enfermedad = null;
        String query = "SELECT * FROM enfermedad_virals WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            enfermedad = new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("nombre"),
                set.getString("descripcion")
            };
        }        
        return enfermedad;
    }
    
    
    public int existe(int id) throws SQLException {
        int idx = -1;
        String query = "SELECT * FROM enfermedad_virals WHERE id=?";
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
