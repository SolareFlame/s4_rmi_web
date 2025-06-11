package proxy.routers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.ServiceDatabaseInterface;
import proxy.ServiceProxyInterface;
import java.io.IOException;
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

        System.out.println("DatabaseRouter.handle() - Received request for path: " + path);

        if (s_p.getServiceDatabase() == null) {
            String error = "{\"error\": \"ServiceDatabase not initialized\"}";
            sendResponse(exchange, 503, error);
            return;
        }

        path = path.replaceAll("/+", "/");
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        System.out.println("DatabaseRouter.handle() - Path: " + path);

        if (path.equals("/database/restaurants")) {
            try {
                String response = s_p.getServiceDatabase().consulterToutesDonneesRestoNancy();
                sendResponse(exchange, 200, response);
            } catch (Exception e) {
                String error = "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
                sendResponse(exchange, 500, error);
            }
            return;
        }
        sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
}
