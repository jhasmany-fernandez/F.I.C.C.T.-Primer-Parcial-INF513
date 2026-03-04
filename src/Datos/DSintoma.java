
package Datos;

import Conexion.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class DSintoma {
    private DBConnection conn;
    
    
    public DSintoma() {
         conn = new DBConnection();
        
    }
    
    public void guardar(String nombre, String descripcion) throws SQLException, ParseException {
        String query = "INSERT INTO sintomas(nombre,descripcion)"
                + " values(?,?)";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, nombre);
        ps.setString(2, descripcion);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DSintoma.java dice: "
                    + "Ocurrio un error al agregar un sintoma");
            throw new SQLException();
        }
    }

    public void modificar(int id, String nombre, String descripcion) throws SQLException, ParseException {
        String query = "UPDATE sintomas SET nombre=?, descripcion=? "
                + " WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, nombre);
        ps.setString(2, descripcion);
        ps.setInt(3, id);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DSintoma.java dice: "
                    + "Ocurrio un error al modificar un sintoma");
            throw new SQLException();
        }      
    }
    
    
    public void eliminar(int id) throws SQLException {
        String query = "DELETE FROM sintomas WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DSintoma.java dice: "
                    + "Ocurrio un error al eliminar un sintoma");
            throw new SQLException();
        }
        
    }
    public List<String[]> listar() throws SQLException {
        List<String[]> sintoma = new ArrayList<>();
        String query = "SELECT * FROM sintomas";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        
        ResultSet set = ps.executeQuery();
        while(set.next()) {
            sintoma.add(new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("nombre"),
                set.getString("descripcion")
            });
        }
        
        return sintoma;
    }
   
    
    public String[] ver(int id) throws SQLException {
        String[] sintoma = null;
        String query = "SELECT * FROM sintomas WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            sintoma = new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("nombre"),
                set.getString("descripcion")
            };
        }        
        return sintoma;
    }
    
    
    public int existe(int id) throws SQLException {
        int idx = -1;
        String query = "SELECT * FROM sintomas WHERE id=?";
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
