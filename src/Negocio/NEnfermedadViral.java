
package Negocio;

import Datos.DEnfermedadViral;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class NEnfermedadViral {
    private DEnfermedadViral dEnfermedadViral;
     
    public NEnfermedadViral() {
        dEnfermedadViral = new DEnfermedadViral();
    }
    
    public void guardar(List<String> parametros) throws SQLException, ParseException {
        if(parametros.size()>2){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        dEnfermedadViral.guardar(parametros.get(0), parametros.get(1));
        dEnfermedadViral.desconectar();
        System.out.println("NEGOCIO: CASO ENFERMEDAD GUARDADO");
    }
    
    public void modificar(List<String> parametros) throws SQLException, ParseException {
        if(parametros.size()>3){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        int id = Integer.parseInt(parametros.get(0));
        
        if(dEnfermedadViral.existe(id)== -1){
            throw new RuntimeException ("id no existente");
        }
        
        dEnfermedadViral.modificar(Integer.parseInt(parametros.get(0)), parametros.get(1), 
                parametros.get(2));
        dEnfermedadViral.desconectar();
        System.out.println("NEGOCIO: CASO ENFERMEDAD MODIFICADO");
        
    }
    
    public void eliminar(List<String> parametros) throws SQLException {
        if(parametros.size()>1){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        int id = Integer.parseInt(parametros.get(0));
        
        if(dEnfermedadViral.existe(id)== -1){
            throw new RuntimeException ("id no existente");
        }
        
        dEnfermedadViral.eliminar(Integer.parseInt(parametros.get(0)));
        dEnfermedadViral.desconectar();
        System.out.println("NEGOCIO: CASO ENFERMEDAD ELIMINADO");
        
    }
    
    public ArrayList<String[]> listar() throws SQLException {
        
        ArrayList<String[]> enfermedad = (ArrayList<String[]>) dEnfermedadViral.listar();
        dEnfermedadViral.desconectar();
        System.out.println("NEGOCIO: CASO ENFERMEDAD LISTAR");
        return enfermedad;
    }
    
    public String[] ver(List<String> parametros) throws SQLException, ParseException {
        String[] enfermedad = dEnfermedadViral.ver(Integer.valueOf(parametros.get(0)));
        dEnfermedadViral.desconectar();
        return enfermedad;
    }
}
