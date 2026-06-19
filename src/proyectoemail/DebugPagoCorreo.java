package proyectoemail;

import Metodos.Pagos;
import java.util.Arrays;

public class DebugPagoCorreo {

    public static void main(String[] args) {
        Pagos pagos = new Pagos();
        System.out.println("Iniciando prueba completa de correo PagoFacil...");
        pagos.ejecutar(
                "generar_qr",
                Arrays.asList("7", "10.50", "2026-06-02", "12345678", "70000000", "Mensualidad junio"),
                "jhasmany.fernandez.dev@gmail.com"
        );
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Prueba finalizada.");
    }
}
