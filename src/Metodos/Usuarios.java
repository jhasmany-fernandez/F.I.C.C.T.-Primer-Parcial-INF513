/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Metodos;

import Negocio.NUsuario;
import Utils.Email;
import Utils.Handler;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import proyectoemail.MailAplication;

public class Usuarios {

    NUsuario nUsuario;

    public Usuarios() {
        nUsuario = new NUsuario();
    }

    public void agregar(ArrayList<String> parametros, String correo) {
        try {
            String titulo = "CU1 Usuario, insertar usuario";
            String mensaje = "Ocurrio un inconveniente";

            /*if (nUsuario.permiso(correo)) {

                if (!nUsuario.isAdministrador(correo)) {
                    mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos de Administrador");
                } else {*/

                    if (!NUsuario.esCorreoElectronico(parametros.get(2).trim())) {
                        mensaje = HtmlBuilder.generateError(titulo, "El correo del usuario a insertar no es valido");
                    } else {
                        nUsuario.guardar(parametros);
                        mensaje = HtmlBuilder.generateSuccess(titulo, "Se agregó correctamente un usuario al sistema");
                    }
               /* }
            } else {
                mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos, No estas registrado");
            }*/

            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println("---------------CU1 Usuario Insertar Completado---------------");
            System.out.println(mensaje);
        } catch (SQLException ex) {
            Handler.handleError(Handler.SQLERROR, correo, null, ex.getMessage());
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null, "");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null, "");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null, "");
        } catch (IllegalArgumentException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null, "");
        } catch (NullPointerException ex) {
            Handler.handleError(Handler.ROL_ERROR, correo, null, "");
        }
    }

    public void modificar(ArrayList<String> parametros, String correo) {
        try {
            String titulo = "CU1 Usuario, modificar usuario";
            String mensaje = "Ocurrio un inconveniente";

            if (nUsuario.permiso(correo)) {

                if (!nUsuario.isAdministrador(correo)) {
                    mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos de Administrador");
                } else {
                    if (NUsuario.esCorreoElectronico(parametros.get(2))) {
                        mensaje = HtmlBuilder.generateError(titulo, "El correo co el que quiere editar el usuario no es valido");
                    } else {
                        nUsuario.modificar(parametros);
                        mensaje = HtmlBuilder.generateSuccess(titulo, "Se modificó correctamente un usuario del sistema");
                    }
                }
            } else {
                mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos, No estas registrado");
            }

            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println("---------------CU1 Usuario Modificar Completado---------------");
            System.out.println(mensaje);
        } catch (SQLException ex) {
            Handler.handleError(Handler.SQLERROR, correo, null, ex.getMessage());
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null, "");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null, "");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null, "");
        } catch (IllegalArgumentException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null, "");
        } catch (NullPointerException ex) {
            Handler.handleError(Handler.ROL_ERROR, correo, null, "");
        }
    }

    public void eliminar(ArrayList<String> parametros, String correo) {
        try {
            String titulo = "CU1 Usuarios, inhabilitar usuario";
            String mensaje = "Ocurrio un inconveniente";

            if (nUsuario.permiso(correo)) {

                if (!nUsuario.isAdministrador(correo)) {
                    mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos de Administrador");
                } else {
                    nUsuario.eliminar(parametros);
                    mensaje = "Se eliminó correctamente del sistema un usuario";
                    mensaje = HtmlBuilder.generateSuccess(titulo, mensaje);
                }
            } else {
                mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos, No estas registrado");
            }

            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println("---------------CU1 Usuario Inhabilitar Completado---------------");
        } catch (SQLException ex) {
            Handler.handleError(Handler.SQLERROR, correo, null, ex.getMessage());
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null, "");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null, "");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null, "");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null, "");
        }
    }

    public void ver(ArrayList<String> parametros, String correo) {
        try {
            String titulo = "CU1 Usuarios, ver usuario";
            String mensaje = "Ocurrio un inconveniente";

            if (nUsuario.permiso(correo)) {

                if (!nUsuario.isAdministrador(correo)) {
                    mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos de Administrador");
                } else {
                    String[] usuario = nUsuario.ver(parametros);
                    ArrayList<String[]> usuarios = new ArrayList<>();
                    if (usuario != null) {
                        usuarios.add(usuario);
                    }
                    String[] headers = new String[]{
                        "Id",
                        "Ci",
                        "Nombre",
                        "Email",
                        "Apellido paterno",
                        "Apellido materno",
                        "Telefono",
                        "Ubicacion",
                        "Longitud",
                        "Latitud",
                        "Estado",
                        "Genero",
                        "Fecha de nacimiento",
                        "Contraseña"
                    };
                    mensaje = HtmlBuilder.generateTable(titulo, "Listar Usuarios", headers, usuarios);
                }
            } else {
                mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos, No estas registrado");
            }

            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println("---------------CU1 Usuario Ver Completado---------------");
        } catch (SQLException ex) {
            Handler.handleError(Handler.SQLERROR, correo, null, ex.getMessage());
        } catch (ParseException ex) {
            Handler.handleError(Handler.PARSE_ERROR, correo, null, "");
        } catch (IndexOutOfBoundsException ex) {
            Handler.handleError(Handler.INDEX_OUT_OF_BOUND_ERROR, correo, null, "");
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null, "");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null, "");
        }
    }

    public void listar(ArrayList<String> parametros, String correo) {
        try {
            String titulo = "CU1 Usuario listar";
            String mensaje = "Ocurrio un inconveniente";

            if (nUsuario.permiso(correo)) {

                if (!nUsuario.isAdministrador(correo)) {
                    mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos de Administrador");
                } else {
                    ArrayList<String[]> usuarios = nUsuario.listar();
                    String[] headers = new String[]{
                        "Id",
                        "Ci",
                        "Nombre",
                        "Email",
                        "Apellido paterno",
                        "Apellido materno",
                        "Telefono",
                        "Ubicacion",
                        "Longitud",
                        "Latitud",
                        "Estado",
                        "Genero",
                        "Fecha de nacimiento",
                        "Contraseña"
                    };
                    mensaje = HtmlBuilder.generateTable("CU1 Usuario", "Listar Usuarios", headers, usuarios);
                }
            } else {
                mensaje = HtmlBuilder.generateError(titulo, "No tiene los permisos, No estas registrado");
            }

            Email emailObject = new Email(correo, Email.SUBJECT, mensaje);
            Email.sendEmail(emailObject);
            System.out.println("---------------CU1 Usuario Listar Completado---------------");
        } catch (SQLException ex) {
            Handler.handleError(Handler.SQLERROR, correo, null, ex.getMessage());
        } catch (NumberFormatException ex) {
            Handler.handleError(Handler.NUMBER_FORMAT_ERROR, correo, null, "");
        } catch (RuntimeException ex) {
            Handler.handleError(Handler.CONSTRAINTS_ERROR, correo, null, "");
        }
    }
}
