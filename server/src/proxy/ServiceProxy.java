package proxy;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import config.ConfigLoader;
import config.SSLgen;
import data.ServiceDataInterface;
import database.ServiceDatabaseInterface;
import proxy.routers.DataRouter;
import proxy.routers.DatabaseRouter;

import javax.net.ssl.SSLContext;
import java.rmi.RemoteException;

import static config.SSLgen.createSSLContext;

public class ServiceProxy implements ServiceProxyInterface {

    private ServiceDatabaseInterface s_db;
    private ServiceDataInterface s_data;
    private HttpsServer server;

    public synchronized boolean enregisterServiceDB(ServiceDatabaseInterface s_db) {
        try {
            this.s_db = s_db;
            System.out.println("Un nouveau service DB s'est connecté");
            return true;
        } catch (Throwable e){
            System.err.println("Un service DB n'a pas put se connecter");
            return false;
        }
    }

    @Override
    public synchronized boolean enregisterServiceData(ServiceDataInterface s_data) {
        try {
            this.s_data = s_data;
            System.out.println("Un nouveau service Data s'est connecté");
            return true;
        } catch (Throwable e){
            System.err.println("Un service Data n'a pas put se connecter");
            return false;
        }
    }

    public synchronized ServiceDataInterface getServiceData() {
        return s_data;
    }

    public synchronized ServiceDatabaseInterface getServiceDatabase() {
        return s_db;
    }

    public void startHttpServer() throws Exception {
        ConfigLoader config = new ConfigLoader();
        int web_port = Integer.parseInt(config.get("port"));
        System.out.println("Starting HTTP server on port: " + web_port);

        // Configuration SSL
        SSLContext sslContext = createSSLContext(config.get("keystore_path"), config.get("keystore_password"));

        this.server = HttpsServer.create(new InetSocketAddress(web_port), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));

        // PARTIE DATABASE RMI
        DatabaseRouter router_db = new DatabaseRouter(this);
        server.createContext("/database", router_db);

        // PARTIE DATA RMI
        DataRouter router_d = new DataRouter(this);
        server.createContext("/data", router_d);

        // PING
        server.createContext("/ping", exchange -> {
            String response = "Pong";

            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            byte[] bytes = response.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
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
