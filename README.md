# NoteVerse Web App — Merge Instructions

This folder is the skeleton Tomcat needs to serve your project as one real website.
Right now it's mostly empty — follow these steps to merge in your existing files.

## Target structure (what you're building toward)

```
noteverse-webapp/                  ← this whole folder gets copied into Tomcat's webapps/
├── index.html                      ← YOUR frontend HTML files go here (loose, at this level)
├── notes.html
├── register.html
├── ... (your other 5 pages)
├── css/                             ← your CSS folder
├── js/                              ← your JS folder
└── WEB-INF/                         ← Tomcat-only folder, browsers can NEVER access this directly
    ├── web.xml                      ← already created for you
    ├── classes/                     ← compiled .java files (as .class) go here
    │   └── com/noteverse/...
    ├── lib/                         ← jar files go here (NOT the top-level lib/ folder anymore)
    │   └── mysql-connector-j-9.x.x.jar
    └── db.properties                ← move your existing db.properties here
```

## Steps to merge

### 1. Copy your frontend files in
From your Desktop frontend folder, copy ALL your .html files, plus your css/ and js/
folders, directly into this `noteverse-webapp` folder (same level as WEB-INF, not inside it).

### 2. Copy your Java source in
From your existing `noteverse/src/com` folder, copy the whole `com` folder into
`WEB-INF/classes/`. So you'll end up with:
`WEB-INF/classes/com/noteverse/db/DBConnection.java` etc.

(Note: eventually these need to be *compiled* .class files in WEB-INF/classes, not
.java source files — we'll handle compiling in the next step when we set up the
VS Code Tomcat extension, which does this automatically on deploy.)

### 3. Copy the jar in
Copy your `mysql-connector-j-x.x.x.jar` from your old `lib/` folder into
`WEB-INF/lib/` in this new structure. Web apps look for jars specifically inside
`WEB-INF/lib`, not a top-level `lib` folder.

### 4. Move db.properties in
Move your `db.properties` file into `WEB-INF/` (update the DBConnection.java code
path if needed — we'll adjust this together once you've done the copy).

### 5. Don't deploy yet
Once you've copied everything in, stop here — don't manually copy this folder into
Tomcat's webapps yet. In the next step we'll set up a VS Code extension that deploys
this properly (compiles your Java automatically and handles reloads), which is much
less error-prone than manual copying every time you make a change.

Let me know once you've done steps 1-4 and I'll walk you through setting up the
VS Code Tomcat extension for deployment.
