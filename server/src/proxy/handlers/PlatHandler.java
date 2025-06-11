package proxy.handlers;

import com.sun.net.httpserver.HttpExchange;
import database.ServiceDatabaseInterface;
import proxy.ApiParser;

import java.io.IOException;
import java.util.Map;

/*
 * API FORMAT
 * GET /restaurant/{id}/table => Get all tables for a restaurant by ID*
 */
public class PlatHandler extends Handler {

    public PlatHandler(ServiceDatabaseInterface s_db) {
        super(s_db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        Map<String, String> queryParams = ApiParser.parseQuery(exchange.getRequestURI().getQuery());

/*        //GET /plats/
        if (parts.length == 3 && "plats".equals(parts[2])) {
            if (queryParams.isEmpty()) {
                String response = s_db.consulterPlatsDispo();
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Invalid query parameters\"}");
            }
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
        }*/
    }
}
