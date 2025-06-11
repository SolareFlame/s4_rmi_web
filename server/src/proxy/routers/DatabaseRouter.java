package proxy.routers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.ServeurNonIdentifieException;
import database.ServiceDatabaseInterface;
import proxy.ServiceProxyInterface;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

import static proxy.JSONSender.*;

public class DatabaseRouter implements HttpHandler {
    private ServiceProxyInterface s_p;

    public DatabaseRouter(ServiceProxyInterface proxy) throws RemoteException {
        this.s_p = proxy;
    }

    /**
     * @param exchange e.g: GET /restaurants/
     * @throws IOException
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (s_p.getServiceDatabase() == null) {
            sendJson(exchange, 503, toErrorJson("Service database is not available"));
            return;
        }

        path = path.replaceAll("/+", "/");
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        System.out.println("DatabaseRouter:" + path);

        if (path.equals("/database/restaurants")) {
            try {
                switch (exchange.getRequestMethod()) {
                    case "GET":
                        System.out.println("GET request to /database/restaurants");
                        String getResponse = s_p.getServiceDatabase().consulterToutesDonneesRestoNancy();
                        sendJson(exchange, 200, getResponse);
                        break;

                    case "POST":
                        System.out.println("POST request to /database/restaurants");
                        String jsonBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        String postResponse = s_p.getServiceDatabase().demandeReservationTable(jsonBody);
                        sendJson(exchange, 200, postResponse);
                        break;

                    default:
                        sendJson(exchange, 405, toErrorJson("Method Not Allowed: " + exchange.getRequestMethod()));
                }
            } catch (ServeurNonIdentifieException e) {
                sendJson(exchange, 503, toErrorJson("Serveur non identifié: " + e.getMessage()));
            } catch (RemoteException e) {
                sendJson(exchange, 500, toErrorJson("Remote service error: " + e.getMessage()));
            } catch (Exception e) {
                sendJson(exchange, 500, toErrorJson("Internal server error: " + e.getMessage()));
            }
            return;
        }
        sendJson(exchange, 404, toErrorJson("Not Found: " + path));
    }
}
