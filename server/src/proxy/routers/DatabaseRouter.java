package proxy.routers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.ServeurNonIdentifieException;
import proxy.ServiceProxyInterface;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

import static config.JSONSender.*;

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
            sendJson(exchange, toErrorJson("Service database is not available", 503));
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
                        sendJson(exchange, getResponse);
                        break;

                    case "POST":
                        System.out.println("POST request to /database/restaurants");
//                        System.out.println("Request body: " + new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

                        String jsonBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        System.out.println("Parsed JSON body: " + jsonBody);
                        if (jsonBody.isEmpty()) {
                            sendJson(exchange, toErrorJson("Empty request body", 400));
                            return;
                        }
                        String postResponse = s_p.getServiceDatabase().demandeReservationTable(jsonBody);
                        sendJson(exchange, postResponse);
                        break;

                    default:
                        sendJson(exchange, toErrorJson("Method Not Allowed: " + exchange.getRequestMethod(), 405));
                }
            } catch (ServeurNonIdentifieException e) {
                sendJson(exchange, toErrorJson("ServiceDatabase non identifié: " + e.getMessage(), 503));
            } catch (RemoteException e) {
                sendJson(exchange, toErrorJson("Remote service error: " + e.getMessage(), 500));
            } catch (Exception e) {
                sendJson(exchange, toErrorJson("Internal server error: " + e.getMessage(), 500));
            }
            return;
        }
        sendJson(exchange, toErrorJson("Not Found: " + path, 404));
    }
}
