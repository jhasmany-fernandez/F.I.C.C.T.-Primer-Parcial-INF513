
package Metodos;

import Negocio.NEnfermedadViral;
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


public class Enfermedad {
     NEnfermedadViral nEnfermedadViral;
     
     public Enfermedad() {
        this.nEnfermedadViral = new NEnfermedadViral();
    }
     
     public void agregar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            nEnfermedadViral.guardar(parametros);
            
            mensaje = HtmlBuilder.generateSuccess("agregar Enfermedad Viral", "Se agregó correctamente una enfermedad viral al sistema");

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
            ArrayList<String[]> casos = nEnfermedadViral.listar();
            mensaje = "Caso Enfermedad Listar Correctamente";
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
            nEnfermedadViral.eliminar(parametros);
            mensaje = HtmlBuilder.generateSuccess("eliminar Enfermedad Viral", "Se elimino correctamente una enfermedad viral en el sistema");

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
            nEnfermedadViral.modificar(parametros);
            mensaje = HtmlBuilder.generateSuccess("modificar Enfermedad Viral", "Se modifico correctamente una enfermedad viral en el sistema");

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

           String[] pa = nEnfermedadViral.ver(parametros);
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
