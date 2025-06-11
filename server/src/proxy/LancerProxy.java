package proxy;

import config.ConfigLoader;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

class LancerProxy {
    public static void main(String[] args) throws RemoteException {
        try {
            ConfigLoader config = new ConfigLoader();
            int rmi_port = Integer.parseInt(config.get("rmi_registry_port"));
            String rmi_service_name = config.get("rmi_service_name");

            System.out.println("Starting RMI registry on port: " + rmi_port);
            System.out.println("Service name: " + rmi_service_name);

            Registry registry = LocateRegistry.createRegistry(rmi_port);

            ServiceProxy serviceProxy = new ServiceProxy();
            ServiceProxyInterface proxy = (ServiceProxyInterface) UnicastRemoteObject.exportObject(serviceProxy, 0);

            registry.rebind(rmi_service_name, proxy);

            serviceProxy.startHttpServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}