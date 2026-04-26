# ExamGuard - Online Examination System

A comprehensive Java-based desktop application for managing online exams with role-based access for teachers and students.

## Features

### Authentication
- Secure login/signup system
- Role-based access control (Teacher/Student)
- User credentials stored in database
- Session management

### Teacher Features
- Create and manage exams
- Add questions to exams
- View exam analytics
- Track student performance
- Manage exam status

### Student Features
- View available exams
- Attempt exams
- View results and grades
- Track performance history

## Project Structure

```
ExamGuard/
├── auth/                          # Authentication & Authorization
│   ├── AppTheme.java              # Unified color theme
│   ├── AuthenticationDialog.java   # Login/Signup UI
│   ├── AuthenticationService.java  # Auth logic
│   ├── ExamDatabase.java           # Exam data persistence
│   ├── RoleSelectionDialog.java    # Role selection UI
│   ├── SimpleUserDatabase.java      # User data persistence
│   ├── User.java                   # User model
│   └── UserRole.java               # Role enumeration
├── core/
│   └── ExamGuardRepository.java     # Data repository
├── model/
│   ├── Exam.java                   # Exam entity
│   ├── Question.java               # Question entity
│   └── Result.java                 # Result entity
├── teacher/                        # Teacher module
│   ├── TeacherDashboard.java        # Teacher UI
│   ├── ExamManager.java             # Exam management
│   ├── QuestionManager.java         # Question management
│   ├── AnalyticsDashboard.java      # Analytics UI
│   ├── UITheme.java                 # UI styling
│   └── panels/                      # UI panels
├── exam/                           # Student module
│   ├── StudentDashboard.java        # Student UI
│   └── StudentService.java          # Student business logic
├── ExamGuardLauncher.java          # Application entry point
└── README.md                       # This file
```

## Database Schema

### Users Database (users.db)
```
- user_id (String)
- username (String, UNIQUE)
- password (String)
- full_name (String)
- role (TEACHER/STUDENT)
- is_active (Boolean)
```

### Exams Database (exams.db)
```
- exam_id (String)
- title (String)
- subject (String)
- created_by_teacher_id (String)
- duration_minutes (int)
- total_marks (int)
- questions (List<Question>)
```

### Questions Database (questions.db)
```
- question_id (int)
- exam_id (String)
- question_text (String)
- option_a, option_b, option_c, option_d (String)
- correct_answer (char)
- marks (int)
```

### Results Database (results.db)
```
- result_id (String)
- student_id (String)
- student_name (String)
- exam_id (String)
- exam_title (String)
- marks_obtained (int)
- total_marks (int)
- percentage (double)
- grade (String)
- attempted_at (LocalDateTime)
```

## Running the Application

### Prerequisites
- Java 11 or higher
- No external dependencies required

### Compilation
```bash
javac -d bin auth/*.java core/*.java Teacher/model/*.java Teacher/teacher/*.java exam/*.java ExamGuardLauncher.java
```

### Execution
```bash
java -cp bin ExamGuardLauncher
```

## User Flow

1. **Launch Application** → Role Selection Dialog
2. **Select Role** → Teacher or Student
3. **Authentication** → Login or Create Account
4. **Dashboard** → Role-specific interface
5. **Logout** → Return to role selection

## Default Test Accounts

Create your own account using the signup feature, or use existing credentials stored in the database.

## Grading System

- A: 90-100%
- B: 80-89%
- C: 70-79%
- D: 60-69%
- E: 40-59%
- F: Below 40%

## Color Theme

- Primary Dark: #191E32
- Primary Blue: #2980B9 (Teacher)
- Accent Green: #2ECC71 (Student)
- Accent Red: #DC3545 (Logout)
- Text Primary: White
- Text Secondary: Light Gray

## Data Persistence

All data is automatically persisted to database files:
- `users.db` - User credentials
- `exams.db` - Exam information
- `questions.db` - Question details
- `results.db` - Student results

## Technologies Used

- Java Swing for GUI
- Object serialization for data persistence
- Singleton pattern for repository management
- MVC architecture for separation of concerns

## License

This project is provided as-is for educational purposes.
