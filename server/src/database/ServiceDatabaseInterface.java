package database;

public interface ServiceDatabaseInterface {

    /**
     * a. Consulter les table disponibles pour une date et heure données.
     *
     * @param date  la date de la reservation YYYY-MM-DD
     * @param heure l'heure de la reservation JUSTE L'HEURE
     * @return la liste des table disponibles
     */
    public String consulterTable(String date, String heure) throws ServeurNonIdentifieException;

    /**
     * b. Réserver une table pour une date et heure données.
     *
     * @param numtab le numero de la table a reserver
     * @param date   la date de la reservation YYYY-MM-DD
     * @param heure  l'heure de la reservation JUSTE L'HEURE
     */
    public boolean reserverTable(int numtab, String date, String heure) throws ServeurNonIdentifieException;

    /**
     * c. Consulter les plats disponibles pour une éventuelle commande.
     *
     * @return la liste des plats disponibles
     */
    public String consulterPlatsDispo();

    /**
     * d. Commander des plats.
     *
     * @param numres  le numero de la reservation
     * @param numplat le numero du plat a commander
     * @param qty     la quantite a commander
     * @return true si la commande a reussi, false sinon
     */
    public boolean commanderPlats(int numres, int numplat, int qty) throws ServeurNonIdentifieException;

    /**
     * a. Consulter les affectations des serveurs
     */
    public String consulterAffectation() throws ServeurNonIdentifieException, ServeurActionNonPermiseException;

    /**
     * b. Affecter des serveurs à des table.
     *
     * @param numServeur le serveur à affecter
     * @param numTable   la table à affecter
     * @param date       la date de l'affectation
     */
    public void affecterServeur(int numServeur, int numTable, String date) throws ServeurNonIdentifieException, ServeurActionNonPermiseException;

    /**
     * Calculer le montant total d’une réservation consommée (numéro de réservation) et mettre à jour la table RESERVATION pour l’encaissement.
     *
     * @param numRes le numero de la reservation
     * @return true si le montant a ete mis a jour, false sinon
     */
    public boolean calculerMontantPourEncaissement(int numRes) throws ServeurNonIdentifieException, ServeurActionNonPermiseException;

    public String consulterToutesDonneesRestoNancy() throws ServeurNonIdentifieException, ServeurActionNonPermiseException;

}

