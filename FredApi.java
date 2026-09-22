import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FredApi {

    public static String getSeries(String seriesId)
            throws IOException, InterruptedException {

        String apiKey = System.getenv("FRED_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "FRED_API_KEY environment variable is not set."
            );
        }

        String url = "https://api.stlouisfed.org/fred/series/observations"
                + "?series_id=" + seriesId
                + "&api_key=" + apiKey
                + "&file_type=json"
                + "&sort_order=desc"
                + "&limit=1";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(
                    "FRED API returned HTTP " + response.statusCode()
            );
        }

        return response.body();
    }

    public static double getLatestValue(String seriesId)
            throws IOException, InterruptedException {

        String json = getSeries(seriesId);

        Pattern pattern = Pattern.compile(
                "\"value\":\"([^\"]+)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IOException(
                    "Could not find a value in FRED response."
            );
        }

        String value = matcher.group(1);

        if (value.equals(".") || value.equals("..")) {
            throw new IOException(
                    "FRED returned a missing value."
            );
        }

        return Double.parseDouble(value);
    }
}