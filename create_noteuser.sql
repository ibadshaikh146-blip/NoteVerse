-- create_noteuser.sql
-- Creates a dedicated application user for NoteVerse and grants privileges.

CREATE DATABASE IF NOT EXISTS noteverse;
CREATE USER 'noteuser'@'localhost' IDENTIFIED BY 'StrongAppPass!';
GRANT ALL PRIVILEGES ON noteverse.* TO 'noteuser'@'localhost';
FLUSH PRIVILEGES;

-- After running this, update your app config to use noteuser / StrongAppPass!
