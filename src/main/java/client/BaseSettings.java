package client;

import java.net.URI;
import java.net.http.HttpRequest;

public class BaseSettings {
    public static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static HttpRequest createGetRequest(String endpoint, String wKey) {
        return HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .headers(
                        "Content-Type", "application/json",
                        "X-Yandex-Weather-Key", wKey
                )
                .GET()
                .build();
    }
}
