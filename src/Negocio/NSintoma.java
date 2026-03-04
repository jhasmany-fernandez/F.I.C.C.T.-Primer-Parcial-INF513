
package Negocio;


import Datos.DSintoma;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class NSintoma {
    
    private DSintoma dSintoma;
     
    public NSintoma() {
        dSintoma = new DSintoma();
    }
    
    public void guardar(List<String> parametros) throws SQLException, ParseException {
        if(parametros.size()>2){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        dSintoma.guardar(parametros.get(0), parametros.get(1));
        dSintoma.desconectar();
        System.out.println("NEGOCIO: CASO SINTOMAS GUARDADO");
    }
    
    public void modificar(List<String> parametros) throws SQLException, ParseException {
        if(parametros.size()>3){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        int id = Integer.parseInt(parametros.get(0));
        
        if(dSintoma.existe(id)== -1){
            throw new RuntimeException ("id no existente");
        }
        
        dSintoma.modificar(Integer.parseInt(parametros.get(0)), parametros.get(1), 
                parametros.get(2));
        dSintoma.desconectar();
        System.out.println("NEGOCIO: CASO SINTOMAS MODIFICADO");
    }
    
    public void eliminar(List<String> parametros) throws SQLException {
        if(parametros.size()>1){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        int id = Integer.parseInt(parametros.get(0));
        
        if(dSintoma.existe(id)== -1){
            throw new RuntimeException ("id no existente");
        }
        dSintoma.eliminar(Integer.parseInt(parametros.get(0)));
        dSintoma.desconectar();
        System.out.println("NEGOCIO: CASO SINTOMAS ELIMINADO");
    }
    
    public ArrayList<String[]> listar() throws SQLException {
        ArrayList<String[]> sintomas = (ArrayList<String[]>) dSintoma.listar();
        dSintoma.desconectar();
        System.out.println("NEGOCIO: CASO SINTOMAS LISTAR");
        return sintomas;
    }
    
    public String[] ver(List<String> parametros) throws SQLException, ParseException {
        String[] sintomas = dSintoma.ver(Integer.valueOf(parametros.get(0)));
        dSintoma.desconectar();
        return sintomas;
    }
}
