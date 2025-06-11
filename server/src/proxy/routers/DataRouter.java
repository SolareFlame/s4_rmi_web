package proxy.routers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.ServiceDataInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DataRouter implements HttpHandler {

    public ServiceDataInterface s_d;

    public DataRouter(ServiceDataInterface sData) {
        this.s_d = sData;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (s_d == null) {
            String error = "ServiceData not initialized";
            exchange.sendResponseHeaders(503, error.length());
            exchange.getResponseBody().write(error.getBytes());
            exchange.close();
            return;
        }


        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                String data = s_d.getData();
                System.out.println("Data retrieved: " + data);

                byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.flush();
                os.close();
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
                e.printStackTrace();
            } finally {
                exchange.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}
