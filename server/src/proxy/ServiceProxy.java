package proxy;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import data.ServiceDataInterface;
import database.ServiceDatabaseInterface;
import proxy.routers.DataRouter;
import proxy.routers.DatabaseRouter;
import java.rmi.RemoteException;

public class ServiceProxy implements ServiceProxyInterface {

    private ServiceDatabaseInterface s_db;
    private ServiceDataInterface s_data;
    private HttpServer server;

    public boolean enregisterServiceDB(ServiceDatabaseInterface s_db) {
        try {
            this.s_db = s_db;
            System.out.println("Un nouveau service DB s'est connecté");
            /* TEST AVANT CLIENT, A SUPPRIMER ENSUITE */
            Gson gson = new Gson();
            System.out.println(s_db.consulterToutesDonneesRestoNancy());
            //System.out.println(gson.fromJson(s_db.consulterToutesDonneesRestoNancy()));
            return true;
        } catch (Throwable e){
            System.err.println("Un service DB n'a pas put se connecter");
            return false;
        }
    }

    @Override
    public void enregisterServiceData(ServiceDataInterface s_data) {
        this.s_data = s_data;
    }

    public ServiceDataInterface getServiceData() {
        return s_data;
    }

    public ServiceDatabaseInterface getServiceDatabase() {
        return s_db;
    }

    public void startHttpServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);

        // PARTIE DATABASE RMI
        DatabaseRouter router_db = new DatabaseRouter(s_db);
        server.createContext("/database", router_db);

        // PARTIE DATA RMI
        DataRouter router_d = new DataRouter(s_data);
        server.createContext("/data", router_d);

        // PING
        server.createContext("/ping", exchange -> {
            String response = "Pong";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });


        server.start();

        System.out.println("Server started");
    }

    public void stopHttpServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
    }
}
