
package Metodos;

import Negocio.NTipoPunto;
import Utils.Email;
import Utils.Handler;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import proyectoemail.MailAplication;


public class TipoPunto {
    NTipoPunto nTipoPunto;
     
     public TipoPunto() {
        this.nTipoPunto = new NTipoPunto();
    }
    public void agregar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            nTipoPunto.guardar(parametros);
            
            mensaje = HtmlBuilder.generateSuccess("agregar tipo Punto", "Se agregó correctamente un tipo Punto al sistema");

            //}
            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (SQLException ex) {
            System.out.println("entre a SQLException");
            Logger.getLogger(MailAplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        }
     }
     
     public void listar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            ArrayList<String[]> casos = nTipoPunto.listar();
            mensaje = "Caso Tipo Puntos Listar Correctamente";
            
            String[] headers = new String[]{
                 "Id",
                 "Nombre",
                 "Descripcion"
             };
            //}
            mensaje = HtmlBuilder.generateTabla(headers, casos);
            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
            
            for (int i = 0; i < casos.size(); i++) {
                System.out.println("Caso[" + i + "]: " + Arrays.toString(casos.get(i)));
            }
        } catch (SQLException ex) {
            System.out.println("entre a SQLException");
            Logger.getLogger(MailAplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        }

    }

   
    public void eliminar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            nTipoPunto.eliminar(parametros);
            mensaje = HtmlBuilder.generateSuccess("eliminar tipo Punto", "Se elimino correctamente un tipo Punto en el sistema");

            //}
            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (SQLException ex) {
            System.out.println("entre a SQLException");
            Logger.getLogger(MailAplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        }

    }

    public void modificar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            nTipoPunto.modificar(parametros);
            mensaje = HtmlBuilder.generateSuccess("modificar tipo Punto", "Se modifico correctamente un tipo Punto en el sistema");

            //}
            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println(mensaje);
        } catch (SQLException ex) {
            System.out.println("entre a SQLException");
            Logger.getLogger(MailAplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        }

    } 
    
    public void ver(ArrayList<String> parametros, String correo) {
        try {
            String titulo = "CU Usuarios, ver usuario";
            String mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos, No es un Usuario registrado");
//            if (nUsuario.permiso(correo)) {

           String[] pa = nTipoPunto.ver(parametros);
            ArrayList<String[]> casos = new ArrayList<>();

            if (pa != null) {
                casos.add(pa);
            }
            
            mensaje = "CU Hospitales Verificar Correctamente";
            //}
            String[] headers = new String[]{
                 "Id",
                 "Nombre",
                 "Descripcion"
             };
            //}
            mensaje = HtmlBuilder.generateTabla(headers, casos);

            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println("***************************usario*******************");
        } catch (SQLException ex) {
            System.out.println("entre a SQLException");
            Logger.getLogger(MailAplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null,"");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        }
    }
}
