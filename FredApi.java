import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FredApi {

    private static final int MAX_ATTEMPTS = 4;

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
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        return sendWithRetry(client, request, seriesId);
    }

    /**
     * Sends the request, retrying when FRED returns a temporary error
     * (HTTP 5xx or 429) or the connection fails or times out.
     *
     * Error messages use only the series ID, never the URL,
     * so the API key can't leak into logs.
     */
    private static String sendWithRetry(
            HttpClient client,
            HttpRequest request,
            String seriesId)
            throws IOException, InterruptedException {

        IOException last = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {

                HttpResponse<String> response =
                        client.send(
                                request,
                                HttpResponse.BodyHandlers.ofString()
                        );

                int code = response.statusCode();

                if (code == 200) {
                    return response.body();
                }

                // Client errors (bad key, bad series ID) won't fix themselves.
                if (code < 500 && code != 429) {
                    throw new IOException(
                            "FRED API returned HTTP " + code
                                    + " for series " + seriesId
                    );
                }

                last = new IOException(
                        "FRED API returned HTTP " + code
                                + " for series " + seriesId
                );

            } catch (java.net.http.HttpTimeoutException e) {

                last = new IOException(
                        "FRED request timed out for series " + seriesId
                );

            } catch (IOException e) {

                // Retry only connection-type problems, not the 4xx thrown above.
                if (e.getMessage() != null
                        && e.getMessage().startsWith("FRED API returned HTTP")) {
                    throw e;
                }

                last = e;
            }

            if (attempt < MAX_ATTEMPTS) {

                System.out.println(
                        "FRED request for " + seriesId + " failed (attempt "
                                + attempt + " of " + MAX_ATTEMPTS + "): "
                                + last.getMessage() + ". Retrying..."
                );

                Thread.sleep(2000L * attempt);
            }
        }

        throw new IOException(
                "FRED failed after " + MAX_ATTEMPTS + " attempts: "
                        + last.getMessage()
        );
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
