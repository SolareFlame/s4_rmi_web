package proxy;

import java.util.HashMap;
import java.util.Map;

public class ApiParser {
    /**
     * @param query the query string to parse, e.g., "key1=value1&key2=value2"
     * @return a map containing the parsed key-value pairs
     */
    public static Map<String, String> parseQuery(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("[&]");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = pair.substring(0, idx);
                    String value = pair.substring(idx + 1);
                    queryParams.put(key, value);
                }
            }
        }
        return queryParams;
    }
}
