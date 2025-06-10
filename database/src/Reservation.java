package activeRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Reservation {
    private int numres;
    private int numtab;
    private String datres;
    private int nbpers;
    private String datpaie;
    private String modpaie;
    private double montant;

    public Reservation(int numres, int numtab, String datres, int nbpers, String datpaie, String modpaie, double montant) {
        this.numres = numres;
        this.numtab = numtab;
        this.datres = datres;
        this.nbpers = nbpers;
        this.datpaie = datpaie;
        this.modpaie = modpaie;
        this.montant = montant;
    }

    public static Reservation getReservation(int numRes) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM reservation WHERE numres = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numRes);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            if (rs.next()) {
                return new Reservation(rs.getInt("numres"), rs.getInt("numtab"), rs.getString("datres"), rs.getInt("nbpers"), rs.getString("datpaie"), rs.getString("modpaie"), rs.getDouble("montcom"));
            } else {
                System.err.println("Reservation : getReservation : numéro " + numRes + " n'existe pas");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.err.println("Reservation : getReservation : numéro" + numRes + " n'existe pas");
        return null;
    }

    public double getMontant() {
        return montant;
    }

    public double calculMontant() throws ReservationException {
        Connection co = DBConnection.getConnection();
        String request = "SELECT SUM(p.prixunit * c.quantite) as montant FROM commande c INNER JOIN plat p ON c.numplat = p.numplat WHERE c.numres = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numres);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            if (rs.next()) {
                return rs.getDouble("montant");
            } else {
                throw new ReservationException("Impossible de calculer le montant de la réservation");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Reservation> getReservations() {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM reservation;";
        ArrayList<Reservation> res = new ArrayList<>();
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            while (rs.next()) {
                res.add(new Reservation(rs.getInt("numres"), rs.getInt("numtab"), rs.getString("datres"), rs.getInt("nbpers"), rs.getString("datpaie"), rs.getString("modpaie"), rs.getDouble("montant")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean isExist(int numres) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT numres FROM reservation WHERE numres = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numres);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Reservation : isExist " + e.getMessage());
        }
        return false;
    }

    public boolean setMontant(double montant) {
        Connection co = DBConnection.getConnection();
        String request = "UPDATE reservation SET montcom = ? WHERE numres = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setDouble(1, montant);
            prep.setInt(2, numres);
            prep.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Reservation : setMontant : impossible de modifier le montant de la réservation");
            return false;
        }
    }

    public static boolean reserver(Connection co, int numtab, String date, String heure) {

        String dateHeure = date + " " + heure + ":00:00";

        String request = "INSERT INTO reservation (numtab, datres) VALUES (?, ?);";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numtab);
            prep.setString(2, dateHeure);
            prep.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
