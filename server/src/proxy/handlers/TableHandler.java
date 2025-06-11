package proxy.handlers;

import com.sun.net.httpserver.HttpExchange;
import database.ServiceDatabaseInterface;

import java.io.IOException;

/*
 * API FORMAT
 * GET /table/{id} => Get table data by ID
 * GET /table/{id} (queryParam: date, heure) => Get tables available for a specific date and time
 *
 * POST /api/table/{id} (queryParam: date, heure) => Reserve a table by ID for a specific date and time
 */
public class TableHandler extends Handler {

    public TableHandler(ServiceDatabaseInterface s_db) {
        super(s_db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {


    }
}
