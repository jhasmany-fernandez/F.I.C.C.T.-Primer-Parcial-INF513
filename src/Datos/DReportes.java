/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

import Conexion.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pedri
 */
public class DReportes {

    private final DBConnection connection;

    public DReportes() {
        connection = new DBConnection();
    }

    public Map<String, Integer> verPacientesGenero() throws SQLException {
        Map<String, Integer> response = new HashMap<>();
        response.put("M", 0);
        response.put("F", 0);
        String query = "SELECT genero, COUNT(*) as cant "
                + "FROM users INNER JOIN model_has_roles ON model_has_roles.model_id=users.id "
                + "WHERE model_has_roles.role_id=4 "
                + "GROUP BY genero";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            response.put(rs.getString("genero"), rs.getInt("cant"));
        }
        return response;
    }

    public Map<String, Integer> numPacienteEnfermedad() throws SQLException {
        Map<String, Integer> response = new HashMap<>();

        String query = "SELECT ev.nombre, COUNT(*) as cant "
                + "FROM enfermedad_virals as ev INNER JOIN estadia_enfermedads as ee ON ee.enfermedad_id=ev.id "
                + "WHERE ee.estado_id=6 "
                + "GROUP BY ev.nombre";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            response.put(rs.getString("nombre"), rs.getInt("cant"));
        }
        return response;
    }

    public Map<String, Integer> maxCasosFecha() throws SQLException {
        Map<String, Integer> response = new HashMap<>();
        String query = "SELECT DATE(ee.fecha_ini) as fecha, COUNT(*) as cant "
                + "FROM enfermedad_virals as ev INNER JOIN estadia_enfermedads as ee ON ee.enfermedad_id=ev.id "
                + "WHERE ee.estado_id=6 "
                + "GROUP BY DATE(ee.fecha_ini) "
                + "ORDER BY COUNT(*) DESC LIMIT 1";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            response.put(rs.getString("fecha"), rs.getInt("cant"));
        }
        return response;
    }
    
    public Map<String, Integer> casosPorMes() throws SQLException {
        Map<String, Integer> response = new HashMap<>();
        String query = "SELECT EXTRACT('year' FROM ee.fecha_ini) as anio, EXTRACT('month' FROM ee.fecha_ini) as mes, COUNT(*) as cant "
                + "FROM enfermedad_virals as ev INNER JOIN estadia_enfermedads as ee ON ee.enfermedad_id=ev.id "
                + "WHERE ee.estado_id=6 "
                + "GROUP BY EXTRACT('year' FROM ee.fecha_ini), EXTRACT('month' FROM ee.fecha_ini) "
                + "ORDER BY EXTRACT('year' FROM ee.fecha_ini), EXTRACT('month' FROM ee.fecha_ini) ASC";
        PreparedStatement ps = connection.conectar().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            response.put(rs.getString("anio") + rs.getString("mes"), rs.getInt("cant"));
        }
        return response;
    }

}
