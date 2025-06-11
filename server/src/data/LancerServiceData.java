package data;

import proxy.ServiceProxy;
import proxy.ServiceProxyInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static config.Connect.getReg;

public class LancerServiceData {
    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        String port = "1234";
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

        ServiceProxyInterface proxy = (ServiceProxyInterface) getReg(host).lookup("proxy");
        proxy.enregisterServiceData(serviceDataInterface);
    }
}
