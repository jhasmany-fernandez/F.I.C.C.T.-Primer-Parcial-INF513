package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    private static final String DRIVER = "jdbc:postgresql://";
    private final String HOST = getEnv("PROYECTOEMAIL_DB_HOST", "localhost");
    private final String PUERTO = getEnv("PROYECTOEMAIL_DB_PORT", "5432");
    private final String DB = getEnv("PROYECTOEMAIL_DB_NAME", "db_grupo27sa");
    private final String USER = getEnv("PROYECTOEMAIL_DB_USER", "grupo27sa");
    private final String PASSWORD = getEnv("PROYECTOEMAIL_DB_PASSWORD", "");
    
    private static DBConnection instancia;
    private Connection connection;
    
    public DBConnection(){
        this.connection = null;
    }
    
    public Connection conectar() {
        try {
            String url = DRIVER + HOST + ":" + PUERTO + "/" + DB;
            this.connection = DriverManager.getConnection(url, USER, PASSWORD);
            System.out.println("Conexion exitosa");
        } catch (SQLException e) {
            System.out.println("Excepcion al conectar a postgresql DBConnection: " + e);
        }
        return this.connection;
    }
    
    public void desconectar(){
        try{
            this.connection.close();
        }catch(SQLException e){
            System.out.println("Excepcion al deconectar DBConnection: " + e.getMessage());
        }
    }
    
    public static DBConnection getInstance(){
        if(instancia == null){
            instancia = new DBConnection();
            return instancia;
        }
        return instancia;
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
    
     public static void main(String[] args) {
        DBConnection A = new DBConnection();
        A.conectar();
    }
    
   
}
