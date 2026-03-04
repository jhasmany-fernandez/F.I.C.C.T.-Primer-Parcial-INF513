package Datos;

import Conexion.DBConnection;
import Utils.Utiles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DEnfermedadViralMapa {

    private DBConnection conexion;

    public DEnfermedadViralMapa() {
        this.conexion = new DBConnection();
    }

    public void guardar(int enfermedad_viral_id, int mapa_id) throws SQLException, ParseException {

        String query = "INSERT INTO enfermedad_viral_mapa(enfermedad_viral_id,mapa_id, created_at, updated_at)"
                + " values(?,?,?,?)";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, enfermedad_viral_id);
        ps.setInt(2, mapa_id);
        ps.setTimestamp(3, Utiles.now());
        ps.setTimestamp(4, Utiles.now());

        if (ps.executeUpdate() == 0) {
            System.err.println("Class D_enfermedad_viral_mapa.java dice: Ocurrio un error al guardar()");
            throw new SQLException();
        }
    }

    public void eliminar(int mapa_id) throws SQLException, ParseException {

        String query = "DELETE FROM enfermedad_viral_mapa WHERE mapa_id=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, mapa_id);

        if (ps.executeUpdate() == 0) {
            System.err.println("Class D_enfermedad_viral_mapa.java dice: "
                    + "Ocurrio un error al eliminar eliminar()");
            throw new SQLException();
        }
    }

    public String[] ver(int mapaId) throws SQLException {
        ArrayList<String> enfermedadesId = new ArrayList<>();
        String query = "SELECT * FROM enfermedad_viral_mapa WHERE mapa_id=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, mapaId);

        ResultSet set = ps.executeQuery();

        while (set.next()) {
            enfermedadesId.add(String.valueOf(set.getInt("enfermedad_viral_id")));
        }

        // Convertir la lista a un arreglo
        String[] enfermedadesIdArray = enfermedadesId.toArray(new String[0]);

        return enfermedadesIdArray;
    }

    
    public static void main(String[] args) throws SQLException, ParseException {

        DEnfermedadViralMapa A = new DEnfermedadViralMapa();
        //A.guardar(1, 1);
        System.out.println(Arrays.toString(A.ver(4)));

    }

}
