package Utils;

import java.sql.Date;
import java.util.ArrayList;

public class Validator {

    /*
   |    Date fecha_ini = Utiles.StringToDate(parametros.get(0));
        Date fecha_fin = Utiles.StringToDate(parametros.get(1));
        String detalle = parametros.get(2);
        int user_id = Integer.parseInt(parametros.get(3));
        int estado_id = Integer.parseInt(parametros.get(4));
        int enfermedad_id = Integer.parseInt(parametros.get(5));
        int estadia_enfermedable_id = Integer.parseInt(parametros.get(6));*/
    
    public static boolean esNumero(String cadena) {
        return cadena.matches("-?\\d+");
    }

}
