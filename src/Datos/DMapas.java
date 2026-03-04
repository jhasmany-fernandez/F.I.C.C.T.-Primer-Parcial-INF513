package Datos;

import Conexion.DBConnection;
import Utils.Utiles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class DMapas {

    private DBConnection conexion;

    public DMapas() {
        this.conexion = new DBConnection();
    }

    public long guardar(String name, String detalle, Double latitud, Double longitud) throws SQLException, ParseException {

        String query = "INSERT INTO mapas(name, detalle, latitud, longitud, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conexion.conectar().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, name);
        ps.setString(2, detalle);
        ps.setDouble(3, latitud);
        ps.setDouble(4, longitud);
        ps.setTimestamp(5, Utiles.now());
        ps.setTimestamp(6, Utiles.now());

        int rowsAffected = ps.executeUpdate();

        if (rowsAffected == 0) {
            System.err.println("Class D_Mapas.java dice: Ocurrio un error al insertar un usuario guardar()");
            throw new SQLException();
        }

        long idGenerado = 0;
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            idGenerado = generatedKeys.getLong(1);
            System.out.println("ID generado: " + idGenerado);
        } else {
            System.err.println("No se pudo obtener el ID generado");
        }
        desconectar();
        
        return idGenerado;
    }

    public void modificar(int mapaId, String name, String detalle, Double latitud, Double longitud) throws SQLException, ParseException {

        String query = "UPDATE mapas SET name=?, detalle=?, latitud=?, longitud=?, updated_at=? "
                + " WHERE id=?";

        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, detalle);
        ps.setDouble(3, latitud);
        ps.setDouble(4, longitud);
        ps.setTimestamp(5, Utiles.now());
        ps.setInt(6, mapaId);

        if (ps.executeUpdate() == 0) {
            System.err.println("Class D_mapas.java dice: Ocurrio un error al modificar()");
            throw new SQLException();
        }
        
        desconectar();
    }

    public void eliminar(int mapaId) throws SQLException {
        String query = "DELETE FROM mapas WHERE id=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, mapaId);
        if (ps.executeUpdate() == 0) {
            System.err.println("Class D_mapas.java dice: Ocurrio un error eliminar()");
            throw new SQLException();
        }
        desconectar();
        
    }

    public String[] ver(int mapaId) throws SQLException {
        String[] mapa = null;
        String query = "SELECT * FROM mapas WHERE id=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, mapaId);

        ResultSet set = ps.executeQuery();
        if (set.next()) {
            mapa = new String[]{
                String.valueOf(set.getInt("id")),
                set.getString("name"),
                set.getString("detalle"),
                String.valueOf(set.getDouble("latitud")),
                String.valueOf(set.getDouble("longitud"))
            };
        }
        desconectar();
        
        return mapa;
    }

    
     public ArrayList<String> listar() throws SQLException {
        ArrayList<String> mapas = new ArrayList<>();
        String query = "SELECT id FROM mapas ORDER BY id ASC";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ResultSet set = ps.executeQuery();
        while(set.next()) {
            mapas.add( String.valueOf(set.getInt("id"))/*,
                set.getString("name"),
                set.getString("detalle"),
                String.valueOf(set.getDouble("latitud")),
                String.valueOf(set.getDouble("longitud"))
            }*/);
        }
        desconectar();
        
        return mapas;
    }
     
     
    public boolean existeID(int id) throws SQLException {
        int idr = -1;
        String query = "SELECT * FROM mapas WHERE id=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, id);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            idr = set.getInt("id");
        } 
        return idr != -1;
    }
     
    public void desconectar() {
        if (conexion != null) {
            conexion.desconectar();
        }
    }

    
    
    
    
    public static void main(String[] args) throws SQLException {
        DMapas A = new DMapas();
     
        //A.modificar(1, "mapa1X", "detalle1", 12.54, 13.345);
        /*ArrayList<String[]> mapas = A.listar();
        for (String[] mapa : mapas) {
            System.out.println(Arrays.toString(mapa));
        }*/
        

    }
    
}
