package proxy.handlers;

import com.sun.net.httpserver.HttpExchange;
import database.ServiceDatabaseInterface;
import proxy.ServiceProxyInterface;

import java.io.IOException;

public abstract class Handler {

    protected ServiceProxyInterface s_p;

    public Handler(ServiceProxyInterface s_p) {
        this.s_p = s_p;
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    public abstract void handle(HttpExchange exchange) throws IOException;
}
