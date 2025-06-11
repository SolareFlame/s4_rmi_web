package proxy;

import data.ServiceDataInterface;
import database.ServiceDatabaseInterface;

import java.rmi.RemoteException;

public class ServiceProxy implements ServiceProxyInterface {

    private ServiceDatabaseInterface s_db;
    private ServiceDataInterface s_data;

    public void enregisterServiceDB(ServiceDatabaseInterface s_db) {
        this.s_db = s_db;
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
