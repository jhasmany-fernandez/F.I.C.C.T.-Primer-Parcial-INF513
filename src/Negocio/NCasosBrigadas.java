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

public class NCasosBrigadas {

    private DEstadiaEnfermedad dEstadiaEnfermedad;
    private DEstadiaEnfermedadSintoma dEstadiaEnfermedadSintoma;

    public NCasosBrigadas() {
        this.dEstadiaEnfermedad = new DEstadiaEnfermedad();
        this.dEstadiaEnfermedadSintoma = new DEstadiaEnfermedadSintoma();
    }

    public boolean permiso(String correo) throws SQLException {
        int usuarioId = dEstadiaEnfermedad.getIdByCorreo(correo);
        return usuarioId != -1;
    }

    public void guardar(ArrayList<String> parametros) throws SQLException, ParseException {

        
        Date fecha_ini = Utiles.StringToDate(parametros.get(0));
        Date fecha_fin = Utiles.StringToDate(parametros.get(1));
        String detalle = parametros.get(2);
        int user_id = Integer.parseInt(parametros.get(3));
        int estado_id = Integer.parseInt(parametros.get(4));
        int enfermedad_id = Integer.parseInt(parametros.get(5));
        int estadia_enfermedable_id = Integer.parseInt(parametros.get(6));
        String estadia_enfermedable_type = "App\\Models\\brigada";

        int estadia_id = dEstadiaEnfermedad.guardar(fecha_ini, fecha_fin, detalle, user_id, estado_id, enfermedad_id, estadia_enfermedable_id, estadia_enfermedable_type);

        ArrayList<String> SintomasId = new ArrayList<>(parametros.subList(7, parametros.size()));
        int sintoma_id;
        for (int i = 0; i < SintomasId.size(); i++) {
            sintoma_id = Integer.parseInt(SintomasId.get(i));
            dEstadiaEnfermedadSintoma.guardar(estadia_id, sintoma_id);
        }

        System.out.println("NEGOCIO: CASO GUARDADO");

    }

    public void modificar(ArrayList<String> parametros) throws SQLException, ParseException {

        int id = Integer.parseInt(parametros.get(0));

        if (!dEstadiaEnfermedad.existeIDTipo(id,"App\\Models\\brigada")) {
            throw new RuntimeException("id no existente tipo Brigada");
        }

        Date fecha_ini = Utiles.StringToDate(parametros.get(1));
        Date fecha_fin = Utiles.StringToDate(parametros.get(2));
        String detalle = parametros.get(3);
        int user_id = Integer.parseInt(parametros.get(4));
        int estado_id = Integer.parseInt(parametros.get(5));
        int enfermedad_id = Integer.parseInt(parametros.get(6));
        int estadia_enfermedable_id = Integer.parseInt(parametros.get(7));
        String estadia_enfermedable_type = "App\\Models\\brigada";

        dEstadiaEnfermedad.modificar(id, fecha_ini, fecha_fin, detalle, user_id, estado_id, enfermedad_id, estadia_enfermedable_id, estadia_enfermedable_type);
        
        dEstadiaEnfermedadSintoma.eliminar(id);
        
        ArrayList<String> SintomasId = new ArrayList<>(parametros.subList(8, parametros.size()));
        int sintoma_id;
        for (int i = 0; i < SintomasId.size(); i++) {
            sintoma_id = Integer.parseInt(SintomasId.get(i));
            dEstadiaEnfermedadSintoma.guardar(id, sintoma_id);
        }
        
        System.out.println("NEGOCIO: CASO BRIGADA MODIFICADO");
    }

    public void eliminar(ArrayList<String> parametros) throws SQLException, ParseException {

        if (parametros.size() > 1) {
            throw new IndexOutOfBoundsException("Muchos Parametros");
        }

        int estadiaId = Integer.parseInt(parametros.get(0));
        if (!dEstadiaEnfermedad.existeIDTipo(estadiaId,"App\\Models\\brigada")) {
            throw new RuntimeException("id no existente tipo brigada");
        }
        
        dEstadiaEnfermedadSintoma.eliminar(estadiaId);
        dEstadiaEnfermedad.eliminar(estadiaId);
        
        System.out.println("NEGOCIO: CASO BRIGADA ELIMINADO");
    }

    public String[] ver(ArrayList<String> parametros) throws SQLException, ParseException {

        if (parametros.size() > 1) {
            throw new IndexOutOfBoundsException("Muchos Parametros");
        }

        int estadia_id = Integer.parseInt(parametros.get(0));

        if (!dEstadiaEnfermedad.existeIDTipo(estadia_id,"App\\Models\\brigada")) {
            throw new RuntimeException("id no existente");
        }

        String[] estadia = dEstadiaEnfermedad.ver(estadia_id,"App\\Models\\brigada");
        String[] sintomas = dEstadiaEnfermedadSintoma.ver(estadia_id);

        String sintos = "";
        for (String sintoma : sintomas) {
            sintos += sintoma+", "; 
        }
        
        String[] newEstadia = new String[estadia.length+1];
        System.arraycopy(estadia, 0, newEstadia, 0, estadia.length);
        newEstadia[estadia.length] = sintos;

        System.out.println("NEGOCIO: CASO BRIGADA VER");
        return newEstadia;
    }
    
    private String[] verList(String id) throws SQLException, ParseException {
        
        int estadia_id = Integer.parseInt(id);

        String[] estadia = dEstadiaEnfermedad.ver(estadia_id,"App\\Models\\brigada");
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
    

    public ArrayList<String[]> listar() throws SQLException, ParseException {
        ArrayList<String> casosId = new ArrayList<>();
        casosId = dEstadiaEnfermedad.listarRegistradasPorBrigadas();
        ArrayList<String[]> listado = new ArrayList<>();
        for (String casoId : casosId) {
            listado.add(verList(casoId));
        }
        return listado;
    }

    public static void main(String[] args) throws SQLException, ParseException {
        NCasosBrigadas brigada = new NCasosBrigadas();

        ArrayList<String> parametros = new ArrayList<>(Arrays.asList("2023-11-02", "2023-12-02", "detalle SI", "1", "6", "4", "2","1","2"));
        //brigada.guardar(parametros);
        ArrayList<String> parametros2 = new ArrayList<>(Arrays.asList("1"));
       
        //System.out.println(Arrays.toString(brigada.ver(parametros2)));
        //System.out.println(brigada.listar());
        ArrayList<String[]> casos = brigada.listar();
        for (int i = 0; i < casos.size(); i++) {
            System.out.println("Caso[" + i + "]: " + Arrays.toString(casos.get(i)));
        }

    }

}
