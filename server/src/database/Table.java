package database;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public static ArrayList<Table> getTablesByRestauId(int idRestaurant) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM `table` WHERE id_restau = ?;";
        ArrayList<Table> res = new ArrayList<Table>();
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, idRestaurant);
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
     * Récupère les créneaux horaires disponibles dans une fenêtre de ±1h autour de l'heure demandée
     *
     * @param dateHeure    date et heure souhaitées au format "YYYY-MM-DD HH:MM:SS"
     * @param idRestaurant identifiant du restaurant
     * @return Map<String, ArrayList<Table>> avec les créneaux disponibles et leurs tables
     */
    public static Map<String, ArrayList<Table>> getCreneauxDisponibles(String dateHeure, int idRestaurant) {
        Connection co = DBConnection.getConnection();
        Map<String, ArrayList<Table>> creneauxDisponibles = new HashMap<>();

        try {
            // Parser la date/heure demandée
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date heureDemandee = sdf.parse(dateHeure);
            System.out.println("Date demandée : " + heureDemandee);
            Calendar cal = Calendar.getInstance();
            cal.setTime(heureDemandee);

            // Générer les créneaux de 15 minutes entre -1h et +1h
            Calendar calDebut = (Calendar) cal.clone();
            calDebut.add(Calendar.HOUR, -1);

            Calendar calFin = (Calendar) cal.clone();
            calFin.add(Calendar.HOUR, 1);

            // Tester chaque créneau de 15 minutes
            Calendar calCourant = (Calendar) calDebut.clone();
            while (calCourant.before(calFin) || calCourant.equals(calFin)) {
                String creneauActuel = sdf.format(calCourant.getTime());
                ArrayList<Table> tablesDisponibles = getTablesDispoForCreneau(creneauActuel, idRestaurant, co);

                if (!tablesDisponibles.isEmpty()) {
                    creneauxDisponibles.put(creneauActuel, tablesDisponibles);
                }

                // Avancer de 15 minutes
                calCourant.add(Calendar.MINUTE, 15);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return creneauxDisponibles;
    }

    /**
     * Vérifie si des tables sont disponibles pour un créneau donné
     * Prend en compte les réservations qui pourraient chevaucher (±1h30)
     */
    private static ArrayList<Table> getTablesDispoForCreneau(String creneauHeure, int idRestaurant, Connection co) {
        ArrayList<Table> tablesDisponibles = new ArrayList<>();

        try {
            // Calculer la fenêtre de conflit (±1h30 pour tenir compte de la durée des réservations)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date heureCreneau = sdf.parse(creneauHeure);
            Calendar cal = Calendar.getInstance();
            cal.setTime(heureCreneau);

            // Fenêtre de conflit : 1h30 avant et 1h30 après
            Calendar calAvant = (Calendar) cal.clone();
            calAvant.add(Calendar.MINUTE, -90);
            String heureAvant = sdf.format(calAvant.getTime());

            Calendar calApres = (Calendar) cal.clone();
            calApres.add(Calendar.MINUTE, 90);
            String heureApres = sdf.format(calApres.getTime());

            // Requête pour trouver les tables non réservées dans cette fenêtre
            String request = """
            SELECT t.* FROM `table` t 
            WHERE t.id_restau = ? 
            AND t.numtab NOT IN (
                SELECT r.numtab 
                FROM reservation r 
                WHERE r.datres BETWEEN ? AND ?
            )
        """;

            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, idRestaurant);
            prep.setString(2, heureAvant);
            prep.setString(3, heureApres);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                Table table = new Table(
                        rs.getInt("numtab"),
                        rs.getInt("nbplace"),
                        rs.getInt("id_restau")
                );
                tablesDisponibles.add(table);
            }

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return tablesDisponibles;
    }

    public static ArrayList<String> getHeuresDisponibles(String dateHeure, int idRestaurant) {
        Map<String, ArrayList<Table>> creneaux = getCreneauxDisponibles(dateHeure, idRestaurant);
        return new ArrayList<>(creneaux.keySet());
    }

    /**
     * Vérifie si la date/heure proposée est disponible pour le restaurant
     *
     * @param dateHeure date et heure au format "YYYY-MM-DD HH:MM:SS"
     * @param idRestaurant identifiant du restaurant
     * @return true si au moins une table est disponible à cette date/heure, false sinon
     */
    public static boolean isDateHeureDisponible(String dateHeure, int idRestaurant) {
        Connection co = DBConnection.getConnection();

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date heureDemandee = sdf.parse(dateHeure);
            Calendar cal = Calendar.getInstance();
            cal.setTime(heureDemandee);

            // 1h30 avant et apres
            Calendar calAvant = (Calendar) cal.clone();
            calAvant.add(Calendar.MINUTE, -90);
            String heureAvant = sdf.format(calAvant.getTime());

            Calendar calApres = (Calendar) cal.clone();
            calApres.add(Calendar.MINUTE, 90);
            String heureApres = sdf.format(calApres.getTime());

            // Vérifier s'il existe au moins une table libre dans cette fenêtre
            String request = """
            SELECT COUNT(*) as nb_tables_libres 
            FROM `table` t 
            WHERE t.id_restau = ? 
            AND t.numtab NOT IN (
                SELECT r.numtab 
                FROM reservation r 
                WHERE r.datres BETWEEN ? AND ?
            )
        """;

            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, idRestaurant);
            prep.setString(2, heureAvant);
            prep.setString(3, heureApres);

            ResultSet rs = prep.executeQuery();

            if (rs.next()) {
                int nbTablesLibres = rs.getInt("nb_tables_libres");
                return nbTablesLibres > 0;
            }

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Choisit la meilleure table disponible (la plus petite qui convient au nombre de personnes)
     *
     * @param dateHeure date et heure au format "YYYY-MM-DD HH:MM:SS"
     * @param idRestaurant identifiant du restaurant
     * @param nbPersonnes nombre de personnes pour la réservation
     * @return la Table optimale, ou null si aucune table n'est disponible
     */
    public static Table choisirMeilleureTable(String dateHeure, int idRestaurant, int nbPersonnes) {
        Connection co = DBConnection.getConnection();

        try {
            dateHeure = dateHeure.replace("T", " ") + ":00"; // Assurer le format correct
            // Calculer la fenêtre de conflit (±1h30 pour tenir compte de la durée des réservations)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date heureDemandee = sdf.parse(dateHeure);
            Calendar cal = Calendar.getInstance();
            cal.setTime(heureDemandee);

            // Fenêtre de conflit : 1h30 avant et 1h30 après
            Calendar calAvant = (Calendar) cal.clone();
            calAvant.add(Calendar.MINUTE, -90);
            String heureAvant = sdf.format(calAvant.getTime());

            Calendar calApres = (Calendar) cal.clone();
            calApres.add(Calendar.MINUTE, 90);
            String heureApres = sdf.format(calApres.getTime());

            // Récupérer les tables disponibles, triées par nombre de places croissant
            String request = """
            SELECT t.* 
            FROM `table` t 
            WHERE t.id_restau = ? 
            AND t.nbplace >= ?
            AND t.numtab NOT IN (
                SELECT r.numtab 
                FROM reservation r 
                WHERE r.datres BETWEEN ? AND ?
            )
            ORDER BY t.nbplace ASC
            LIMIT 1
        """;

            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, idRestaurant);
            prep.setInt(2, nbPersonnes);
            prep.setString(3, heureAvant);
            prep.setString(4, heureApres);

            ResultSet rs = prep.executeQuery();

            if (rs.next()) {
                return new Table(
                        rs.getInt("numtab"),
                        rs.getInt("nbplace"),
                        rs.getInt("id_restau")
                );
            }

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}