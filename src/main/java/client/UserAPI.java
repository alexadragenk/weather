package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class UserAPI extends BaseSettings {
    public static void main(String[] args) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {

            printLn(writer, "Введите X-Yandex-Weather-Key");
            String wKey = readString(reader);

            String lat = getCoordinate(reader, writer, "lat");

            String lon = getCoordinate(reader, writer, "lon");

            int limit = getLimit(reader, writer);

            String query = "?lat=" + lat + "&lon=" + lon;
            String limits = "&limit=" + limit;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = BaseSettings.createGetRequest(BASE_URL + query, wKey);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            String prettyResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(response.body(), Map.class));

            printLn(writer, "GET Response:");
            printLn(writer, prettyResponse);

            JsonNode responseParsed = mapper.readTree(response.body());
            printLn(writer,"Temperature: " + responseParsed.at("/fact/temp"));

            HttpRequest requestWithLimit = BaseSettings.createGetRequest(BASE_URL + query + limits, wKey);
            HttpResponse<String> responseWithLimit = client.send(requestWithLimit, HttpResponse.BodyHandlers.ofString());

            double sum = averageTemp(limit, mapper.readTree(responseWithLimit.body()));
            printLn(writer,"Average temperature for " + limit + " days: " + sum / limit);
        }
    }

    private static int getLimit(BufferedReader reader, BufferedWriter writer) throws IOException {
        int limit;
        while (true) {
            printLn(writer, "Введите лимит дней (положительное целое число):");
            String limitInput = readString(reader);
            if (isValidInteger(limitInput) && Integer.parseInt(limitInput) > 0) {
                limit = Integer.parseInt(limitInput);
                break;
            } else {
                printLn(writer, "Неверный формат. Пожалуйста, введите положительное целое число для лимита дней.");
            }
        }
        return limit;
    }

    private static String getCoordinate(BufferedReader reader, BufferedWriter writer, String coordinateName) throws IOException {
        String coordinate;
        while (true) {
            printLn(writer, "Введите координату " + coordinateName + ":");
            coordinate = readString(reader);
            if (isValidDouble(coordinate)) {
                break;
            } else {
                printLn(writer, "Неверный формат. Пожалуйста, введите корректное дробное число для " + coordinateName + ".");
            }
        }
        return coordinate;
    }

    private static void printLn(BufferedWriter writer, String str) throws IOException {
        writer.write(str);
        writer.newLine();
        writer.flush();
    }

    private static double averageTemp(int limit, JsonNode node2) {
        double sum = 0;
        for (int i = 0; i < limit; i++) {
            JsonNode temp = node2.at("/forecasts/" + i + "/parts/day_short/temp");
            sum += temp.asInt();
        }
        return sum;
    }
    private static boolean isValidDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private static boolean isValidInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String readString(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}
