package Metodos;

import Negocio.NCasosBrigadas;
import Negocio.NMapas;
import Utils.Email;
import Utils.Handler;
import Utils.HtmlBuilder;
import Utils.Utiles;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.util.PSQLException;
import proyectoemail.MailAplication;

public class Mapa {

    private NMapas nmapa;
    private String[] encabezado = {"Id","Nombre","Detalle", "Latitud", "Longitud", "Enfermedades"};

    public Mapa() {
        this.nmapa = new NMapas();
    }

    public void listar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            ArrayList<String[]> mapas = nmapa.listar();
            mensaje = "Mapa Listar Correctamente";
            //}
            Email emailObject = new Email(correo, "GRUPO 13 SC", HtmlBuilder.generateTabla(encabezado,mapas));
            Email.sendEmail(emailObject);
            
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (PSQLException e) {
            Handler.handleError(Handler.SQLERROR, correo, null,e.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(CasoBrigada.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void ver(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            String[] mapa = nmapa.ver(parametros);
            mensaje = "Mapa Ver Correctamente";
            //}
            ArrayList<String[]> cuerpo = new ArrayList<>();
            cuerpo.add(mapa);
            //Genera el Mapa en archivo
            //Email emailObject = new Email(correo, "GRUPO 13 SC", HtmlBuilder.generateMapa());
            Email emailObject = new Email(correo, "GRUPO 13 SC", HtmlBuilder.generateTabla(encabezado,cuerpo));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
            System.out.println(Arrays.toString(mapa));
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (PSQLException e) {
            Handler.handleError(Handler.SQLERROR, correo, null,e.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(CasoBrigada.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void eliminar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            nmapa.eliminar(parametros);
            mensaje = "Mapa Eliminado Correctamente";
            //}
            Email emailObject = new Email(correo, "GRUPO 13 SC", HtmlBuilder.generateSuccess("Muy Bien", mensaje));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (PSQLException e) {
            Handler.handleError(Handler.SQLERROR, correo, null,e.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(CasoBrigada.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void modificar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            nmapa.modificar(parametros);
            mensaje = "Mapa Modificado Correctamente";
            //}
            Email emailObject = new Email(correo, "GRUPO 13 SC", HtmlBuilder.generateSuccess("Muy Bien", mensaje));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (PSQLException e) {
            Handler.handleError(Handler.SQLERROR, correo, null,e.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(CasoBrigada.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void agregar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            nmapa.guardar(parametros);
            mensaje = "Mapa Guardado Correctamente";
            //}
            Email emailObject = new Email(correo, "GRUPO 13 SC", HtmlBuilder.generateSuccess("Muy Bien", mensaje));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (PSQLException e) {
            Handler.handleError(Handler.SQLERROR, correo, null,e.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(CasoBrigada.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
