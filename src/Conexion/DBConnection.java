package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    private static final String DRIVER = "jdbc:postgresql://";
    private final String HOST = "localhost";
    private final String PUERTO = "5432";
    private final String DB = "tecnito";
    private final String USER = "postgres";
    private final String PASSWORD = "toor";
    
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
    
     public static void main(String[] args) {
        DBConnection A = new DBConnection();
        A.conectar();
    }
    
   
}
