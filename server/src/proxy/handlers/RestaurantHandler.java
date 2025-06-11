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
public class RestaurantHandler extends Handler {

    public RestaurantHandler(ServiceDatabaseInterface s_db) {
        super(s_db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        Map<String, String> queryParams = ApiParser.parseQuery(exchange.getRequestURI().getQuery());

        //GET /restaurant/{id}/table
        if (parts.length == 4 && "table".equals(parts[3])) {
            if (queryParams.containsKey("id")) {
                String restaurantId = queryParams.get("id");
                //String response = s_db.getAllTables(restaurantId);
                //sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Missing restaurant ID\"}");
            }
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
        }


    }
}
