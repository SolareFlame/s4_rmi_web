package proxy;

import com.google.gson.Gson;
import data.ServiceDataInterface;
import database.ServiceDatabaseInterface;

import java.rmi.RemoteException;

public class ServiceProxy implements ServiceProxyInterface {

    private ServiceDatabaseInterface s_db;
    private ServiceDataInterface s_data;

    public boolean enregisterServiceDB(ServiceDatabaseInterface s_db) {
        try {
            this.s_db = s_db;
            System.out.println("Un nouveau service DB s'est connecté");
            /* TEST AVANT CLIENT, A SUPPRIMER ENSUITE */
            Gson gson = new Gson();
            System.out.println(s_db.consulterToutesDonneesRestoNancy());
            //System.out.println(gson.fromJson(s_db.consulterToutesDonneesRestoNancy()));
            return true;
        } catch (Throwable e){
            System.err.println("Un service DB n'a pas put se connecter");
            return false;
        }
    }

    @Override
    public void enregisterData(ServiceDataInterface s_data) throws RemoteException {

    }

    public void enregisterServiceData(ServiceDataInterface s_data) {
        this.s_data = s_data;
    }

    public ServiceDataInterface getServiceData() {
        return s_data;
    }

    public ServiceDatabaseInterface getServiceDatabase() {
        return s_db;
    }
}
