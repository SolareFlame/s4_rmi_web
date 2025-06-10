

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Table {
    private int numtab;
    private int nbplace;

    private Table(int numtab, int nbplace) {
        this.numtab = numtab;
        this.nbplace = nbplace;
    }

    public int getNumtab() {
        return numtab;
    }

    public int getNbplace() {
        return nbplace;
    }

    public static ArrayList<Table> getTables() {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM tables;";
        ArrayList<Table> res = new ArrayList<>();
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            while (rs.next()) {
                res.add(new Table(rs.getInt("numtab"), rs.getInt("nbplace")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean exist(int numtab) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM tables WHERE numtab = ?;";
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
        String request = "SELECT * FROM tables WHERE numtab NOT IN (SELECT numtab FROM reservation WHERE datres = ?);";
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

    public static Table getTable(int numtab) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM tables WHERE numtab = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numtab);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            if (rs.next()) {
                return new Table(rs.getInt("numtab"), rs.getInt("nbplace"));
            } else
                throw new TableInexistanteException();
        } catch (SQLException | TableInexistanteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
