# ExamGuard

ExamGuard is a Java Swing application for managing and taking exams with separate student and teacher modules.

## Overview

- **Authentication**: Students and teachers log in using MySQL-backed credentials.
- **Student dashboard**: view available exams, attempt exams, resume progress, and see results.
- **Teacher dashboard**: manage exams, create questions, and view analytics.
- **Persistence**: exam/result state is stored locally in `data/` and marks are also saved in the MySQL `marks` table.

## Requirements

- Java JDK 8 or newer
- MySQL server
- `mysql-connector-j-9.7.0.jar` (included under `mysql-connector-j-9.7.0/`)

## Database Setup

1. Start MySQL.
2. Run the SQL script in `schema.sql` to create the required database and tables.
3. Verify that tables `students`, `teachers`, and `marks` exist.

## Configuration

The application currently uses these default database settings in `core/DatabaseAuthenticator.java`:

- URL: `jdbc:mysql://localhost:3306/examguard`
- User: `root`
- Password: `mysql`

If your environment differs, update these values in `core/DatabaseAuthenticator.java` and recompile.

## Running the App

run command:

```powershell
java -cp out;mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar ExamGuardLauncher
```

Compile command:

```powershell
javac -d out -cp "mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar" *.java auth\*.java core\*.java exam\*.java Teacher\teacher\*.java model\*.java
```

Then run the launcher as shown above.

## Project Structure

- `ExamGuardLauncher.java` — application entry point
- `auth/` — login UI and authentication logic
- `core/` — database access, repositories, and persistence hooks
- `exam/` — student exam flow, dashboard, and services
- `Teacher/teacher/` — teacher dashboard and management UI
- `schema.sql` — MySQL schema for `students`, `teachers`, and `marks`
- `mysql-connector-j-9.7.0/` — JDBC driver bundle

