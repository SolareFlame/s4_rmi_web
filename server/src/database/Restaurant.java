package database;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Restaurant {
    private String ville;
    private String rue;
    private int numeroRue;
    private String nom;

    private Restaurant(String ville, String rue, int numeroRue, String nom) {
        this.ville = ville;
        this.rue = rue;
        this.numeroRue = numeroRue;
        this.nom = nom;
    }

    public static ArrayList<Restaurant> getRestaurants() {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM restaurant;";
        ArrayList<Restaurant> res = new ArrayList<Restaurant>();
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            while (rs.next()) {
                res.add(new Restaurant(rs.getString("nom"), rs.getString("rue"), rs.getInt("numerorue"), rs.getString("ville")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * @return Nom_restaurant:
     * rue
     * numero_rue
     * ville
     */
    public static Map<String, Map<String, String>> getCoordonnees() {
        Connection co = DBConnection.getConnection();
        String request = "SELECT nom, rue, numero_rue, ville FROM restaurant;";

        Map<String, Map<String, String>> restaurants = new LinkedHashMap<>();

        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            while (rs.next()) {
                String nomRestaurant = rs.getString("nom");

                Map<String, String> coordonnees = new LinkedHashMap<String, String>();
                coordonnees.put("rue", rs.getString("rue"));
                coordonnees.put("numero_rue", rs.getString("numero_rue"));
                coordonnees.put("ville", rs.getString("ville"));

                restaurants.put(nomRestaurant, coordonnees);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Conversion en JSON
        return restaurants;
        /*Gson gson = new Gson();
        return gson.toJson(restaurants);*/
    }

    public static boolean exist(int numtab) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM table WHERE numtab = ?;";
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
        String request = "SELECT * FROM table WHERE numtab NOT IN (SELECT numtab FROM reservation WHERE datres = ?);";
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

}
