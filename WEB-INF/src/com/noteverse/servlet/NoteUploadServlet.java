package com.noteverse.servlet;

import com.noteverse.dao.NoteDAO;
import com.noteverse.model.Note;
import com.noteverse.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebServlet("/upload")
@MultipartConfig(
    maxFileSize = 20L * 1024 * 1024,      // 20MB max file size
    maxRequestSize = 25L * 1024 * 1024
)
public class NoteUploadServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            respond(response, 401, false, "You must be logged in to upload a note.");
            return;
        }
        int uploaderId = (int) session.getAttribute("userId");

        String title = request.getParameter("noteTitle");
        String semesterStr = request.getParameter("semester");
        String subjectIdStr = request.getParameter("subject");
        String resourceType = request.getParameter("resourceType");
        String description = request.getParameter("noteDescription");

        if (title == null || title.trim().isEmpty()) {
            respond(response, 400, false, "Please enter a title.");
            return;
        }

        int semester, subjectId;
        try {
            semester = Integer.parseInt(semesterStr);
            subjectId = Integer.parseInt(subjectIdStr);
        } catch (NumberFormatException | NullPointerException e) {
            respond(response, 400, false, "Please select a semester and subject.");
            return;
        }

        Part filePart = request.getPart("noteFile");
        if (filePart == null || filePart.getSize() == 0) {
            respond(response, 400, false, "Please choose a PDF to upload.");
            return;
        }

        String fileUrl;
        try {
            // Parse CLOUDINARY_URL format: cloudinary://api_key:api_secret@cloud_name
            String cloudinaryEnv = System.getenv("CLOUDINARY_URL");
            if (cloudinaryEnv == null || !cloudinaryEnv.startsWith("cloudinary://")) {
                throw new RuntimeException("CLOUDINARY_URL environment variable is not configured correctly.");
            }

            String withoutScheme = cloudinaryEnv.substring("cloudinary://".length());
            String[] parts = withoutScheme.split("@");
            String[] credentials = parts[0].split(":");
            String apiKey = credentials[0];
            String apiSecret = credentials[1];
            String cloudName = parts[1];

            // Upload to Cloudinary REST API using basic authentication
            fileUrl = uploadToCloudinary(filePart, cloudName, apiKey, apiSecret);

        } catch (Exception e) {
            e.printStackTrace();
            respond(response, 500, false, "Cloud upload failed: " + e.getMessage());
            return;
        }

        // Save note record into Aiven MySQL database
        Note note = new Note();
        note.setTitle(title.trim());
        note.setDescription(description != null ? description.trim() : "");
        note.setSubjectId(subjectId);
        note.setSemester(semester);
        note.setNoteType(resourceType);
        note.setFilePath(fileUrl);
        note.setUploaderId(uploaderId);

        boolean saved = noteDAO.addNote(note);

        if (saved) {
            respond(response, 200, true, "Uploaded! Your note is pending admin review.");
        } else {
            respond(response, 500, false, "Something went wrong saving your upload details.");
        }
    }

    private String uploadToCloudinary(Part filePart, String cloudName, String apiKey, String apiSecret) throws IOException {
        String boundary = "===" + System.currentTimeMillis() + "===";
        URL url = new URL("https://api.cloudinary.com/v1_1/" + cloudName + "/auto/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        String authString = apiKey + ":" + apiSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        try (OutputStream outputStream = conn.getOutputStream()) {
            // Write multipart header safely using bytes
            String header = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + filePart.getSubmittedFileName() + "\"\r\n" +
                    "Content-Type: application/pdf\r\n\r\n";
            outputStream.write(header.getBytes(StandardCharsets.UTF_8));

            // Write file stream bytes directly
            try (InputStream inputStream = filePart.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // Write closing boundary
            String footer = "\r\n--" + boundary + "--\r\n";
            outputStream.write(footer.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new IOException("Cloudinary error (" + responseCode + "): " + errorResponse.toString());
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder jsonResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }
            
            String json = jsonResponse.toString();
            String searchKey = "\"secure_url\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                return json.substring(startIndex, endIndex).replace("\\/", "/");
            } else {
                throw new IOException("Could not parse secure_url from Cloudinary response.");
            }
        }
    }

    private void respond(HttpServletResponse response, int statusCode, boolean success, String message)
            throws IOException {
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.successResponse(success, message));
        }
    }
}