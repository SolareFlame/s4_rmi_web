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
            sendError(exchange, 503, "ServiceDatabase not initialized");
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
                        sendResponse(exchange, 200, getResponse);
                        break;

                    case "POST":
                        System.out.println("POST request to /database/restaurants");
                        String jsonBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        String postResponse = s_p.getServiceDatabase().demandeReservationTable(jsonBody);
                        sendResponse(exchange, 200, postResponse);
                        break;

                    default:
                        sendError(exchange, 405, "Method Not Allowed");
                }
            } catch (ServeurNonIdentifieException e) {
                e.printStackTrace();
                sendError(exchange, 403, "Serveur non identifié : " + e.getMessage());
            } catch (RemoteException e) {
                e.printStackTrace();
                sendError(exchange, 502, "RemoteException: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
            return;
        }

        sendError(exchange, 404, "Not Found");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String errorJson = "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}";
        sendResponse(exchange, statusCode, errorJson);
    }

}
