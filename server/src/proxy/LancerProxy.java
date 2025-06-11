package proxy;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

class LancerProxy {
    public static void main(String[] args) throws RemoteException {
        try {
            Registry registry = LocateRegistry.createRegistry(1234);

            ServiceProxy serviceProxy = new ServiceProxy();
            ServiceProxyInterface proxy = (ServiceProxyInterface) UnicastRemoteObject.exportObject(serviceProxy, 0);

            registry.rebind("proxy", proxy);

            System.out.println("Proxy started and waiting for connections...");
            Thread.sleep(20000); // Wait for 20 seconds to ensure the proxy is ready
            serviceProxy.startHttpServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}