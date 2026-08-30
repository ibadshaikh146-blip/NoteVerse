package com.noteverse;

import com.noteverse.dao.NoteDAO;
import com.noteverse.dao.UserDAO;
import com.noteverse.dao.BookmarkDAO;
import com.noteverse.model.Note;
import com.noteverse.model.User;

import java.util.List;

/**
 * Quick manual test harness for Phase 4.
 * Run this after setting up the database and db.properties
 * to confirm your JDBC layer works end-to-end.
 */
public class Main {
    public static void main(String[] args) {
        NoteDAO noteDAO = new NoteDAO();
        UserDAO userDAO = new UserDAO();
        BookmarkDAO bookmarkDAO = new BookmarkDAO();

        System.out.println("=== All Notes ===");
        List<Note> allNotes = noteDAO.getAllNotes();
        allNotes.forEach(System.out::println);

        System.out.println("\n=== Filtered: Semester 2, type NOTES ===");
        List<Note> filtered = noteDAO.getFilteredNotes(null, 2, "NOTES");
        filtered.forEach(System.out::println);

        System.out.println("\n=== User lookup by email ===");
        User user = userDAO.getUserByEmail("ibad@example.com");
        System.out.println(user);

        System.out.println("\n=== Bookmark toggle test ===");
        if (user != null && !allNotes.isEmpty()) {
            int noteId = allNotes.get(0).getNoteId();
            boolean nowBookmarked = bookmarkDAO.toggleBookmark(user.getUserId(), noteId);
            System.out.println("Note " + noteId + " bookmarked? " + nowBookmarked);

            System.out.println("\n=== Bookmarked Notes for user ===");
            bookmarkDAO.getBookmarkedNotes(user.getUserId()).forEach(System.out::println);
        }
    }
}