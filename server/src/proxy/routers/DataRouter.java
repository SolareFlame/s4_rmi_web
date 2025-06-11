package proxy.routers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import proxy.ServiceProxyInterface;

import java.io.IOException;
import java.rmi.RemoteException;

import static config.JSONSender.*;


public class DataRouter implements HttpHandler {
    private final ServiceProxyInterface s_p;
    private static final Gson gson = new Gson();

    public DataRouter(ServiceProxyInterface proxy) {
        this.s_p = proxy;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        System.out.println("DataRouter:" + path);

        if (s_p.getServiceData() == null) {
            sendJson(exchange, toErrorJson("Service data is not available", 503));
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, toErrorJson("Method Not Allowed: " + exchange.getRequestMethod(), 405));
            return;
        }

        try {
            String data = s_p.getServiceData().getData();
            if (data == null || data.isEmpty()) {
                sendJson(exchange, toErrorJson("Data not found", 404));
            }
            sendJson(exchange, toJson(data, 200));

        } catch (RemoteException e) {
            sendJson(exchange, toErrorJson("Remote service error: " + e.getMessage(),500));
        } catch (Exception e) {
            sendJson(exchange, toErrorJson("Internal server error: " + e.getMessage(),500));
        }
    }
}
