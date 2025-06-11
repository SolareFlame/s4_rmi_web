package proxy;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import data.ServiceDataInterface;
import database.ServiceDatabaseInterface;
import proxy.routers.DataRouter;
import proxy.routers.DatabaseRouter;

public class ServiceProxy implements ServiceProxyInterface {

    private ServiceDatabaseInterface s_db;
    private ServiceDataInterface s_data;
    private HttpServer server;

    public void enregisterServiceDB(ServiceDatabaseInterface s_db) {
        this.s_db = s_db;
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

