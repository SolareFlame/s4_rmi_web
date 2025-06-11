package database;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceDatabaseInterface extends Remote {

    /* Fonction sujet commence ici */

    public String consulterToutesDonneesRestoNancy() throws RemoteException;

}

