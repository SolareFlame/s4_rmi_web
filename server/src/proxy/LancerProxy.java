package proxy;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

class LancerProxy {
    public static void main(String[] args) throws RemoteException {
        try {
            Registry registry = LocateRegistry.createRegistry(1235);

            ServiceProxy serviceProxy = new ServiceProxy();
            ServiceProxyInterface proxy = (ServiceProxyInterface) UnicastRemoteObject.exportObject(serviceProxy, 0);

            registry.rebind("proxy", proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}