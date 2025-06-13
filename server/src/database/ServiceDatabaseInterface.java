package database;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceDatabaseInterface extends Remote {

    /* Fonction sujet commence ici */

    // /database/restaurants/
    public String consulterToutesDonneesRestoNancy() throws RemoteException;

    public String demandeReservationTable(String JSONdata) throws RemoteException, ServeurNonIdentifieException;

    public String consulterTablesDisponibles(String JSONdata) throws RemoteException, ServeurNonIdentifieException;

    //public String consulterPlatsRestaurant(String JSONdata) throws RemoteException, ServeurNonIdentifieException;

    //public String demandeCommandePlat(String JSONdata) throws RemoteException, ServeurNonIdentifieException;

}

