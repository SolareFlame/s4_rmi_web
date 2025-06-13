package config;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

public class SSLgen {

    /**
     * Génère un SSLContext basé sur un fichier keystore JKS
     *
     * @param keystorePath     Chemin vers le fichier .jks
     * @param keystorePassword Mot de passe du keystore
     * @return SSLContext configuré
     * @throws Exception En cas d'erreur de chargement/configuration
     */
    public static SSLContext createSSLContext(String keystorePath, String keystorePassword) throws Exception {
        char[] passwordArray = keystorePassword.toCharArray();

        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, passwordArray);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, passwordArray);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        return sslContext;
    }
}
