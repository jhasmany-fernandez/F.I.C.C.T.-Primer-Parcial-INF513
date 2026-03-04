
package Negocio;

import Datos.DTipoPunto;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class NTipoPunto {

    private DTipoPunto dTipoPunto;
     
    public NTipoPunto() {
        dTipoPunto = new DTipoPunto();
    }
    
    public void guardar(List<String> parametros) throws SQLException, ParseException {
        if(parametros.size()>2){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        dTipoPunto.guardar(parametros.get(0), parametros.get(1));
        dTipoPunto.desconectar();
        System.out.println("NEGOCIO: CASO TIPO PUNTO GUARDADO");
    }
    
    public void modificar(List<String> parametros) throws SQLException, ParseException {
        if(parametros.size()>3){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        int id = Integer.parseInt(parametros.get(0));
        
        if(dTipoPunto.existe(id)== -1){
            throw new RuntimeException ("id no existente");
        }
        
        dTipoPunto.modificar(Integer.parseInt(parametros.get(0)), parametros.get(1), 
                parametros.get(2));
        dTipoPunto.desconectar();
        System.out.println("NEGOCIO: CASO TIPO PUNTO MODIFICADO");
    }
    
    public void eliminar(List<String> parametros) throws SQLException {
        if(parametros.size()>1){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        int id = Integer.parseInt(parametros.get(0));
        if(dTipoPunto.existe(id)== -1){
            throw new RuntimeException ("id no existente");
        }
        dTipoPunto.eliminar(Integer.parseInt(parametros.get(0)));
        dTipoPunto.desconectar();
        System.out.println("NEGOCIO: CASO TIPO PUNTO ELIMINADO");
    }
    
    public ArrayList<String[]> listar() throws SQLException {
        ArrayList<String[]> tipoPuntos = (ArrayList<String[]>) dTipoPunto.listar();
        dTipoPunto.desconectar();
        System.out.println("NEGOCIO: CASO TIPO PUNTO LISTAR");
        return tipoPuntos;
    }
    
    public String[] ver(List<String> parametros) throws SQLException, ParseException {
        String[] tipoPuntos = dTipoPunto.ver(Integer.valueOf(parametros.get(0)));
        dTipoPunto.desconectar();
        return tipoPuntos;
    }
}
