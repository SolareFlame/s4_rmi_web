package proxy.routers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.ServiceDatabaseInterface;
import proxy.handlers.RestaurantHandler;
import proxy.handlers.TableHandler;
import java.io.IOException;

public class DatabaseRouter implements HttpHandler {
    private ServiceDatabaseInterface s_db;

    public DatabaseRouter(ServiceDatabaseInterface sDb) {
        this.s_db = sDb;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length < 2) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid request\"}");
            return;
        }
        switch(parts[2]) {
            case "restaurant":
                new RestaurantHandler(s_db).handle(exchange);
                break;
            case "table":
                new TableHandler(s_db).handle(exchange);
                break;
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
}
