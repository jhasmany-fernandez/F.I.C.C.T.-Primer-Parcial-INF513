
package Negocio;


import Datos.DEstado;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class NEstado {
    private DEstado dEstado;
     
    public NEstado() {
        dEstado = new DEstado();
    }
    
    public void guardar(List<String> parametros) throws SQLException, ParseException {
        if(parametros.size()>2){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        dEstado.guardar(parametros.get(0), parametros.get(1));
        dEstado.desconectar();
        System.out.println("NEGOCIO: CASO ESTADO GUARDADO");
    }
    
    public void modificar(List<String> parametros) throws SQLException, ParseException {
        if(parametros.size()>3){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        int id = Integer.parseInt(parametros.get(0));
        
        if(dEstado.existe(id)== -1){
            throw new RuntimeException ("id no existente");
        }
        
        dEstado.modificar(Integer.parseInt(parametros.get(0)), parametros.get(1), 
                parametros.get(2));
        dEstado.desconectar();
        System.out.println("NEGOCIO: CASO ESTADO MODIFICADO");
    }
    
    public void eliminar(List<String> parametros) throws SQLException {
         if(parametros.size()>1){
            throw new IndexOutOfBoundsException("Muchos Parametros: ");
        }
        int id = Integer.parseInt(parametros.get(0));
        
        if(dEstado.existe(id)== -1){
            throw new RuntimeException ("id no existente");
        }
        
        dEstado.eliminar(Integer.parseInt(parametros.get(0)));
        dEstado.desconectar();
        System.out.println("NEGOCIO: CASO ESTADO ELIMINADO");

    }
    
    public ArrayList<String[]> listar() throws SQLException {
        ArrayList<String[]> estados = (ArrayList<String[]>) dEstado.listar();
        dEstado.desconectar();
        System.out.println("NEGOCIO: CASO ESTADO LISTAR");
        return estados;
    }
    
    public String[] ver(List<String> parametros) throws SQLException, ParseException {
        String[] estados = dEstado.ver(Integer.valueOf(parametros.get(0)));
        dEstado.desconectar();
        return estados;
    }
}
