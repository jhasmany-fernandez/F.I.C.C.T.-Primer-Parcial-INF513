/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Negocio;

import Datos.DEnfermedadViralMapa;
import Datos.DMapas;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import org.postgresql.util.PSQLException;

/**
 *
 * @author Usuario
 */
public class NMapas {

    private DMapas dmapas;
    private DEnfermedadViralMapa denfermedadviralmapa;

    
    public NMapas() {
        this.dmapas = new DMapas();
        this.denfermedadviralmapa = new DEnfermedadViralMapa();
    }

    
    public void guardar(ArrayList<String> parametros) throws SQLException, ParseException, PSQLException {

        String name = parametros.get(0);
        String detalle = parametros.get(1);
        Double latitud = Double.parseDouble(parametros.get(2));
        Double longitud = Double.parseDouble(parametros.get(3));

        long mapa_id = dmapas.guardar(name, detalle, latitud, longitud);
        int enfermedad_id;
        
        ArrayList<String> enfermedadesID = new ArrayList<>(parametros.subList(4, parametros.size()));
        
        for (int i = 0; i < enfermedadesID.size(); i++) {
            enfermedad_id = Integer.parseInt(enfermedadesID.get(i));
            denfermedadviralmapa.guardar(enfermedad_id, (int) mapa_id);
        }

        System.out.println("NEGOCIO: MAPA GUARDADO");
    }
    

    public void modificar(ArrayList<String> parametros) throws SQLException, ParseException, PSQLException  {

        int mapa_id = Integer.parseInt(parametros.get(0));
        String name = parametros.get(1);
        String detalle = parametros.get(2);
        Double latitud = Double.parseDouble(parametros.get(3));
        Double longitud = Double.parseDouble(parametros.get(4));
        
        if(!dmapas.existeID(mapa_id))
            throw new RuntimeException ("id no existente");

        dmapas.modificar(mapa_id, name, detalle, latitud, longitud);
        denfermedadviralmapa.eliminar(mapa_id);
        
        int enfermedad_id;
        ArrayList<String> enfermedadesID = new ArrayList<>(parametros.subList(5, parametros.size()));
        for (int i = 0; i < enfermedadesID.size(); i++) {
            enfermedad_id = Integer.parseInt(enfermedadesID.get(i));
            denfermedadviralmapa.guardar(enfermedad_id, mapa_id);
        }

        System.out.println("NEGOCIO: MAPA MODIFICADO");
    }
    
    
    public void eliminar(ArrayList<String> parametros) throws SQLException, ParseException, PSQLException  {
        
        int mapa_id = Integer.parseInt(parametros.get(0));
        
        if (parametros.size() > 1) {
            throw new IndexOutOfBoundsException("Muchos Parametros");
        }
        
        if (!dmapas.existeID(mapa_id)) {
            throw new RuntimeException("id no existente tipo brigada");
        }
        
        denfermedadviralmapa.eliminar(mapa_id);
        dmapas.eliminar(mapa_id);
        
        System.out.println("NEGOCIO: MAPA ELIMINADO");
    }
    
    
    public String[] ver(ArrayList<String> parametros) throws SQLException, ParseException, PSQLException  {
        
        if (parametros.size() > 1) {
            throw new IndexOutOfBoundsException("Muchos Parametros");
        }
        
        int mapa_id = Integer.parseInt(parametros.get(0));
        
        if (!dmapas.existeID(mapa_id)) {
            throw new RuntimeException("id no existente");
        }
        
        String[] mapa = dmapas.ver(mapa_id);
        String[] enfermedadesId = denfermedadviralmapa.ver(mapa_id);
        
        String enfers = "";
        for (String enfermedad : enfermedadesId) {
            enfers += enfermedad + ", ";
        }

        String[] newMapa = new String[mapa.length + 1];
        System.arraycopy(mapa, 0, newMapa, 0, mapa.length);
        newMapa[mapa.length] = enfers;

        System.out.println("NEGOCIO: MAPA VER");
        return newMapa;
    }
    
    private String[] verList(String id) throws SQLException, ParseException, PSQLException  {
        
        int mapa_id = Integer.parseInt(id);
 
        String[] mapa = dmapas.ver(mapa_id);
        String[] enfermedadesId = denfermedadviralmapa.ver(mapa_id);
        
        String enfers = "";
        for (String enfermedad : enfermedadesId) {
            enfers += enfermedad + ", ";
        }

        String[] newMapa = new String[mapa.length + 1];
        System.arraycopy(mapa, 0, newMapa, 0, mapa.length);
        newMapa[mapa.length] = enfers;

        System.out.println("NEGOCIO: MAPA VER");
        return newMapa;
    }
    
    public ArrayList<String[]> listar() throws SQLException, ParseException, PSQLException {
        ArrayList<String[]> mapasConEnfermedades = new ArrayList<>();
        ArrayList<String> mapasId = dmapas.listar();
        
        for (String mapa : mapasId) {
            mapasConEnfermedades.add(verList(mapa));
        }
  
        return mapasConEnfermedades;
    }

    public static void main(String[] args) throws SQLException, ParseException {
        NMapas A = new NMapas();

        ArrayList<String> datos = new ArrayList<>(Arrays.asList("mapa1X","detalle de mapa 2X","10.57","19.457","2","2","3"));
        //ArrayList<String> datos = new ArrayList<>(Arrays.asList("2","mapa2XL","detalle de mapa 2X","10.57","19.457","1","3"));
        ArrayList<String> datosver = new ArrayList<>(Arrays.asList("2"));
        //A.guardar(datos);
        //A.modificar(datos);
        //A.eliminar("3");
        
        //System.out.println( Arrays.toString(A.ver(datosver)) );
        
        ArrayList<String[]> listado = A.listar();
        for (int i = 0; i < listado.size(); i++) {
            System.out.println("Mapa["+i+"]: "+Arrays.toString(listado.get(i)));
        }
        //estaba probando el listar del negocio

    }

}
