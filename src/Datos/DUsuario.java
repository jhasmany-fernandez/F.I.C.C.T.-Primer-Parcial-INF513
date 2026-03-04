package Datos;

import Conexion.DBConnection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class DUsuario {

    private final DBConnection connection;

    public DUsuario() {
        connection = new DBConnection();
    }

    public int getIdByCorreo(String correo) throws SQLException {
        int id = -1;
        String query = "SELECT id FROM users WHERE email=?";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ps.setString(1, correo);

        ResultSet set = ps.executeQuery();
        if (set.next()) {
            id = set.getInt("id");
        }
        return id;
    }

    public int getRolByCorreo(String correo) throws SQLException {
        int id = -1;
        String query = "SELECT model_has_roles.role_id FROM users INNER JOIN model_has_roles ON users.id=model_has_roles.model_id WHERE users.email=? LIMIT 1";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ps.setString(1, correo);

        ResultSet set = ps.executeQuery();
        if (set.next()) {
            id = set.getInt("id");
        }
        return id;
    }

    // id, ci, name, email, ap_parerno, ap_materno, telefono, ubicacion, longitud, latitud, estado, password, created_at, update_at
    public void guardar(int ci, String name, String email, String ap_paterno, String ap_materno, String telefono,
            String ubicacion, float longitud, float latitud, String genero, Date fecha_nac, String password, int rol) throws SQLException {

        String query = "INSERT INTO users(ci, name, email, ap_paterno, ap_materno, telefono, ubicacion, longitud, latitud, genero, fecha_nac, password, created_at, updated_at) "
                + "VALUES(?, ? , ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        java.util.Date currentDate = new java.util.Date();
        Timestamp timestamp = new Timestamp(currentDate.getTime());

        PreparedStatement ps = connection.conectar().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, ci);
        ps.setString(2, name);
        ps.setString(3, email);
        ps.setString(4, ap_paterno);
        ps.setString(5, ap_materno);
        ps.setString(6, telefono);
        ps.setString(7, ubicacion);
        ps.setFloat(8, longitud);
        ps.setFloat(9, latitud);
        ps.setString(10, genero);
        ps.setDate(11, fecha_nac);
        ps.setString(12, BCrypt.hashpw(password, BCrypt.gensalt()));
        ps.setTimestamp(13, timestamp);
        ps.setTimestamp(14, timestamp);
        if (ps.executeUpdate() == 0) {
            System.out.println("Class DUsuario.java dice: "
                    + "Ocurrio un error al insertar un usuario guardar()");
            throw new SQLException();
        }
        ResultSet rs = ps.getGeneratedKeys();

        if (rs.next()) {
            System.out.println("" + rs.getInt(1));
            query = "INSERT INTO model_has_roles(role_id, model_type ,model_id) values (?, ?, ?)";
            PreparedStatement pr = connection.conectar().prepareStatement(query);
            pr.setInt(1, rol);
            pr.setString(2, "App\\Models\\User");
            pr.setInt(3, rs.getInt(1));

            if (pr.executeUpdate() == 0) {
                System.out.println("Class DUsuario.java dice: "
                        + "Ocurrio un error al modificar un usuario modificar()");
                throw new SQLException();
            }
        }
    }

    public void modificar(int id, String ci, String name, String email, String ap_paterno, String ap_materno, String telefono,
            String ubicacion, String longitud, String latitud, String genero, String fecha_nac, String password, int rol) throws SQLException, ParseException, IllegalArgumentException {

        String query = "UPDATE users SET ci=?, name=?, email=?, ap_paterno=?, "
                + "ap_materno=?, telefono=?, ubicacion=?, longitud=?, latitud=?, genero=?, fecha_nac=?, password=?, updated_at=? "
                + "WHERE id=?";

        java.util.Date currentDate = new java.util.Date();
        Timestamp timestamp = new Timestamp(currentDate.getTime());

        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ps.setInt(1, Integer.valueOf(ci));
        ps.setString(2, name);
        ps.setString(3, email);
        ps.setString(4, ap_paterno);
        ps.setString(5, ap_materno);
        ps.setString(6, telefono);
        ps.setString(7, ubicacion);
        ps.setFloat(8, Float.valueOf(longitud));
        ps.setFloat(9, Float.valueOf(latitud));
        ps.setString(10, genero);
        ps.setDate(11, Date.valueOf(fecha_nac));
        ps.setString(12, BCrypt.hashpw(password, BCrypt.gensalt()));
        ps.setTimestamp(14, timestamp);
        ps.setInt(15, id);

        if (ps.executeUpdate() == 0) {
            System.out.println("Class DUsuario.java dice: "
                    + "Ocurrio un error al modificar un usuario modificar()");
            throw new SQLException();
        }

        query = "UPDATE models_has_roles SET role_id=? WHERE model_id=?";
        PreparedStatement pr = connection.conectar().prepareStatement(query);
        pr.setInt(1, rol);
        pr.setInt(2, id);

        if (pr.executeUpdate() == 0) {
            System.out.println("Class DUsuario.java dice: "
                    + "Ocurrio un error al modificar un usuario modificar()");
            throw new SQLException();
        }
    }

    public void eliminar(int id) throws SQLException {
        String query = "UPDATE users SET estado=0 WHERE id=?";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ps.setInt(1, id);
        if (ps.executeUpdate() == 0) {
            System.out.println("Class DUsuario.java dice: "
                    + "Ocurrio un error al eliminar un usuario eliminar()");
            throw new SQLException();
        }
    }

    public void habilitar(int id) throws SQLException {
        String query = "UPDATE users SET estado=1 WHERE id=?";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ps.setInt(1, id);
        if (ps.executeUpdate() == 0) {
            System.out.println("Class DUsuario.java dice: "
                    + "Ocurrio un error al habilitar un usuario habilitar()");
            throw new SQLException();
        }
    }

    public ArrayList<String[]> listar() throws SQLException {
        ArrayList<String[]> usuarios = new ArrayList<>();
        String query = "SELECT * FROM users";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ResultSet set = ps.executeQuery();

        while (set.next()) {
            usuarios.add(new String[]{
                String.valueOf(set.getInt("id")),
                String.valueOf(set.getInt("ci")),
                set.getString("name"),
                set.getString("email"),
                set.getString("ap_paterno"),
                set.getString("ap_materno"),
                set.getString("telefono"),
                set.getString("ubicacion"),
                String.valueOf(set.getFloat("longitud")),
                String.valueOf(set.getString("latitud")),
                set.getInt("estado") == 0 ? "Inactivo" : "Activo",
                set.getString("genero") == "M" ? "Masculino" : "Femenino",
                String.valueOf(set.getDate("fecha_nac")),
                set.getString("password")
            });
        }
        return usuarios;
    }

    public String[] ver(int id) throws SQLException {
        String[] usuario = null;
        String query = "SELECT * FROM users WHERE id=?";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ps.setInt(1, id);
        ResultSet set = ps.executeQuery();
        if (set.next()) {
            usuario = new String[]{
                String.valueOf(set.getInt("id")),
                String.valueOf(set.getInt("ci")),
                set.getString("name"),
                set.getString("email"),
                set.getString("ap_paterno"),
                set.getString("ap_materno"),
                set.getString("telefono"),
                set.getString("ubicacion"),
                String.valueOf(set.getFloat("longitud")),
                String.valueOf(set.getString("latitud")),
                set.getInt("estado") == 0 ? "Inactivo" : "Activo",
                "M".equals(set.getString("genero")) ? "Masculino" : "Femenino",
                String.valueOf(set.getDate("fecha_nac")),
                set.getString("password")
            };
        }
        return usuario;
    }
    
    public int getEstado(String correo) throws SQLException {
        int estado = -1;
        String query = "SELECT estado FROM users WHERE email=?";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ps.setString(1, correo);

        ResultSet set = ps.executeQuery();
        if (set.next()) {
            estado = set.getInt("estado");
        }
        return estado;
    }

    public void desconectar() {
        if (connection != null) {
            connection.desconectar();
        }
    }
}
