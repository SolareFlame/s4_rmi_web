package config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JSONSender {
    private static final Gson gson = new Gson();

    /**
     * Forme un objet JSON à partir d'une Map.
     * @param data Map<String, Object> ou Map<String, String>, etc.
     * @return Chaîne JSON correspondante.
     */
    public static String toJson(Object data, int statusCode) {
        return gson.toJson(Map.of(
                "status", statusCode,
                "data", data
        ));
    }

    public static String toJson(String jsonData, int statusCode) {
        JsonObject wrapper = new JsonObject();

        wrapper.addProperty("status", statusCode);
        wrapper.add("data", JsonParser.parseString(jsonData));

        return gson.toJson(wrapper);
    }

    /**
     * Forme un JSON d'erreur sous forme {"error": "..."}.
     * @param message Message d'erreur.
     * @return Chaîne JSON {"error": "..."}
     */
    public static String toErrorJson(String message, int StatusCode) {
        return toJson(Map.of("error", message), StatusCode);
    }

    public static int getJsonStatusCode(String jsonError) {
        try {
            Map<String, Object> errorMap = gson.fromJson(jsonError, Map.class);
            if (errorMap.containsKey("status")) {
                return ((Number) errorMap.get("status")).intValue();
            }
        } catch (Exception e) {
            return 500;
        }
        return 500;
    }

    /**
     * Envoie une chaîne déjà formatée en JSON.
     * @param exchange L'échange HTTP.
     * @param jsonBody Corps JSON (doit être une chaîne JSON valide).
     * @throws IOException
     */
    public static void sendJson(HttpExchange exchange, String jsonBody) throws IOException {
        sendJson(exchange, jsonBody, getJsonStatusCode(jsonBody));
    }

    /**
     * Envoie une chaîne déjà formatée en JSON.
     * @param exchange L'échange HTTP.
     * @param statusCode Code HTTP (200, 404, etc.).
     * @param jsonBody Corps JSON (doit être une chaîne JSON valide).
     * @throws IOException
     */
    public static void sendJson(HttpExchange exchange, String jsonBody, int statusCode) throws IOException {
        byte[] responseBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
