package proxy.handlers;

import com.sun.net.httpserver.HttpExchange;
import database.ServiceDatabaseInterface;

import java.io.IOException;

public abstract class Handler {

    protected ServiceDatabaseInterface s_db;

    public Handler(ServiceDatabaseInterface s_db) {
        this.s_db = s_db;
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    public abstract void handle(HttpExchange exchange) throws IOException;
}
