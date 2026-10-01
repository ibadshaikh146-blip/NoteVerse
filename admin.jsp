<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Control Center | NoteVerse</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=IBM+Plex+Sans:wght@400;500;600&family=IBM+Plex+Mono:wght@500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
    <style>
        /* NoteVerse Authentic Theme Styling */
        body {
            font-family: 'IBM Plex Sans', sans-serif;
            background-color: #f4f5f0;
            color: #1e293b;
            margin: 0;
            padding: 0;
        }
        /* Match exact header style from site */
        header.site-header {
            background-color: #141c2c;
            color: #ffffff;
            padding: 1.5rem 3rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        header.site-header .logo {
            font-family: 'Fraunces', serif;
            font-size: 1.5rem;
            color: #ffffff;
            text-decoration: none;
            letter-spacing: 0.05em;
        }
        header.site-header nav a {
            color: #cbd5e1;
            text-decoration: none;
            margin-left: 2rem;
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            font-weight: 500;
        }
        header.site-header nav a:hover {
            color: #ffffff;
        }
        .container {
            max-width: 1200px;
            margin: 2.5rem auto;
            padding: 0 1.5rem;
        }
        h1 {
            font-family: 'Fraunces', serif;
            font-size: 2.2rem;
            color: #141c2c;
            margin-bottom: 2rem;
        }
        .card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
            margin-bottom: 2.5rem;
            overflow: hidden;
        }
        .card-header {
            padding: 1.25rem 1.75rem;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #ffffff;
        }
        .card-header h3 {
            margin: 0;
            font-family: 'Fraunces', serif;
            color: #141c2c;
            font-size: 1.35rem;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }
        th, td {
            padding: 1rem 1.75rem;
            border-bottom: 1px solid #f1f5f9;
        }
        th {
            background-color: #f8fafc;
            color: #64748b;
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            font-family: 'IBM Plex Mono', monospace;
        }
        tr:hover {
            background-color: #fafbfc;
        }
        .btn {
            padding: 0.45rem 0.9rem;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 0.75rem;
            font-weight: 600;
            font-family: 'IBM Plex Mono', monospace;
            color: white;
            margin-right: 0.3rem;
            transition: opacity 0.2s;
        }
        .btn-primary { background-color: #2563eb; }
        .btn-success { background-color: #16a34a; }
        .btn-danger { background-color: #dc2626; }
        .btn-warning { background-color: #d97706; }
        .btn:hover { opacity: 0.9; }

        /* Modal Styles */
        .modal {
            display: none;
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(20, 28, 44, 0.75);
            justify-content: center;
            align-items: center;
            z-index: 1000;
        }
        .modal-content {
            background: #ffffff;
            border: 1px solid #cbd5e1;
            width: 80%;
            height: 80%;
            border-radius: 12px;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
        }
        .modal-header {
            padding: 1.25rem 1.75rem;
            background: #141c2c;
            color: white;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .modal-header h3 {
            margin: 0;
            font-family: 'Fraunces', serif;
        }
        .modal-header button {
            background: none;
            border: none;
            color: #ffffff;
            font-size: 1.5rem;
            cursor: pointer;
        }
        .modal-body {
            flex: 1;
            padding: 1rem;
            background: #f1f5f9;
        }
        iframe {
            width: 100%;
            height: 100%;
            border: none;
            border-radius: 6px;
        }
    </style>
</head>
<body>

    <!-- Header matching NoteVerse theme -->
    <header class="site-header">
        <a href="index.html" class="logo">—NoteVerse Admin</a>
        <nav>
            <a href="index.html">Home</a>
            <a href="notes-explorer.html">Browse</a>
            <a href="my-account.html">My Account</a>
        </nav>
    </header>

    <!-- Main Container -->
    <div class="container">
        <h1>Platform Control Center</h1>

        <!-- Pending Notes Section -->
        <div class="card">
            <div class="card-header">
                <h3>Pending Notes Queue</h3>
                <button class="btn btn-primary" onclick="loadPendingNotes()">Refresh</button>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>Title / Details</th>
                        <th>Semester</th>
                        <th>Type</th>
                        <th style="text-align: center;">Actions</th>
                    </tr>
                </thead>
                <tbody id="pendingTableBody">
                    <tr><td colspan="4" style="text-align: center; color: #64748b;">Loading pending notes...</td></tr>
                </tbody>
            </table>
        </div>

        <!-- Student Directory Section -->
        <div class="card">
            <div class="card-header">
                <h3>Registered Students Directory</h3>
                <button class="btn btn-primary" onclick="loadUsers()">Refresh Users</button>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>Full Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th style="text-align: center;">Manage Role / Actions</th>
                    </tr>
                </thead>
                <tbody id="userTableBody">
                    <tr><td colspan="4" style="text-align: center; color: #64748b;">Loading users...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- PDF Preview Modal -->
    <div id="previewModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>PDF Preview</h3>
                <button onclick="closePreview()">&times;</button>
            </div>
            <div class="modal-body">
                <iframe id="pdfViewerFrame"></iframe>
            </div>
        </div>
    </div>

    <!-- JavaScript Logic -->
    <script>
        document.addEventListener("DOMContentLoaded", () => {
            loadPendingNotes();
            loadUsers();
        });

        function loadPendingNotes() {
            fetch('admin/pending-notes')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.getElementById('pendingTableBody');
                    tbody.innerHTML = '';

                    if (data.length === 0) {
                        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #64748b;">No pending notes waiting for approval. Great job!</td></tr>';
                        return;
                    }

                    data.forEach(note => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td>
                                <strong style="color: #141c2c;">${note.title}</strong><br>
                                <small style="color: #646f82;">${note.description || 'No description provided.'}</small>
                            </td>
                            <td>Sem ${note.semester}</td>
                            <td>${note.noteType}</td>
                            <td style="text-align: center;">
                                <button class="btn btn-primary" onclick="previewPdf('${note.filePath}')">Preview</button>
                                <button class="btn btn-success" onclick="moderateNote(${note.noteId}, 'APPROVE')">Approve</button>
                                <button class="btn btn-danger" onclick="moderateNote(${note.noteId}, 'DELETE')">Delete</button>
                            </td>
                        `;
                        tbody.appendChild(tr);
                    });
                })
                .catch(err => {
                    console.error('Error fetching pending notes:', err);
                    document.getElementById('pendingTableBody').innerHTML = '<tr><td colspan="4" style="text-align: center; color: #dc2626;">Failed to load pending notes.</td></tr>';
                });
        }

        function moderateNote(noteId, action) {
            if (!confirm('Are you sure you want to ' + action.toLowerCase() + ' this note?')) return;

            const formData = new URLSearchParams();
            formData.append('noteId', noteId);
            formData.append('action', action);

            fetch('admin/moderate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            })
            .then(res => res.json())
            .then(result => {
                if (result.success) {
                    loadPendingNotes();
                } else {
                    alert('Action failed: ' + result.message);
                }
            })
            .catch(err => alert('Network error occurred.'));
        }

        function loadUsers() {
            fetch('admin/users')
                .then(res => res.json())
                .then(data => {
                    const tbody = document.getElementById('userTableBody');
                    tbody.innerHTML = '';

                    if (data.length === 0) {
                        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #64748b;">No registered users found.</td></tr>';
                        return;
                    }

                    data.forEach(user => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td><strong style="color: #141c2c;">${user.fullName}</strong></td>
                            <td>${user.email}</td>
                            <td>${user.role}</td>
                            <td style="text-align: center;">
                                <button class="btn btn-warning" onclick="toggleRole(${user.userId}, '${user.role}')">Toggle Role</button>
                                <button class="btn btn-danger" onclick="deleteUser(${user.userId})">Remove</button>
                            </td>
                        `;
                        tbody.appendChild(tr);
                    });
                })
                .catch(err => console.error('Error fetching users:', err));
        }

        function toggleRole(userId, currentRole) {
            const newRole = currentRole === 'ADMIN' ? 'STUDENT' : 'ADMIN';
            if (!confirm('Change user role to ' + newRole + '?')) return;

            const formData = new URLSearchParams();
            formData.append('userId', userId);
            formData.append('action', 'UPDATE_ROLE');
            formData.append('role', newRole);

            fetch('admin/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            })
            .then(res => res.json())
            .then(result => {
                if (result.success) loadUsers();
                else alert('Failed to update role.');
            });
        }

        function deleteUser(userId) {
            if (!confirm('Are you sure you want to delete this user account?')) return;

            const formData = new URLSearchParams();
            formData.append('userId', userId);
            formData.append('action', 'DELETE_USER');

            fetch('admin/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            })
            .then(res => res.json())
            .then(result => {
                if (result.save || result.success) loadUsers();
                else alert('Failed to delete user.');
            });
        }

        function previewPdf(filePath) {
            const modal = document.getElementById('previewModal');
            const frame = document.getElementById('pdfViewerFrame');
            frame.src = filePath;
            modal.style.display = 'flex';
        }

        function closePreview() {
            const modal = document.getElementById('previewModal');
            const frame = document.getElementById('pdfViewerFrame');
            frame.src = '';
            modal.style.display = 'none';
        }
    </script>
</body>
</html>