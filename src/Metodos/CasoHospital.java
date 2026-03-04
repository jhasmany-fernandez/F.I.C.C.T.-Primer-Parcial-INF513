package Metodos;

import Negocio.NCasosHospitales;
import Utils.Email;
import Utils.Handler;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.util.PSQLException;
import proyectoemail.MailAplication;


public class CasoHospital {
    
    private NCasosHospitales ncasohospital;
    private String[] encabezado = {"Id", "fecha Inicio", "Fecha Final", "Detalle", "User_id", "Estado_id", "Enfermedad_id","Estadia_enfermedable_id", "Estadia_enfermedable_type", "created_at", "updated_at", "Sintomas"};
    

    public CasoHospital() {
        this.ncasohospital = new NCasosHospitales();
    }
    
    
    public void listar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            ArrayList<String[]> casos = ncasohospital.listar();
            mensaje = "Caso Brigada Listar Correctamente";
            //}
            Email emailObject = new Email(correo, Email.SUBJECT, HtmlBuilder.generateTabla(encabezado,casos));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
            for (int i = 0; i < casos.size(); i++) {
                System.out.println("Caso[" + i + "]: " + Arrays.toString(casos.get(i)));
            }
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
            String[] caso = ncasohospital.ver(parametros);
            mensaje = "Caso Brigada Ver Correctamente";
            //}
            ArrayList<String[]> cuerpo = new ArrayList<>();
            cuerpo.add(caso);
            Email emailObject = new Email(correo, Email.SUBJECT, HtmlBuilder.generateTabla(encabezado,cuerpo));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
            System.out.println(Arrays.toString(caso));
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
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
            ncasohospital.eliminar(parametros);
            mensaje = "Caso Brigada Eliminado Correctamente";
            //}
            Email emailObject = new Email(correo, Email.SUBJECT, HtmlBuilder.generateSuccess("Muy Bien", mensaje));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
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
            ncasohospital.modificar(parametros);
            mensaje = "Caso Brigada Modificado Correctamente";
            //}
            Email emailObject = new Email(correo, Email.SUBJECT, HtmlBuilder.generateSuccess("Muy Bien", mensaje));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
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
            ncasohospital.guardar(parametros);
            mensaje = "Caso Brigada Guardado Correctamente";
            //}
            Email emailObject = new Email(correo, Email.SUBJECT, HtmlBuilder.generateSuccess("Muy Bien", mensaje));
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        }catch (PSQLException e) {
            Handler.handleError(Handler.SQLERROR, correo, null,e.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(CasoBrigada.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
