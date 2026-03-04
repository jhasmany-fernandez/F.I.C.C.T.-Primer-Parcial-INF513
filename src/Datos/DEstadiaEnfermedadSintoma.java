package Datos;

import Conexion.DBConnection;
import Utils.Utiles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class DEstadiaEnfermedadSintoma {

    private DBConnection conexion;

    public DEstadiaEnfermedadSintoma() {
        this.conexion = new DBConnection();
    }

    public void guardar(int estadia_id, int sintoma_id) throws SQLException, ParseException {

        String query = "INSERT INTO estadia_enfermedad_sintoma(estadia_enfermedad_id,sintoma_id, created_at, updated_at)"
                + " values(?,?,?,?)";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, estadia_id);
        ps.setInt(2, sintoma_id);
        ps.setTimestamp(3, Utiles.now());
        ps.setTimestamp(4, Utiles.now());

        if (ps.executeUpdate() == 0) {
            System.err.println("Class DEstadia_Enfermedad_Sintoma.java dice: Ocurrio un error al guardar()");
            throw new SQLException();
        }
    }

    public void eliminar(int estadia_id) throws SQLException, ParseException {

        String query = "DELETE FROM estadia_enfermedad_sintoma WHERE estadia_enfermedad_id=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, estadia_id);

        if (ps.executeUpdate() == 0) {
            System.err.println("Class DEstadia_Enfermedad_Sintoma.java dice: "
                    + "Ocurrio un error al eliminar eliminar()");
            throw new SQLException();
        }
    }

    public String[] ver(int estadia_id) throws SQLException {
        ArrayList<String> SintomasId = new ArrayList<>();
        String query = "SELECT * FROM estadia_enfermedad_sintoma WHERE estadia_enfermedad_id=?";
        PreparedStatement ps = conexion.conectar().prepareStatement(query);
        ps.setInt(1, estadia_id);

        ResultSet set = ps.executeQuery();

        while (set.next()) {
            SintomasId.add(String.valueOf(set.getInt("sintoma_id")));
        }

        // Convertir la lista a un arreglo
        String[] SintomasIdArray = SintomasId.toArray(new String[0]);

        return SintomasIdArray;
    }

    
    public static void main(String[] args) throws SQLException, ParseException {

        DEnfermedadViralMapa A = new DEnfermedadViralMapa();
        //A.guardar(1, 1);
        System.out.println(Arrays.toString(A.ver(4)));

    }

}

