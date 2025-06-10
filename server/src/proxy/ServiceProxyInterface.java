package proxy;
import data.ServiceDataInterface;
import database.ServiceDatabaseInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceProxyInterface extends Remote {
    public void enregisterServiceDB(ServiceDatabaseInterface s_db) throws RemoteException;

    public void enregisterServiceData(ServiceDataInterface s_data) throws RemoteException;
}
