/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Metodos;

import Negocio.NReportes;
import Negocio.NUsuario;
import Utils.Email;
import proyectoemail.MailAplication;
import Utils.Handler;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedri
 */
public class Reportes {
    NReportes nReportes;

    public Reportes() {
        nReportes = new NReportes();
    }
    
    public void ver(ArrayList<String> parametros, String correo) {
         try {
            String titulo = "CU8 Reportes y Estadisticas";
            String mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos, No estas registrado");

            NUsuario nUsuario = new NUsuario();
            //if (nUsuario.permiso(correo)) {
            //    if (!nUsuario.isAdministrador(correo) && !nUsuario.isFuncionario(correo)) {
            //        mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos de Administrador o Funcionario");
            //    } else {
                    mensaje = nReportes.ver(parametros);
           //     }
           // }
            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println("---------------CU8 Reportes y Estadisticas Completado---------------");
        } catch (SQLException ex) {
            System.out.println("entre a SQLException");
            Logger.getLogger(MailAplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null,"");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null,"");
        }
    }
}
