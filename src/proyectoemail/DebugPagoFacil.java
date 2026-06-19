package proyectoemail;

import Negocio.NPago;
import java.util.Arrays;

public class DebugPagoFacil {

    public static void main(String[] args) throws Exception {
        NPago nPago = new NPago();
        System.out.println("Iniciando prueba directa PagoFacil...");
        Object result = nPago.generarQr(
                Arrays.asList("7", "10.50", "2026-06-02", "12345678", "70000000", "Mensualidad junio"),
                "jhasmany.fernandez.dev@gmail.com"
        );
        System.out.println("Resultado: " + result);
    }
}
