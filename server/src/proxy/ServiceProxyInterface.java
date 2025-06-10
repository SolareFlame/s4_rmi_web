package proxy;
import data.ServiceDataInterface;

import java.rmi.RemoteException;

public interface ServiceProxyInterface {
    //public void enregisterDB(ServiceDatabaseInterface s_db) throws RemoteException;

    public void enregisterData(ServiceDataInterface s_data) throws RemoteException;
}
