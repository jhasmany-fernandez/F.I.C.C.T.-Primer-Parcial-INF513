package Datos;

import Conexion.DBConnection;
import Utils.Utiles;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class DEstadiaEnfermedad {

    private DBConnection conexion;

    public DEstadiaEnfermedad() {
        this.conexion = new DBConnection();
    }

    public int guardar(Date fecha_ini, Date fecha_fin, String detalle, int user_id, int estado_id, int enfermedad_id, int estadia_enfermedable_id, String estadia_enfermedable_type) throws SQLException, ParseException {

        String query = "INSERT INTO estadia_enfermedads(fecha_ini, fecha_fin, detalle, user_id, estado_id, enfermedad_id, estadia_enfermedable_id, estadia_enfermedable_type, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conexion.conectar().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        
        ps.setDate(1, fecha_ini);
        ps.setDate(2, fecha_fin);
        ps.setString(3, detalle);
        ps.setInt(4, user_id);
        ps.setInt(5, estado_id);
        ps.setInt(6, enfermedad_id);
        ps.setInt(7, estadia_enfermedable_id);
        ps.setString(8, estadia_enfermedable_type);
        ps.setTimestamp(9, Utiles.now());
        ps.setTimestamp(10, Utiles.now());

        int rowsAffected = ps.executeUpdate();

        if (rowsAffected == 0) {
            System.err.println("Class D_estadia_enfermedad.java dice: Ocurrio un error al guardar()");
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

        return (int) idGenerado;
    }
    

    

    public void modificar(int estadiaId, Date fecha_ini, Date fecha_fin, String detalle, int user_id, int estado_id, int enfermedad_id, int estadia_enfermedable_id, String estadia_enfermedable_type) throws SQLException, ParseException {

        String query = "UPDATE estadia_enfermedads SET fecha_ini=?, fecha_fin=?, detalle=?, user_id=?, estado_id=?, enfermedad_id=?, estadia_enfermedable_id=?, estadia_enfermedable_type=?, updated_at=? "
                + " WHERE id=?";

        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setDate(1, fecha_ini);
        ps.setDate(2, fecha_fin);
        ps.setString(3, detalle);
        ps.setInt(4, user_id);
        ps.setInt(5, estado_id);
        ps.setInt(6, enfermedad_id);
        ps.setInt(7, estadia_enfermedable_id);
        ps.setString(8, estadia_enfermedable_type);
        ps.setTimestamp(9, Utiles.now());
        ps.setInt(10, estadiaId);

        if (ps.executeUpdate() == 0) {
            System.err.println("Class D_estadia_enfermedad.java dice: Ocurrio un error al modificar()");
            throw new SQLException();
        }

        desconectar();
    }

    
    public String[] ver(int estadiaId, String tipo) throws SQLException {
        String[] estadia = null;
        String query = "SELECT * FROM estadia_enfermedads WHERE id=? AND estadia_enfermedable_type=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, estadiaId);
        ps.setString(2, tipo);

        ResultSet set = ps.executeQuery();
        if (set.next()) {
            //validar que ninguncampo sea null
            estadia = new String[]{
                String.valueOf(set.getInt("id")),
                Utiles.DateSQLToString(set.getDate("fecha_ini")),
                Utiles.DateSQLToString(set.getDate("fecha_fin")),
                set.getString("detalle"),
                String.valueOf(set.getInt("user_id")),
                String.valueOf(set.getInt("estado_id")),
                String.valueOf(set.getInt("enfermedad_id")),
                String.valueOf(set.getInt("estadia_enfermedable_id")),
                set.getString("estadia_enfermedable_type"),
                Utiles.TimestampToString(set.getTimestamp("created_at")),
                Utiles.TimestampToString(set.getTimestamp("updated_at")),
            };
        }
        desconectar();
        
        return estadia;
    }
    
    
    public ArrayList<String> listarRegistradasPorBrigadas() throws SQLException {
        ArrayList<String> estadiasID = new ArrayList<>();
        String query = "SELECT id FROM estadia_enfermedads WHERE estadia_enfermedable_type=? ORDER BY id ASC";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setString(1, "App\\Models\\brigada");
        ResultSet set = ps.executeQuery();
        while(set.next()) {
            estadiasID.add(
                String.valueOf(set.getInt("id"))/*
                Utiles.DateSQLToString(set.getDate("fecha_ini")),
                Utiles.DateSQLToString(set.getDate("fecha_fin")),
                set.getString("detalle"),
                String.valueOf(set.getInt("user_id")),
                String.valueOf(set.getInt("estado_id")),
                String.valueOf(set.getInt("enfermedad_id")),
                String.valueOf(set.getInt("estadia_enfermedable_id")),
                set.getString("estadia_enfermedable_type"),
                Utiles.TimestampToString(set.getTimestamp("created_at")),
                Utiles.TimestampToString(set.getTimestamp("updated_at")),*/
            );
        }
        desconectar();
        
        return estadiasID;
    }
    
    public ArrayList<String> listarRegistradasPorHospitales() throws SQLException {
        ArrayList<String> estadias = new ArrayList<>();
        String query = "SELECT * FROM estadia_enfermedads WHERE estadia_enfermedable_type=? ORDER BY id ASC";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setString(1, "App\\Models\\punto_atencion");
        ResultSet set = ps.executeQuery();
        while(set.next()) {
            estadias.add( String.valueOf(set.getInt("id")) );
        }
        desconectar();
        
        return estadias;
    }
    
    
    public void eliminar(int estadiaId) throws SQLException {
        String query = "DELETE FROM estadia_enfermedads WHERE id=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, estadiaId);
        if (ps.executeUpdate() == 0) {
            System.err.println("Class D_estadia_enfermedad.java dice: Ocurrio un error eliminar()");
            throw new SQLException();
        }
        desconectar();

    }
    
    public int getIdByCorreo(String correo) throws SQLException {
        int id = -1;
        String query = "SELECT * FROM users WHERE email=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setString(1, correo);
                
        ResultSet set = ps.executeQuery();
        if(set.next()) {
            id = set.getInt("id");
        } 
        return id;
    }
    
    public boolean existeIDTipo(int id, String tipo) throws SQLException {
        int idr = -1;
        String query = "SELECT * FROM estadia_enfermedads WHERE id=? AND estadia_enfermedable_type=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, id);
        ps.setString(2, tipo);
                
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

    public static void main(String[] args) throws ParseException, SQLException {
        DEstadiaEnfermedad A = new DEstadiaEnfermedad();
        //(,,detalle,user_id,estado_id,enfermedad_id,estadia_enfermedable_id,estadia_enfermedable_type)

        //A.guardar(Utiles.StringToDateSQL("2023-07-02"), Utiles.StringToDateSQL("2023-12-02"), "detalle", 1, 6, 4, 2, "App\\Models\\brigada");
        //A.modificar(2,Utiles.StringToDateSQL("2023-07-02"), Utiles.StringToDateSQL("2023-12-02"), "detalleX", 1, 6, 4, 2, "App\\Models\\punto_atencion");
        //System.out.println(Arrays.toString(A.ver(2)));
        
        //A.eliminar(1);
        
        //ArrayList<String[]> estadias = A.listarRegistradasPorBrigadas();
        /*ArrayList<String[]> estadias = A.listarRegistradasPorHospitales();
        for (String[] estadia : estadias) {
            System.out.println(Arrays.toString(estadia));
        }*/

        
    }

}
