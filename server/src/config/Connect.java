package config;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Connect {
    /**
     * @param args IP + HOST (optional)
     * @return REG
     * @throws RemoteException
     */
    public static Registry getReg(String[] args) throws RemoteException {
        if (args.length < 1 || args[0].isEmpty()) {
            System.err.println("Host non fourni");
            System.exit(1);
        }

        String host = args[0];

        String port = "1099";
        if (args.length > 1 && args[1] != null && !args[1].isEmpty()) {
            port = args[1];
        }

        try {
            int portNumber = Integer.parseInt(port);
            Registry reg = LocateRegistry.getRegistry(host, portNumber);
            return reg;
        } catch (NumberFormatException e) {
            System.err.println("Port invalide: " + port);
            System.exit(1);
        }

        return null;
    }
}
