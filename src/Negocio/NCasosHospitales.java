package Negocio;

import Datos.DEnfermedadViralMapa;
import Datos.DEstadiaEnfermedad;
import Datos.DEstadiaEnfermedadSintoma;
import Datos.DMapas;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import Utils.Utiles;
import Utils.Validator;
import org.postgresql.util.PSQLException;

public class NCasosHospitales {

    private DEstadiaEnfermedad dEstadiaEnfermedad;
    private DEstadiaEnfermedadSintoma dEstadiaEnfermedadSintoma;

    public NCasosHospitales() {
        this.dEstadiaEnfermedad = new DEstadiaEnfermedad();
        this.dEstadiaEnfermedadSintoma = new DEstadiaEnfermedadSintoma();
    }

    public boolean permiso(String correo) throws SQLException {
        int usuarioId = dEstadiaEnfermedad.getIdByCorreo(correo);
        return usuarioId != -1;
    }

    public void guardar(ArrayList<String> parametros) throws SQLException, ParseException, PSQLException {

        Date fecha_ini = Utiles.StringToDate(parametros.get(0));
        Date fecha_fin = Utiles.StringToDate(parametros.get(1));
        String detalle = parametros.get(2);
        int user_id = Integer.parseInt(parametros.get(3));
        int estado_id = Integer.parseInt(parametros.get(4));
        int enfermedad_id = Integer.parseInt(parametros.get(5));
        int estadia_enfermedable_id = Integer.parseInt(parametros.get(6));
        String estadia_enfermedable_type = "App\\Models\\punto_atencion";

        int estadia_id = dEstadiaEnfermedad.guardar(fecha_ini, fecha_fin, detalle, user_id, estado_id, enfermedad_id, estadia_enfermedable_id, estadia_enfermedable_type);

        ArrayList<String> SintomasId = new ArrayList<>(parametros.subList(7, parametros.size()));
        int sintoma_id;
        for (int i = 0; i < SintomasId.size(); i++) {
            sintoma_id = Integer.parseInt(SintomasId.get(i));
            dEstadiaEnfermedadSintoma.guardar(estadia_id, sintoma_id);
        }

        System.out.println("NEGOCIO: CASO HOSPITAL GUARDADO");
    }

    public void modificar(ArrayList<String> parametros) throws SQLException, ParseException, PSQLException {

        int id = Integer.parseInt(parametros.get(0));

        if (!dEstadiaEnfermedad.existeIDTipo(id, "App\\Models\\punto_atencion")) {
            throw new RuntimeException("id no existente");
        }

        Date fecha_ini = Utiles.StringToDate(parametros.get(1));
        Date fecha_fin = Utiles.StringToDate(parametros.get(2));
        String detalle = parametros.get(3);
        int user_id = Integer.parseInt(parametros.get(4));
        int estado_id = Integer.parseInt(parametros.get(5));
        int enfermedad_id = Integer.parseInt(parametros.get(6));
        int estadia_enfermedable_id = Integer.parseInt(parametros.get(7));
        String estadia_enfermedable_type = "App\\Models\\punto_atencion";

        dEstadiaEnfermedad.modificar(id, fecha_ini, fecha_fin, detalle, user_id, estado_id, enfermedad_id, estadia_enfermedable_id, estadia_enfermedable_type);

        dEstadiaEnfermedadSintoma.eliminar(id);

        ArrayList<String> SintomasId = new ArrayList<>(parametros.subList(8, parametros.size()));
        int sintoma_id;
        for (int i = 0; i < SintomasId.size(); i++) {
            sintoma_id = Integer.parseInt(SintomasId.get(i));
            dEstadiaEnfermedadSintoma.guardar(id, sintoma_id);
        }

        System.out.println("NEGOCIO: CASO HOSPITAL MODIFICADO");
    }

    public void eliminar(ArrayList<String> parametros) throws SQLException, ParseException, PSQLException {

        if (parametros.size() > 1) {
            throw new IndexOutOfBoundsException("Muchos Parametros");
        }

        int estadiaId = Integer.parseInt(parametros.get(0));
        if (!dEstadiaEnfermedad.existeIDTipo(estadiaId, "App\\Models\\punto_atencion")) {
            throw new RuntimeException("id no existente");
        }

        dEstadiaEnfermedadSintoma.eliminar(estadiaId);
        dEstadiaEnfermedad.eliminar(estadiaId);

        System.out.println("NEGOCIO: CASO HOSPITAL ELIMINADO");
    }

    public String[] ver(ArrayList<String> parametros) throws SQLException, ParseException, PSQLException {

        if (parametros.size() > 1) {
            throw new IndexOutOfBoundsException("Muchos Parametros");
        }

        int estadia_id = Integer.parseInt(parametros.get(0));

        if (!dEstadiaEnfermedad.existeIDTipo(estadia_id, "App\\Models\\punto_atencion")) {
            throw new RuntimeException("id no existente");
        }

        String[] estadia = dEstadiaEnfermedad.ver(estadia_id, "App\\Models\\punto_atencion");
        String[] sintomas = dEstadiaEnfermedadSintoma.ver(estadia_id);

        String sintos = "";
        for (String sintoma : sintomas) {
            sintos += sintoma + ", ";
        }

        String[] newEstadia = new String[estadia.length + 1];
        System.arraycopy(estadia, 0, newEstadia, 0, estadia.length);
        newEstadia[estadia.length] = sintos;

        System.out.println("NEGOCIO: CASO HOSPITAL VER");
        return newEstadia;
    }
    
    private String[] verList(String id) throws SQLException, ParseException, PSQLException {
        
        int estadia_id = Integer.parseInt(id);

        String[] estadia = dEstadiaEnfermedad.ver(estadia_id,"App\\Models\\punto_atencion");
        String[] sintomas = dEstadiaEnfermedadSintoma.ver(estadia_id);

        String sintos = "";
        for (String sintoma : sintomas) {
            sintos += sintoma+", "; 
        }
        
        String[] newEstadia = new String[estadia.length+1];
        System.arraycopy(estadia, 0, newEstadia, 0, estadia.length);
        newEstadia[estadia.length] = sintos;

        return newEstadia;
    }

    public ArrayList<String[]> listar() throws SQLException, ParseException, PSQLException {
        ArrayList<String> casosId = new ArrayList<>();
        casosId = dEstadiaEnfermedad.listarRegistradasPorHospitales();
        ArrayList<String[]> listado = new ArrayList<>();
        for (String casoId : casosId) {
            listado.add(verList(casoId));
        }
        return listado;
    }

    public static void main(String[] args) throws SQLException, ParseException {
        NCasosHospitales hospital = new NCasosHospitales();

        ArrayList<String> parametros = new ArrayList<>(Arrays.asList("2023-11-02", "2023-12-02", "detalle Hospital", "1", "6", "4", "2"));
        hospital.guardar(parametros);
        //System.out.println(parametros);
        ArrayList<String[]> casos = hospital.listar();
        for (int i = 0; i < casos.size(); i++) {
            System.out.println("Caso[" + i + "]: " + Arrays.toString(casos.get(i)));
        }

    }

}
