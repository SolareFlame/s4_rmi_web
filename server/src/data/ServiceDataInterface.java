package data;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceDataInterface extends Remote {

    /**
     * Récupère les données depuis le service externe.
     *
     * @return une chaîne de caractères contenant les données JSON
     * @throws RemoteException si une erreur se produit lors de la récupération des données
     */
    String getData() throws IOException;
}
