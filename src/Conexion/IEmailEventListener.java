package Conexion;

import java.util.List;
import Utils.Email;


public interface IEmailEventListener {
    
    void onReceiveEmailEvent(List<Email> emails);
    
}
