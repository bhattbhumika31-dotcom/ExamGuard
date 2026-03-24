# ExamGuard

ExamGuard is a Java Swing based exam management system with separate teacher and student modules.

The project lets teachers create exams, add MCQ questions, publish exams, and review student performance. Students can view available exams, attempt active exams, and see their results.

## Features

- Teacher dashboard for exam management
- Question management for each exam
- Student dashboard for viewing and attempting exams
- Analytics and result tracking
- Shared repository so teacher and student modules work on the same data
- Local persistence using serialized data files

## Project Structure

```text
ExamGuard/
├── core/
│   └── ExamGuardRepository.java
├── exam/
│   ├── StudentDashboard.java
│   ├── StudentMenu.java
│   └── StudentService.java
├── Teacher/
│   ├── model/
│   │   ├── Exam.java
│   │   ├── Question.java
│   │   └── Result.java
│   └── teacher/
│       ├── TeacherDashboard.java
│       ├── ExamManager.java
│       ├── QuestionManager.java
│       └── panels/
├── data/
│   ├── exams.ser
│   └── results.ser
└── ExamGuardLauncher.java
```

## Requirements

- Java 17 or later recommended
- Windows PowerShell for the commands below

## Compile

Run this from the project root:

```powershell
javac -d out_all ExamGuardLauncher.java core\*.java exam\*.java Teacher\model\*.java Teacher\teacher\*.java Teacher\teacher\panels\*.java
```

## Run

### Run both teacher and student modules

```powershell
java -cp out_all ExamGuardLauncher
```

### Run only the teacher panel

```powershell
java -cp out_all teacher.TeacherDashboard
```

### Run only the student panel

```powershell
java -cp out_all StudentDashboard
```

## How It Works

### Teacher module

- Create an exam
- Add questions to the exam
- Toggle the exam status to `ACTIVE`
- Review results in the analytics section

### Student module

- View active exams
- Attempt an exam
- Submit answers
- View saved results

## Shared Data

The teacher and student modules are integrated through:

- `core/ExamGuardRepository.java`
- `data/exams.ser`
- `data/results.ser`

Teacher actions update the shared exam store, and student submissions update the shared results store.

## Typical Workflow

1. Open the teacher panel.
2. Create an exam.
3. Add questions.
4. Make the exam `ACTIVE`.
5. Open the student panel.
6. Refresh the student dashboard.
7. Attempt the exam.
8. Go back to the teacher analytics panel to review the result.

## Notes

- Students only see exams that are marked `ACTIVE`.
- Results are stored locally in the `data` folder.
- If you change the code, recompile before running again.

## Future Improvements

- Authentication for teacher and student login
- Timer support during exam attempts
- Better result history and student profiles
- Database-backed storage instead of serialized files
- Export/import support

## Author

Built as part of the ExamGuard Java project.
