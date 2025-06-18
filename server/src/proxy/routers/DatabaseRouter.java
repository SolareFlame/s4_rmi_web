package proxy.routers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.ServeurNonIdentifieException;
import proxy.ServiceProxyInterface;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

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
                        // Extraire les paramètres de requête
                        Map<String, Object> queryParams = parseQueryParameters(exchange);
                        System.out.println("Query parameters: " + queryParams);

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

    /**
     * Parse les paramètres de requête avec support des valeurs multiples
     * @param exchange L'échange HTTP
     * @return Map contenant les paramètres avec leurs valeurs (potentiellement multiples)
     */
    private Map<String, Object> parseQueryParameters(HttpExchange exchange) {
        Map<String, Object> parameters = new HashMap<>();

        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getRawQuery();

        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");

            for (String pair : pairs) {
                try {
                    String[] keyValue = pair.split("=", 2);
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                    String value = keyValue.length > 1 ?
                            URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name()) : "";

                    // Gérer les paramètres multiples
                    if (parameters.containsKey(key)) {
                        Object existingValue = parameters.get(key);
                        if (existingValue instanceof java.util.List) {
                            ((java.util.List<String>) existingValue).add(value);
                        } else {
                            java.util.List<String> values = new java.util.ArrayList<>();
                            values.add((String) existingValue);
                            values.add(value);
                            parameters.put(key, values);
                        }
                    } else {
                        parameters.put(key, value);
                    }

                } catch (UnsupportedEncodingException e) {
                    System.err.println("Erreur lors du décodage du paramètre: " + pair);
                }
            }
        }

        return parameters;
    }
}
