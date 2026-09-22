import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EmailSender {

    private static final String RESEND_API_URL =
            "https://api.resend.com/emails";

    public static void sendReport(String htmlReport)
            throws IOException, InterruptedException {

        String apiKey = System.getenv("RESEND_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "RESEND_API_KEY environment variable is not set."
            );
        }

        List<String> recipients = loadRecipients();

        if (recipients.isEmpty()) {
            throw new IllegalStateException(
                    "No email recipients found in EmailRecipients.txt."
            );
        }

        String toJson = buildRecipientsJson(recipients);

        String jsonBody =
                "{"
                + "\"from\":\"Economic Watch <onboarding@resend.dev>\","
                + "\"to\":" + toJson + ","
                + "\"subject\":\"Economic Watch — Columbus, Ohio\","
                + "\"html\":" + jsonString(htmlReport)
                + "}";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESEND_API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        jsonBody,
                        StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "Resend API returned HTTP "
                    + response.statusCode()
                    + ": "
                    + response.body()
            );
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println(" EMAIL SENT SUCCESSFULLY");
        System.out.println("========================================");
        System.out.println("Recipients:");

        for (String recipient : recipients) {
            System.out.println("  " + recipient);
        }

        System.out.println("========================================");
    }

    private static List<String> loadRecipients()
            throws IOException {

        Path file = Path.of("EmailRecipients.txt");

        if (!Files.exists(file)) {
            throw new IOException(
                    "EmailRecipients.txt was not found."
            );
        }

        List<String> recipients = new ArrayList<>();

        for (String line : Files.readAllLines(file)) {

            String email = line.trim();

            if (!email.isEmpty()) {
                recipients.add(email);
            }
        }

        return recipients;
    }

    private static String buildRecipientsJson(
            List<String> recipients) {

        StringBuilder json = new StringBuilder();

        json.append("[");

        for (int i = 0; i < recipients.size(); i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(jsonString(recipients.get(i)));
        }

        json.append("]");

        return json.toString();
    }

    private static String jsonString(String value) {

        return "\""
                + value
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                + "\"";
    }
}