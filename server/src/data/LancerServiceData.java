package data;

import config.ConfigLoader;
import proxy.ServiceProxy;
import proxy.ServiceProxyInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static config.Connect.getReg;

public class LancerServiceData {
    public static void main(String[] args) throws Exception {
        ConfigLoader config = new ConfigLoader();

        String ip = config.get("host");
        String port = config.get("rmi_registry_port");

        System.out.println("Starting RMI lookup on host: " + ip + " and port: " + port);

        String[] host = {ip, port};

        if (args.length > 0)  ip = args[0];
        if (args.length > 1)  port = args[1];

        String[] services = getReg(host).list();
        System.out.println("Services disponibles :");
        for (String service : services) {
            System.out.println(service);
        }

        ServiceData s_data = new ServiceData();
        ServiceDataInterface serviceDataInterface = (ServiceDataInterface) UnicastRemoteObject.exportObject(s_data, 0);

        ServiceProxyInterface proxy = (ServiceProxyInterface) getReg(host).lookup(config.get("rmi_service_name"));
        proxy.enregisterServiceData(serviceDataInterface);
    }
}
