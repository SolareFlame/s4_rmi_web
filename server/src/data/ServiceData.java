package data;

import data.ServiceDataInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        System.out.println("Data requested");

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
