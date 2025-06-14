package database;

import config.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Connection;

public class DBConnection {

    // variables de connectiond
    private static final ConfigLoader config = new ConfigLoader();
    private static final String userName = config.get("usernameDB");
    private static final String password = config.get("passwordDB");
    private static final String serverName = config.get("serverDB");
    private static final String portNumber = config.get("portDB");

    private static String dbName = config.get("nameDB");
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