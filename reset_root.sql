-- reset_root.sql
-- Instructions to reset the MySQL `root` password or create a dedicated app user.
-- Run these commands on the database host with appropriate privileges.

-- 1) Diagnostic: check authentication plugin for users
SELECT User, Host, plugin FROM mysql.user;

-- 2) If you can log in as an administrative user, reset root's password (MySQL 5.7+ / 8.0+):
-- This sets the password to the one you provided: 9099891934
ALTER USER 'root'@'localhost' IDENTIFIED BY '9099891934';
FLUSH PRIVILEGES;

-- 3) If you cannot authenticate at all, use the skip-grant-tables method (Windows example):
-- IMPORTANT: this disables authentication temporarily; follow exactly and restart the server afterwards.
-- a) Stop the MySQL service (name may vary):
--   net stop MySQL80
-- b) Start MySQL manually without grants (run from an elevated prompt; adjust paths to your installation):
--   "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.0\my.ini" --skip-grant-tables --skip-networking
-- c) In a NEW elevated shell connect without password:
--   mysql -u root
-- d) Run the reset commands:
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '9099891934';
FLUSH PRIVILEGES;
-- e) Stop the manual mysqld (Ctrl+C in the window you started it) and restart the service normally:
--   net start MySQL80

-- 4) Alternative (create a dedicated application user, recommended):
CREATE DATABASE IF NOT EXISTS noteverse;
CREATE USER 'noteuser'@'localhost' IDENTIFIED BY 'StrongAppPass!';
GRANT ALL PRIVILEGES ON noteverse.* TO 'noteuser'@'localhost';
FLUSH PRIVILEGES;

-- 5) After reset: update your application config file at
-- WEB-INF/classes/db.properties with the new credentials, then test the connection.

-- Security notes:
-- - Do not leave the server running with --skip-grant-tables without --skip-networking.
-- - Prefer a dedicated app user with least privileges rather than using `root`.