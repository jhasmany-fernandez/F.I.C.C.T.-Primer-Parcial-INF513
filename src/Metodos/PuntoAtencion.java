
package Metodos;

import Negocio.NPuntoAtencion;
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


public class PuntoAtencion {
    NPuntoAtencion nPuntoAtencion;
     
     public PuntoAtencion() {
        this.nPuntoAtencion = new NPuntoAtencion();
    }
    
    public void agregar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            nPuntoAtencion.guardar(parametros);
            mensaje = HtmlBuilder.generateSuccess("CU Hospitales, agregar Punto Atencion", "Se agregó correctamente un punto de atencion al sistema");

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
        } catch (NullPointerException ex) {
            Handler.handleError(Handler.FOREING_KEY_ERROR, correo, null,"");
        }
     }
     
     public void listar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            ArrayList<String[]> casos = nPuntoAtencion.listar();
            mensaje = "CU Hospitales Listar Correctamente";
            //}
            String[] headers = new String[]{
                 "Id",
                 "Nombre",
                 "Descripcion",
                 "Longitud",
                 "Latitud",
                 "Num_Camillas",
                 "Num_Cuartos",
                 "Id Tipo_punto"
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
            nPuntoAtencion.eliminar(parametros);
            mensaje = HtmlBuilder.generateSuccess("CU Hospitales, eliminar Punto Atencion", "Se elimino correctamente un punto de atencion en el sistema");

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
            nPuntoAtencion.modificar(parametros);
            mensaje = HtmlBuilder.generateSuccess("CU Hospitales, modificar Punto Atencion", "Se modifico correctamente un punto de atencion en el sistema");

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
        }catch (Exception ex) {
            Handler.handleError(Handler.FOREING_KEY_ERROR, correo, null,"");
        }

    } 
    
    public void ver(ArrayList<String> parametros, String correo) {
        try {
            String titulo = "CU Usuarios, ver usuario";
            String mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos, No es un Usuario registrado");
//            if (nUsuario.permiso(correo)) {

           String[] pa = nPuntoAtencion.ver(parametros);
            ArrayList<String[]> casos = new ArrayList<>();

            if (pa != null) {
                casos.add(pa);
            }
            
            mensaje = "CU Hospitales Verificar Correctamente";
            //}
            String[] headers = new String[]{
                 "Id",
                 "Nombre",
                 "Descripcion",
                 "Longitud",
                 "Latitud",
                 "Num_Camillas",
                 "Num_Cuartos",
                 "Id Tipo_punto"
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
