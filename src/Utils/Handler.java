/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import interpreter.analex.utils.Token;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class Handler {

    public static final int CONSTRAINTS_ERROR = -2;
    public static final int NUMBER_FORMAT_ERROR = -3;
    public static final int INDEX_OUT_OF_BOUND_ERROR = -4;
    public static final int PARSE_ERROR = -5;
    public static final int AUTHORIZATION_ERROR = -6;
    public static final int SQLERROR = -7;
    public static final int ROL_ERROR = -8;
    public static final int FOREING_KEY_ERROR = -9;

    public static void handleError(int type, String email, List<String> args, String msjSQL) {
        Email emailObject = null;
        String html = "";
        System.out.println("Entre al handleError");

        switch (type) {
            case Token.ERROR_CHARACTER:
                html = HtmlBuilder.generateError("UPS ERROR!", "Caracter desconocido"
                        + "No se pudo ejecutar el comando [" + args.get(0) + "] debido a: "
                        + "El caracter \"" + args.get(1) + "\" es desconocido."
                );
                break;
            case Token.ERROR_COMMAND:
                html = HtmlBuilder.generateError("UPS ERROR!", "Comando desconocido"
                        + "No se pudo ejecutar el comando [" + args.get(0) + "] debido a: "
                        + "No se reconoce la palabra \"" + args.get(1) + "\" como un comando válido"
                );
                break;
            case CONSTRAINTS_ERROR:
                html = HtmlBuilder.generateError("UPS ERROR!", "Error al interactuar con la base de datos"
                        + ", Referencia a información inexistente"
                );
                break;
            case NUMBER_FORMAT_ERROR:
                html = HtmlBuilder.generateError("UPS ERROR!", "Error en el tipo de parámetro"
                        + "El tipo de uno de los parámetros es incorrecto"
                );
                break;
            case INDEX_OUT_OF_BOUND_ERROR:
                html = HtmlBuilder.generateError("UPS ERROR!", "Cantidad de parámetros incorrecta, "
                        + "La cantidad de parámetros para realizar la acción es incorrecta");
                break;
            case PARSE_ERROR:
                html = HtmlBuilder.generateError("UPS ERROR!", "Error al procesar la fecha"
                        + "La fecha introducida posee un formato incorrecto"
                );
                break;
            case AUTHORIZATION_ERROR:
                html = HtmlBuilder.generateError("UPS ERROR!", "Acceso denegado"
                        + "Usted no posee los permisos necesarios para realizar la acción solicitada"
                );
                break;
            case SQLERROR:
                html = HtmlBuilder.generateError("UPS ERROR!", msjSQL);
                break;
            case ROL_ERROR:
                html = HtmlBuilder.generateError("UPS ERROR!", "El nombre del Rol esta mal, "
                        + " Los roles disponibles son: Administrador, Funcionario, Personal Medico, Paciente"
                );
            case FOREING_KEY_ERROR:
                html = HtmlBuilder.generateError("UPS ERROR!", "Error en Llave Foranea "
                        + "La llave foranea que trata de introduccir no existe"
                );
                break;
        }

        emailObject = new Email(email, "GRUPO 13 SC", html);
        Email.sendEmail(emailObject);
        System.out.println("ERROR: " + emailObject.getMessage());
        System.out.println("SUBJECT: " + emailObject.getSubject());
    }

}
