package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Plat {
    private int numplat;
    private String libelle;
    private String type;
    private double prixunit;
    private int qteservie;


    public Plat(int numplat, String libelle, String type, double prixunit, int qteservie) {
        this.numplat = numplat;
        this.libelle = libelle;
        this.type = type;
        this.prixunit = prixunit;
        this.qteservie = qteservie;
    }

    public static ArrayList<Plat> getPlats() {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM plat;";
        ArrayList<Plat> res = new ArrayList<>();
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            while (rs.next()) {
                res.add(new Plat(rs.getInt("numplat"), rs.getString("libelle"), rs.getString("type"), rs.getDouble("prixunit"), rs.getInt("qteservie")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static ArrayList<Plat> getPlatsDispo() {
        Connection co = DBConnection.getConnection();
        String request = "SELECT * FROM plat WHERE qteservie > 0;";
        ArrayList<Plat> res = new ArrayList<>();
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            while (rs.next()) {
                res.add(new Plat(rs.getInt("numplat"), rs.getString("libelle"), rs.getString("type"), rs.getDouble("prixunit"), rs.getInt("qteservie")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean isDispo(int numplat, int qte) {
        ArrayList<Plat> plats = getPlatsDispo();
        boolean dispo = false;

        for (Plat plat : plats) {
            if (plat.numplat == numplat) {
                if (plat.qteservie >= qte) {
                    dispo = true;
                    break;
                } else {
                    if (plat.qteservie == 0) {
                        System.out.println("Le plat " + plat.libelle + " n'est plus disponible.");
                    } else {
                        System.out.println("Il ne reste que " + plat.qteservie + " de " + plat.libelle + ".");
                    }
                }
            }
        }
        return dispo;
    }

    public static boolean isExist(int platEntree) {
        Connection co = DBConnection.getConnection();
        String request = "SELECT numplat FROM plat WHERE numplat = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, platEntree);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean commanderPlat(int numplat, int qte) {
        Connection co = DBConnection.getConnection();
        String request = "UPDATE plat SET qteservie = qteservie - ? WHERE numplat = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, qte);
            prep.setInt(2, numplat);
            prep.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public int getNumplat() {
        return numplat;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getType() {
        return type;
    }

    public double getPrixunit() {
        return prixunit;
    }

    public int getQteservie() {
        return qteservie;
    }


}