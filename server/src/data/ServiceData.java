package data;

import data.ServiceDataInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.rmi.RemoteException;

import config.ConfigLoader;

public class ServiceData implements ServiceDataInterface {

    private final String apiUrl;

    public ServiceData() {
        ConfigLoader config = new ConfigLoader();
        this.apiUrl = config.get("api.url");
        System.out.println("ServiceData initialized with API URL: " + apiUrl);
    }

    public String getData() throws IOException {
        URL url = new URL(apiUrl);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("www-cache", 3128));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        System.out.println("Data requested");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.err.println("Failed to fetch data from API. Response code: " + responseCode);
            throw new RemoteException("Failed to fetch data from API. Response code: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            System.out.println("Data received: " + jsonBuilder.toString());
            return jsonBuilder.toString();

        } catch (MalformedURLException e) {
            System.err.println("Invalid URL: " + e.getMessage());
            throw new RemoteException("Invalid URL for API: " + apiUrl, e);

        } catch (IOException e) {
            System.err.println("Error reading data from API: " + e.getMessage());
            throw new RemoteException("Error reading data from API", e);
        }
    }
}
