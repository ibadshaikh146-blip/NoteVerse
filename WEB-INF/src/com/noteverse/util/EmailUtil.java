package com.noteverse.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sends transactional email through Brevo's REST API over HTTPS (port 443).
 *
 * Why not SMTP / JavaMail? Render's free tier blocks outbound SMTP ports
 * (25, 465, 587), so SMTP connections just time out there. An HTTPS API
 * call is not affected, and needs no extra jar files.
 *
 * Required environment variables:
 *   BREVO_API_KEY      - an API key (starts with "xkeysib-")
 *   MAIL_SENDER_EMAIL  - a sender address you have verified in Brevo
 * Optional:
 *   MAIL_SENDER_NAME   - display name (defaults to "NoteVerse")
 */
public class EmailUtil {

    public static boolean isConfigured() {
        return notBlank(System.getenv("BREVO_API_KEY"))
            && notBlank(System.getenv("MAIL_SENDER_EMAIL"));
    }

    public static void sendEmail(String toEmail, String toName, String subject, String htmlContent)
            throws IOException {

        String apiKey = System.getenv("BREVO_API_KEY");
        String senderEmail = System.getenv("MAIL_SENDER_EMAIL");
        String senderName = System.getenv("MAIL_SENDER_NAME");

        if (!notBlank(apiKey) || !notBlank(senderEmail)) {
            throw new IOException("Email service is not configured (BREVO_API_KEY / MAIL_SENDER_EMAIL).");
        }
        if (!notBlank(senderName)) {
            senderName = "NoteVerse";
        }

        String recipient = "{\"email\":\"" + jsonEscape(toEmail) + "\""
            + (notBlank(toName) ? ",\"name\":\"" + jsonEscape(toName) + "\"" : "")
            + "}";

        String payload = "{"
            + "\"sender\":{\"name\":\"" + jsonEscape(senderName) + "\",\"email\":\"" + jsonEscape(senderEmail) + "\"},"
            + "\"to\":[" + recipient + "],"
            + "\"subject\":\"" + jsonEscape(subject) + "\","
            + "\"htmlContent\":\"" + jsonEscape(htmlContent) + "\""
            + "}";

        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.brevo.com/v3/smtp/email").openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("accept", "application/json");
        conn.setRequestProperty("content-type", "application/json");
        conn.setRequestProperty("api-key", apiKey);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Brevo API error (" + code + "): " + readAll(conn.getErrorStream()));
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        try (InputStream is = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String jsonEscape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
