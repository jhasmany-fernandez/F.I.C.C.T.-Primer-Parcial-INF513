
package Datos;

import Conexion.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class DEstado {
     private DBConnection conn;
    
    
    public DEstado() {
         conn = new DBConnection();
        
    }
    
    public void guardar(String estado, String descripcion) throws SQLException, ParseException {
        String query = "INSERT INTO estados(estado,descripcion)"
                + " values(?,?)";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, estado);
        ps.setString(2, descripcion);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DEstado.java dice: "
                    + "Ocurrio un error al agregar un estado");
            throw new SQLException();
        }
    }

    public void modificar(int id, String estado, String descripcion) throws SQLException, ParseException {
        String query = "UPDATE estados SET estado=?, descripcion=? "
                + " WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, estado);
        ps.setString(2, descripcion);
        ps.setInt(3, id);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DEstado.java dice: "
                    + "Ocurrio un error al modificar un estado");
            throw new SQLException();
        }      
    }
    
    
    public void eliminar(int id) throws SQLException {
        String query = "DELETE FROM estados WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DEstado.java dice: "
                    + "Ocurrio un error al eliminar un estado");
            throw new SQLException();
        }
        
    }
    public List<String[]> listar() throws SQLException {
        List<String[]> estado = new ArrayList<>();
        String query = "SELECT * FROM estados";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        
        ResultSet set = ps.executeQuery();
        while(set.next()) {
            estado.add(new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("estado"),
                set.getString("descripcion")
            });
        }
        
        return estado;
    }
   
    
    public String[] ver(int id) throws SQLException {
        String[] estado = null;
        String query = "SELECT * FROM estados WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            estado = new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("estado"),
                set.getString("descripcion")
            };
        }        
        return estado;
    }
    
    
    public int existe(int id) throws SQLException {
        int idx = -1;
        String query = "SELECT * FROM estados WHERE id=?";
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
