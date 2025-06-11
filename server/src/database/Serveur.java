package database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import proxy.ServiceProxyInterface;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

import static proxy.JSONSender.*;

public class Serveur implements ServiceDatabaseInterface, Remote, Serializable {

    private int numserv = -1;
    private String email;
    private String nom;
    private String grade;
    //private int id;
    //private int id_real;

    public Serveur(String email, String mdp) throws ServeurIncorrectException, SQLException {

        String request = "SELECT * FROM serveur WHERE email = ? and passwd = ?;";

        Connection co = DBConnection.getConnection();
        PreparedStatement prep = co.prepareStatement(request);
        prep.setString(1, email);
        prep.setString(2, mdp);
        prep.execute();

        ResultSet rs = prep.getResultSet();
        if (rs.next()) {
            this.email = rs.getString("email");
            this.nom = rs.getString("nomserv");
            this.numserv = rs.getInt("numserv"); //oui en DB c'est id_rea et pas id_real
            this.grade = rs.getString("grade");
            System.out.println("\nConnexion réussie, bienvenue " + this.nom);
        } else
            throw new ServeurIncorrectException();
/*
        // on lance notre service dans notre annuaire
        lancerService();

        // on inscrit notre service au service central
        inscrireService("127.0.0.1", 1235);*/
    }

    /**
     * a. Consulter les table disponibles pour une date et heure données.
     *
     * @param date  la date de la reservation YYYY-MM-DD
     * @param heure l'heure de la reservation JUSTE L'HEURE
     * @return la liste des table disponibles
     */
    public String consulterTable(String date, String heure) throws ServeurNonIdentifieException {
        if (numserv == -1) throw new ServeurNonIdentifieException();

        String dateHeure = date + " " + heure + ":00:00";
        StringBuilder res = new StringBuilder();
        // pas besoin de verrou car on ne fait que lire, pas de risque de conflit
        ResultSet rs = Table.getDispoByDate(date, heure);
        try {
            if (rs.next()) {
                res.append("Tables disponibles le ").append(dateHeure).append(" : \n");
                do {
                    res.append(rs.getInt("numtab")).append("\n");
                } while (rs.next());
                return res.toString();
            } else {
                return "Aucune table disponible";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur de lecture ?");
            return "";
        }
    }

    public String demandeReservationTable(String JSONdata) throws RemoteException, ServeurNonIdentifieException {
        System.out.println("Appel de reserverTable avec les données : " + JSONdata);
        Gson gson = new Gson();

        JsonObject param = gson.fromJson(JSONdata, JsonObject.class); //ApiParser.parseQuery(JSONdata);
        System.out.println("map : " + param);

        int numtab = param.get("numtab").getAsInt();
        String date = param.get("date").getAsString();
        String heure = param.get("heure").getAsString();
        int nbpers = param.get("nbpers").getAsInt();
        String nom = param.get("nom").getAsString();
        String prenom = param.get("prenom").getAsString();
        String telephone = param.get("telephone").getAsString();
        System.out.println("numtab : " + numtab + ", date : " + date + ", heure : " + heure + " nbpers : " + nbpers + ", nom : " + nom + ", prenom : " + prenom + ", telephone : " + telephone);

        // on verifie que le serveur est connecté
        if (numserv == -1) throw new ServeurNonIdentifieException();

        // on verifie que la table existe
        if (!Table.exist(numtab)) {
            System.out.println("La table n'existe pas");
            //return toJsonReservation("ERROR", "La table n'existe pas");
            return toErrorJson("La table n'existe pas", 404);
        }
        System.out.println("  - Table existe");

        // on verifie que la table est disponible
        if (!Table.isDispoByDate(date, heure, numtab)) {
            System.out.println("La table n'est pas disponible pour cette date et heure");
            //return toJsonReservation("ERROR", "La table n'est pas disponible pour cette date et heure");
            return toErrorJson("La table n'est pas disponible pour cette date et heure", 409);
        }
        System.out.println("  - Table est disponible");

        // on verifie que la table puisse accueillir le nombre de personnes
        if (!Table.isBigEnough(numtab, nbpers)) {
            System.out.println("La table n'est pas assez grande");
            //return toJsonReservation("ERROR", "La table n'est pas assez grande");
            return toErrorJson("La table n'est pas assez grande", 403);
        }
        System.out.println("  - Table assez grande");

        if (reserverTable(numtab, date, heure, nbpers, nom, prenom, telephone)) {
            System.out.println("Table réservée avec succès");
            //return toJsonReservation("OK", "Table réservée avec succès");
            return toJson(Map.of(
                    "status", "OK",
                    "details", "Table réservée avec succès",
                    "numtab", numtab,
                    "date", date,
                    "heure", heure,
                    "nbpers", nbpers,
                    "nom", nom,
                    "prenom", prenom,
                    "telephone", telephone,
                    "statusCode", 201
            ));
        } else {
            System.err.println("Erreur lors de la réservation de la table");
            //return toJsonReservation("ERROR", "Erreur lors de la réservation de la table");
            return toErrorJson("Erreur lors de la réservation de la table", 500);
        }
    }

    /**
     * b. Réserver une table pour une date et heure données.
     *
     * @param numtab le numero de la table a reserver
     * @param date   la date de la reservation YYYY-MM-DD
     * @param heure  l'heure de la reservation JUSTE L'HEURE
     */
    public boolean reserverTable(int numtab, String date, String heure, int nbpers, String nom, String prenom, String telephone) throws ServeurNonIdentifieException {
        if (numserv == -1) throw new ServeurNonIdentifieException();
        Connection co = DBConnection.getConnection();

        assert co != null;
        debutTransaction(co); // début de la transaction

        if (Reservation.reserver(co, numtab, date, heure, nbpers, nom, prenom, telephone)) {
            System.out.println("Réservation réussie");
            finTransaction(co); // fin de la transaction
            return true;
        } else {
            System.err.println("Erreur lors de la réservation");
            annulerTransaction(co); // annulation de la transaction
            return false;
        }
    }

    public String toJsonReservation(String statue, String details) {
        return "{ \"status\": \"" + statue + "\", \"details\": \"" + details + "\" }";
    }

    /**
     * c. Consulter les plats disponibles pour une éventuelle commande.
     *
     * @return la liste des plats disponibles
     */
    public String consulterPlatsDispo() {
        if (numserv == -1) return "Veuillez vous connecter";

        // pas besoin de verrou car on ne fait que lire, pas de risque de conflit
        ArrayList<Plat> plats = Plat.getPlatsDispo();
        if (plats.isEmpty()) return "Aucun plat disponible";
        String res = "Plats disponibles : \n";
        for (Plat p : plats) {
            res += p.getNumplat() + " " + p.getLibelle() + " " + p.getType() + " " + p.getPrixunit() + " " + p.getQteservie() + "\n";
        }
        return res;
    }

    /**
     * d. Commander des plats.
     *
     * @param numres  le numero de la reservation
     * @param numplat le numero du plat a commander
     * @param qty     la quantite a commander
     * @return true si la commande a reussi, false sinon
     */
    public boolean commanderPlats(int numres, int numplat, int qty) throws ServeurNonIdentifieException {
        if (numserv == -1) throw new ServeurNonIdentifieException();

        Connection co = DBConnection.getConnection();

        // on met à jour le nombre de plats qu'il reste à servir par jour
        try {
            assert co != null;
            debutTransaction(co); // début de la transaction

            // verrouiller le plat et la quantité
            String r1 = "SELECT * FROM plat WHERE numplat = ? FOR UPDATE;";
            PreparedStatement prep = co.prepareStatement(r1);
            prep.setInt(1, numplat);
            prep.execute();

            if (!Plat.commanderPlat(numplat, qty)) {
                System.err.println("Erreur lors de la commande : la diminution de la quantité de plat n'a pas fonctionné");
                annulerTransaction(co); // annulation de la transaction
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // on ajoute la commande
        String request = "INSERT INTO commande (numres, numplat, quantite) VALUES (?, ?, ?);";
        try {
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numres);
            prep.setInt(2, numplat);
            prep.setInt(3, qty);
            prep.execute();
            finTransaction(co); // fin de la transaction
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la commande : l'ajout de la commande n'a pas fonctionné");
            annulerTransaction(co); // annulation de la transaction
            return false;
        }
    }

    /**
     * a. Consulter les affectations des serveurs
     */
    public String consulterAffectation() throws ServeurNonIdentifieException, ServeurActionNonPermiseException {
        if (numserv == -1) throw new ServeurNonIdentifieException();
        if (grade.equalsIgnoreCase("serveur")) throw new ServeurActionNonPermiseException();

        Connection co = DBConnection.getConnection();
        // pas besoin de verrou car on ne fait que lire, pas de risque de conflit
        String request = "SELECT * FROM affecter";
        String res = "";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            if (rs.next()) {
                res += "Affectations : \n";
                res += rs.getInt("numtab") + " " + rs.getString("dataff") + " " + rs.getInt("numserv") + "\n";
                while (rs.next()) {
                    res += rs.getInt("numtab") + " " + rs.getString("dataff") + " " + rs.getInt("numserv") + "\n";
                }
                return res;
            } else {
                return "Aucune affectation";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur lors de la connexion à la base de données";
        }
    }

    /**
     * b. Affecter des serveurs à des table.
     *
     * @param numServeur le serveur à affecter
     * @param numTable   la table à affecter
     * @param date       la date de l'affectation
     */
    public void affecterServeur(int numServeur, int numTable, String date) throws ServeurNonIdentifieException, ServeurActionNonPermiseException {
        if (numserv == -1) throw new ServeurNonIdentifieException();
        if (grade.equalsIgnoreCase("serveur")) throw new ServeurActionNonPermiseException();

        Connection co = DBConnection.getConnection();
        // pas besoin de verrou ici, l'insertion fait un verrou par elle meme et non utilisons pas de données variables a l'extérieur
        String request = "INSERT INTO affecter (numtab, dataff, numserv) VALUES (?, ?, ?);";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numTable);
            prep.setString(2, date);
            prep.setInt(3, numServeur);
            prep.execute();
            System.out.println("Affectation réussie");
            System.out.printf("Le serveur %d a été affecté à la table %d le %s\n", numServeur, numTable, date);
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affectation");
            e.printStackTrace();
        }
    }

    public static boolean isExist(int numserv) {
        Connection co = DBConnection.getConnection();
        // pas besoin de verrou ici, on ne fait que lire
        String request = "SELECT numserv FROM serveur WHERE numserv = ?;";
        try {
            assert co != null;
            PreparedStatement prep = co.prepareStatement(request);
            prep.setInt(1, numserv);
            prep.execute();
            ResultSet rs = prep.getResultSet();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Serveur : isExist " + e.getMessage());
        }
        return false;
    }

    /**
     * Calculer le montant total d’une réservation consommée (numéro de réservation) et mettre à jour la table RESERVATION pour l’encaissement.
     *
     * @param numRes le numero de la reservation
     * @return true si le montant a ete mis a jour, false sinon
     */
    public boolean calculerMontantPourEncaissement(int numRes) throws ServeurNonIdentifieException, ServeurActionNonPermiseException {
        if (numserv == -1) throw new ServeurNonIdentifieException();
        if (grade.equalsIgnoreCase("serveur")) throw new ServeurActionNonPermiseException();

        Connection co = DBConnection.getConnection();
        // pas besoin de verrou ici, on insert une données dans un champ vide via des prix fixe.
        // justement c'est au autre méthode de verouiller si elle modifie les prix ou la quantité
        Reservation reservation = Reservation.getReservation(numRes);
        if (reservation == null) return false;
        double montant;
        try {
            montant = reservation.calculMontant();
        } catch (ReservationException e) {
            e.printStackTrace();
            return false;
        }
        if (reservation.setMontant(montant)) {
            System.out.printf("Le montant de la réservation %d a été mis à jour à hauteur de %.2f€\n", numRes, montant);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Début de la transaction
     *
     * @param co la connexion
     */
    private void debutTransaction(Connection co) {
        try {
            co.setAutoCommit(false); // début de la transaction
            co.commit(); // par sécurité
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fin de la transaction
     *
     * @param co la connexion
     */
    private void finTransaction(Connection co) {
        try {
            co.commit(); // fin de la transaction
            co.setAutoCommit(true); // fin de la transaction
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Annulation de la transaction
     *
     * @param co la connexion
     */
    private void annulerTransaction(Connection co) {
        try {
            co.rollback();  // annulation de la transaction
            co.setAutoCommit(true); // fin de la transaction
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet d'inscrire notre service à un service central en RMI
     *
     * @param ip   l'adresse IP du service central
     * @param port le port du service central
     * @return true si l'inscription a réussi, false sinon
     */
    public boolean inscrireService(String ip, int port) {
        try {
            // get annuaire
            try {
                Registry reg = LocateRegistry.getRegistry(ip, port);

                String[] services = reg.list();
                for (String service : services) {
                    System.out.println("* " + service);
                }

                ServiceProxyInterface sp = (ServiceProxyInterface) reg.lookup("proxy");
                if (sp.enregisterServiceDB(this))
                    System.out.println("Inscription au service central: succès");
                else
                    System.out.println("Inscription au service central: echec");

            } catch (Error e) {
                System.err.println("erreur");
                e.printStackTrace();
                return false;
            } catch (NotBoundException e) {
                System.err.println("Service non trouvé dans le registre.");
                e.printStackTrace();
                return false;
            } catch (RemoteException e) {
                System.err.println("Erreur de communication RMI.");
                e.printStackTrace();
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'inscription du service : " + e.getMessage());
            return false;
        }
    }

    public void lancerService() {
        try {
            int port = 1098;
            Registry registry = LocateRegistry.createRegistry(port);
            System.out.println("Registre RMI créé sur le port : " + port);

            String nom = "serviceDB";
            ServiceDatabaseInterface dbService = (ServiceDatabaseInterface) UnicastRemoteObject.exportObject(this, 0);
            registry.rebind(nom, dbService);

            String[] services = registry.list();
            for (String s : services) {
                System.out.println("- " + s);
            }

        } catch (Exception e) {
            System.err.println("Erreur serveur RMI : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String transformerJSON(Object o) {
        Gson gson = new Gson();
        return gson.toJson(o);
    }

    @Override
    public String consulterToutesDonneesRestoNancy() throws RemoteException {
        System.out.println("Demande de consultation des données du restaurant Nancy");
        return transformerJSON(Restaurant.getCoordonnees());
    }
}



