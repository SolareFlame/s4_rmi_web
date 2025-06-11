package database;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceDatabaseInterface extends Remote {

    /* Fonction sujet commence ici */

    // /database/restaurants/
    public String consulterToutesDonneesRestoNancy() throws RemoteException;

    public String reserverTable(int numtab, String date, String heure) throws RemoteException, ServeurNonIdentifieException;

}

