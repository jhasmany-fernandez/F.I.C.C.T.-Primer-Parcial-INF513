
package Datos;

import Conexion.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class DPuntoAtencion {
    private DBConnection conn;
    
    public DPuntoAtencion() {
        conn = new DBConnection();
    }
    
    public void guardar(String nombre, String ubicacion,double longitud, double latitud, 
            int num_camillas, int num_cuartos, int id_tipo_punto) throws SQLException{
        
        String query = "INSERT INTO punto_atencions(nombre,ubicacion,longitud,latitud,num_camillas,num_cuartos,id_tipo_punto)"
                + " values(?,?,?,?,?,?,?)";
        PreparedStatement ps = conn.conectar().prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, nombre);
        ps.setString(2, ubicacion);
        ps.setDouble(3, longitud);
        ps.setDouble(4, latitud);
        ps.setInt(5, num_camillas);
        ps.setInt(6, num_cuartos);
        ps.setInt(7, id_tipo_punto);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DPuntoAtencion.java dice: "
                    + "Ocurrio un error al insertar un tipo punto de atencion guardar()");
            throw new SQLException();
        }
    }

    public void modificar(int id, String nombre, String ubicacion,double longitud, double latitud, 
            int num_camillas, int num_cuartos, int id_tipo_punto) throws SQLException, ParseException {
        
        String query = "UPDATE punto_atencions SET nombre=?, ubicacion=?, longitud=?, latitud=?, num_camillas=?, num_cuartos=?, id_tipo_punto=? "
                + " WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, nombre);
        ps.setString(2, ubicacion);
        ps.setDouble(3, longitud);
        ps.setDouble(4, latitud);
        ps.setInt(5, num_camillas);
        ps.setInt(6, num_cuartos);
        ps.setInt(7, id_tipo_punto);
        ps.setInt(8, id);
        
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DPuntoAtencion.java dice: "
                    + "Ocurrio un error al modificar un tipo punto de atencion modificar()");
            throw new SQLException();
        }      
    }
    
    
    public void eliminar(int id) throws SQLException {
        String query = "DELETE FROM punto_atencions WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
        if(ps.executeUpdate() == 0) {
            System.err.println("Class DPuntoAtencion.java dice: "
                    + "Ocurrio un error al eliminar un usuario eliminar()");
            throw new SQLException();
        }
        
    }
    public List<String[]> listar() throws SQLException {
        List<String[]> puntoAtencion = new ArrayList<>();
        String query = "SELECT * FROM punto_atencions";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        
        ResultSet set = ps.executeQuery();
        while(set.next()) {
            puntoAtencion.add(new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("nombre"),
                set.getString("ubicacion"),
                String.valueOf(set.getDouble("longitud")),
                String.valueOf(set.getDouble("latitud")),
                String.valueOf(set.getInt("num_camillas")),
                String.valueOf(set.getInt("num_cuartos")),
                String.valueOf(set.getInt("id_tipo_punto"))
            });
        }
        
        return puntoAtencion;
    }
   
    public int existe(int id) throws SQLException {
        int idx = -1;
        String query = "SELECT * FROM punto_atencions WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            idx = set.getInt("id");
        } 
        return idx;
    }
    
    public String[] ver(int id) throws SQLException {
        String[] puntoAtencion = null;
        String query = "SELECT * FROM punto_atencions WHERE id=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setInt(1, id);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            puntoAtencion = new String[] {
                String.valueOf(set.getInt("id")),
                set.getString("nombre"),
                set.getString("ubicacion"),
                String.valueOf(set.getDouble("longitud")),
                String.valueOf(set.getDouble("latitud")),
                String.valueOf(set.getInt("num_camillas")),
                String.valueOf(set.getInt("num_cuartos")),
                String.valueOf(set.getInt("id_tipo_punto"))
            };
        }        
        return puntoAtencion;
    }
    
    
    public int getIdByNombre(String nombre) throws SQLException {
        int id = -1;
        String query = "SELECT * FROM punto_atencions WHERE nombre=?";
        PreparedStatement ps = conn.conectar().prepareStatement(query);
        ps.setString(1, nombre);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            id = set.getInt("id");
        } 
        return id;
    }
    
    
    public void desconectar(){
        if(conn != null) {
           conn.desconectar();
        }
    }
}
