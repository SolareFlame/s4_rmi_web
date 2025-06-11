package database;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Table {
    private int numtab;
    private int nbplace;
    private int idRestaurant;

    public int getIdRestaurant() {
        return idRestaurant;
    }

    private Table(int numtab, int nbplace, int idRestaurant) {
        this.numtab = numtab;
        this.nbplace = nbplace;
        this.idRestaurant = idRestaurant;
    }

    public int getNumtab() {
        return numtab;
    }

    public int getNbplace() {
        return nbplace;
    }

    public static ArrayList<Table> getTables() {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM `table`;";
        ArrayList<Table> res = new ArrayList<Table>();
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            while (rs.next()) {
                res.add(new Table(rs.getInt("numtab"), rs.getInt("nbplace"), rs.getInt("id_restau")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * VERSION API
     *
     * @param nomRestaurant
     * @return table:
     * id
     * nb_places
     */
    public static List<Map<String, String>> getTables(String nomRestaurant) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM `table` where nom_restaurant = ?;";

        //Map<String, Map<String, String>> tables = new LinkedHashMap<>();
        List<Map<String, String>> tables = new ArrayList<>();

        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setString(1, nomRestaurant);
            prep.execute();

            ResultSet rs = prep.getResultSet();

            while (rs.next()) {
                Map<String, String> table = new LinkedHashMap<>();
                table.put("numero", rs.getInt("numtab") + "");
                table.put("nbplace", rs.getInt("nbplace") + "");
                tables.add(table);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
/*        List<Map<String, Object>> tables = getTables();
        String json = new Gson().toJson(tables);*/
        return tables;
    }

    public static boolean exist(int numtab) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM `table` WHERE numtab = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numtab);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ResultSet getDispoByDate(String date, String heure) {
        String dateHeure = date + " " + heure + ":00:00";

        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM `table` WHERE numtab NOT IN (SELECT numtab FROM reservation WHERE datres = ?);";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setString(1, dateHeure);
            prep.execute();
            return prep.getResultSet();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la connexion à la base de données");
            return null;
        }
    }

    public static boolean isDispoByDate(String date, String heure, int numtab) {
        ResultSet res = getDispoByDate(date, heure);
        try {
            while (res.next()) {
                if (res.getInt("numtab") == numtab) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données");
            e.printStackTrace();
        }
        System.err.println("Erreur lors de la connexion à la base de données");
        return false;
    }

    public static boolean isBigEnough(int numtab, int nbPersonnes) {
        Connection co = DBConnection.getConnection();

        String request = "SELECT nbplace FROM `table` WHERE numtab = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numtab);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            if (rs.next()) {
                return rs.getInt("nbplace") >= nbPersonnes;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Table getTable(int numtab) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM `table` WHERE numtab = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numtab);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            if (rs.next()) {
                return new Table(rs.getInt("numtab"), rs.getInt("nbplace"), rs.getInt("id_restau"));
            } else
                throw new TableInexistanteException();
        } catch (SQLException | TableInexistanteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
