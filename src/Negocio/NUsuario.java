/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Negocio;

import Datos.DUsuario;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author USUARIO
 */
public class NUsuario {

    private final DUsuario dUsuario;

    private static final Map<String, Integer> ROLES;

    // Bloque estático de inicialización
    static {
        ROLES = new HashMap<>();
        ROLES.put("Administrador", 1);
        ROLES.put("Funcionario", 2);
        ROLES.put("Personal Medico", 3);
        ROLES.put("Paciente", 4);

    }

    public NUsuario() {
        dUsuario = new DUsuario();
    }
    
    public boolean isActivo(String correo) throws SQLException{
        int estado = dUsuario.getEstado(correo);
        dUsuario.desconectar();
        return estado == 1;
    }

    public static boolean esCorreoElectronico(String texto) {
        // Expresión regular para verificar si el texto es una dirección de correo electrónico válida
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        // Compilar la expresión regular en un patrón
        Pattern pattern = Pattern.compile(regex);
        // Crear un objeto Matcher para comparar el texto con el patrón
        Matcher matcher = pattern.matcher(texto);
        // Verificar si el texto coincide con el patrón
        return matcher.matches();
    }

    public boolean isAdministrador(String correo) throws SQLException {
        int usuarioId = dUsuario.getRolByCorreo(correo);
        dUsuario.desconectar();
        return usuarioId == 1;
    }

    public boolean isFuncionario(String correo) throws SQLException {
        int usuarioId = dUsuario.getRolByCorreo(correo);
        dUsuario.desconectar();
        return usuarioId == 2;
    }

    public boolean isPersonalM(String correo) throws SQLException {
        int usuarioId = dUsuario.getRolByCorreo(correo);
        dUsuario.desconectar();
        return usuarioId == 3;
    }

    public boolean isPaciente(String correo) throws SQLException {
        int usuarioId = dUsuario.getRolByCorreo(correo);
        dUsuario.desconectar();
        return usuarioId == 4;
    }

    public boolean permiso(String correo) throws SQLException {
        int usuarioId = dUsuario.getIdByCorreo(correo);
        dUsuario.desconectar();
        return usuarioId != -1;
    }

    public void guardar(ArrayList<String> usuario) throws SQLException, ParseException, IllegalArgumentException, NullPointerException {
        dUsuario.guardar(Integer.valueOf(usuario.get(0)), usuario.get(1), usuario.get(2),
                usuario.get(3), usuario.get(4), usuario.get(5), usuario.get(6),
                Float.valueOf(usuario.get(7)), Float.valueOf(usuario.get(8)), usuario.get(9), Date.valueOf(usuario.get(10)), usuario.get(11), ROLES.get(usuario.get(12).trim()));
        dUsuario.desconectar();
    }

    public void modificar(ArrayList<String> usuario) throws SQLException, ParseException, IllegalArgumentException, NullPointerException {
        dUsuario.modificar(Integer.valueOf(usuario.get(0)), usuario.get(1), usuario.get(2),
                usuario.get(3), usuario.get(4), usuario.get(5), usuario.get(6),
                usuario.get(7), usuario.get(8), usuario.get(9), usuario.get(10), usuario.get(11), usuario.get(12), ROLES.get(usuario.get(13)));
        dUsuario.desconectar();
    }

    public void eliminar(List<String> parametros) throws SQLException, ParseException {
        dUsuario.eliminar(Integer.valueOf(parametros.get(0)));
        dUsuario.desconectar();
    }

    public void habilitar(List<String> parametros) throws SQLException, ParseException {
        dUsuario.habilitar(Integer.valueOf(parametros.get(0)));
        dUsuario.desconectar();
    }

    public ArrayList<String[]> listar() throws SQLException {
        ArrayList<String[]> usuarios = new ArrayList<>();
        usuarios = dUsuario.listar();
        return usuarios;
    }

    public String[] ver(List<String> parametros) throws SQLException, ParseException {
        String[] usuario = dUsuario.ver(Integer.valueOf(parametros.get(0)));
        dUsuario.desconectar();
        return usuario;
    }

    public static void main(String[] args) throws SQLException, ParseException {
        NUsuario u = new NUsuario();
        ArrayList<String> parametros = new ArrayList<>(Arrays.asList("2023-11-02", "2023-12-02", "detalle SI", "1", "6", "4", "2", "1", "2"));

        ArrayList<String> x = new ArrayList<>();
            x.add("12878");
            x.add("asd");
            x.add("asddsa@gmail.com");
            x.add("ASD");
            x.add("ASD");
            x.add("123123");
            x.add("AV");
            x.add("12.32");
            x.add("12.2");
            x.add("M");
            x.add("2001-02-01");
            x.add("123123123");
            x.add("Administrador");
            
        u.guardar(x);
        System.out.println(NUsuario.esCorreoElectronico("ejemplo.jas"));
    }
}
