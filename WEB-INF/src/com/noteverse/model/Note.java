package com.noteverse.model;

import java.sql.Timestamp;

public class Note {
    private int noteId;
    private String title;
    private String description;
    private int subjectId;
    private String subjectName;
    private int semester;
    private String noteType;
    private String status;
    private String filePath;
    private int uploaderId;
    private String uploaderName; // only populated by admin queries
    private Timestamp uploadDate;
    private int downloadCount;

    public Note() {}

    public int getNoteId() { return noteId; }
    public void setNoteId(int noteId) { this.noteId = noteId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getNoteType() { return noteType; }
    public void setNoteType(String noteType) { this.noteType = noteType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getUploaderId() { return uploaderId; }
    public void setUploaderId(int uploaderId) { this.uploaderId = uploaderId; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public Timestamp getUploadDate() { return uploadDate; }
    public void setUploadDate(Timestamp uploadDate) { this.uploadDate = uploadDate; }

    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }

    @Override
    public String toString() {
        return "Note{id=" + noteId + ", title='" + title + "', status='" + status + "'}";
    }
}
