package proxy;
import data.ServiceDataInterface;
import database.ServiceDatabaseInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceProxyInterface extends Remote {
    public boolean enregisterServiceDB(ServiceDatabaseInterface s_db) throws RemoteException;

    public boolean enregisterServiceData(ServiceDataInterface s_data) throws RemoteException;

    public ServiceDataInterface getServiceData() throws RemoteException;
    public ServiceDatabaseInterface getServiceDatabase() throws RemoteException;
}
