/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Metodos;

import Negocio.NCasosBrigadas;
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

/**
 *
 * @author Usuario
 */
public class CasoBrigada {
    
    private NCasosBrigadas ncasobrigada;
    private String[] encabezado = {"Id", "fecha Inicio", "Fecha Final", "Detalle", "User_id", "Estado_id", "Enfermedad_id","Estadia_enfermedable_id", "Estadia_enfermedable_type", "created_at", "updated_at", "Sintomas"};
    

    public CasoBrigada() {
        this.ncasobrigada = new NCasosBrigadas();
    }
    
    
    public void listar(ArrayList<String> parametros, String correo) {
        try {
            String mensaje = "No tiene los permisos, No es un Usuario registrado";
            //if (brigada.permiso(correo)) {
            ArrayList<String[]> casos = ncasobrigada.listar();
            mensaje = "Caso Brigada Listar Correctamente";
            //}
            mensaje = "Mapa Listar Correctamente";
            //}
            Email emailObject = new Email(correo, "GRUPO 13 SC", HtmlBuilder.generateTabla(encabezado,casos));
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
            String[] caso = ncasobrigada.ver(parametros);
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
            ncasobrigada.eliminar(parametros);
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
            ncasobrigada.modificar(parametros);
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
            ncasobrigada.guardar(parametros);
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
        }catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        } catch (PSQLException e) {
            Handler.handleError(Handler.SQLERROR, correo, null,e.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(CasoBrigada.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Manejo de la excepción
        
    }
}
