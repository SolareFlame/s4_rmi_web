package config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Fichier de configuration introuvable: " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement de la configuration", e);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}
