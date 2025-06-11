package proxy;

import com.google.gson.Gson;
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
    public static String toJson(Map<String, ?> data) {
        return gson.toJson(data);
    }

    /**
     * Forme un JSON d'erreur sous forme {"error": "..."}.
     * @param message Message d'erreur.
     * @return Chaîne JSON {"error": "..."}
     */
    public static String toErrorJson(String message) {
        return gson.toJson(Map.of("error", message));
    }

    /**
     * Envoie une chaîne déjà formatée en JSON.
     * @param exchange L'échange HTTP.
     * @param statusCode Code HTTP (200, 404, etc.).
     * @param jsonBody Corps JSON (doit être une chaîne JSON valide).
     * @throws IOException
     */
    public static void sendJson(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        byte[] responseBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
