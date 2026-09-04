package com.noteverse.util;

import com.noteverse.model.Note;
import com.noteverse.model.Subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {

    private static final Pattern FIELD_PATTERN =
        Pattern.compile("\"(\\w+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    public static Map<String, String> parseFlatJson(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null) return result;
        Matcher m = FIELD_PATTERN.matcher(json);
        while (m.find()) {
            String key = m.group(1);
            String value = m.group(2)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
            result.put(key, value);
        }
        return result;
    }

    public static String successResponse(boolean success, String message) {
        String escaped = escape(message);
        return "{\"success\":" + success + ",\"message\":\"" + escaped + "\"}";
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Used by notes-explorer.html — public-facing, no status/uploaderName needed
    public static String notesToJson(List<Note> notes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"noteId\":").append(n.getNoteId()).append(",")
              .append("\"title\":\"").append(escape(n.getTitle())).append("\",")
              .append("\"description\":\"").append(escape(n.getDescription())).append("\",")
              .append("\"subjectId\":").append(n.getSubjectId()).append(",")
              .append("\"subjectName\":\"").append(escape(n.getSubjectName())).append("\",")
              .append("\"semester\":").append(n.getSemester()).append(",")
              .append("\"noteType\":\"").append(escape(n.getNoteType())).append("\",")
              .append("\"filePath\":\"").append(escape(n.getFilePath())).append("\",")
              .append("\"downloadCount\":").append(n.getDownloadCount()).append(",")
              .append("\"uploadDate\":\"").append(n.getUploadDate() != null ? n.getUploadDate().toString() : "").append("\"")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    // Used by admin.html — includes status and who uploaded it
    public static String adminNotesToJson(List<Note> notes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"noteId\":").append(n.getNoteId()).append(",")
              .append("\"title\":\"").append(escape(n.getTitle())).append("\",")
              .append("\"description\":\"").append(escape(n.getDescription())).append("\",")
              .append("\"subjectName\":\"").append(escape(n.getSubjectName())).append("\",")
              .append("\"semester\":").append(n.getSemester()).append(",")
              .append("\"noteType\":\"").append(escape(n.getNoteType())).append("\",")
              .append("\"status\":\"").append(escape(n.getStatus())).append("\",")
              .append("\"filePath\":\"").append(escape(n.getFilePath())).append("\",")
              .append("\"uploaderName\":\"").append(escape(n.getUploaderName())).append("\",")
              .append("\"uploadDate\":\"").append(n.getUploadDate() != null ? n.getUploadDate().toString() : "").append("\"")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String subjectsToJson(List<Subject> subjects) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"subjectId\":").append(s.getSubjectId()).append(",")
              .append("\"subjectName\":\"").append(escape(s.getSubjectName())).append("\",")
              .append("\"semester\":").append(s.getSemester())
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    // Used by my-account.html's "My Uploads" tab — includes status
    // (so the student can see Pending/Approved/Rejected) but not
    // uploaderName, since it's always the current user.
    public static String myUploadsToJson(List<Note> notes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"noteId\":").append(n.getNoteId()).append(",")
              .append("\"title\":\"").append(escape(n.getTitle())).append("\",")
              .append("\"subjectName\":\"").append(escape(n.getSubjectName())).append("\",")
              .append("\"semester\":").append(n.getSemester()).append(",")
              .append("\"noteType\":\"").append(escape(n.getNoteType())).append("\",")
              .append("\"status\":\"").append(escape(n.getStatus())).append("\",")
              .append("\"downloadCount\":").append(n.getDownloadCount()).append(",")
              .append("\"uploadDate\":\"").append(n.getUploadDate() != null ? n.getUploadDate().toString() : "").append("\"")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}