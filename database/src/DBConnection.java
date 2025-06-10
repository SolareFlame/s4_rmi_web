

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Connection;

public class DBConnection {

    // variables de connection
    private static String userName = "root";
    private static String password = "";
    private static String serverName = "127.0.0.1";
    private static String portNumber = "3306";

    private static String dbName = "miaam";
    private static DBConnection dbConnection = null;
    private Connection connect = null;

    private DBConnection() throws SQLException {
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", userName);
            connectionProps.put("password", password);
            connect = DriverManager.getConnection("jdbc:mysql://" + serverName + ":" + portNumber + "/" + dbName, connectionProps);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
        }
    }

    public synchronized static Connection getConnection()  {
        try {
            if (dbConnection == null){
                dbConnection = new DBConnection();
                return dbConnection.connect;
                //Connection connect = DriverManager.getConnection("jdbc:mysql://db4free.net/testpersonne", "scruzlara", "root2014");
            } else {
                return dbConnection.connect;
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Change le nom de la base d'accès.
     * @param db_name Nouveau nom de la base à laquelle on souhaite accéder
     */
    public static void setNomDB(String db_name) throws SQLException {
        dbConnection.connect.close();
        dbName = db_name;
        dbConnection.connect = null;

    }

}