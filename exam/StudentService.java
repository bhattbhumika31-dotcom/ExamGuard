package student;

import java.util.List;
import java.util.Scanner;

import model.Exam;
import model.Question;
import util.FileHandler; 

public class StudentService {

    private FileHandler fileHandler = new FileHandler();

    // start the exam
    public void startExam(String studentId, String examId) {

        Scanner scanner = new Scanner(System.in);

        try {
            // Load exam
            Exam exam = fileHandler.loadExam(examId);

            if (exam == null) {
                System.out.println("Sorry, exam not found!");
                return;
            }

            System.out.println("\nExam started successfully!");
            System.out.println("----------------------------");

            List<Question> questions = exam.getQuestions();

           
            for (Question q : questions) {

                // Show question
                displayQuestion(q);

                // Take answer
                System.out.print("Your answer: ");
                String answer = scanner.nextLine();

                //  (auto-save feature)
                fileHandler.saveAnswer(studentId, examId, q.getqNo(), answer);

                System.out.println("Saved ✔\n");
            }

            // After all questions
            submitExam(studentId, examId);

        } catch (Exception e) {
            System.out.println("Something went wrong during the exam!");
        }
    }

   
    private void displayQuestion(Question question) {

        System.out.println("Q" + question.getqNo() + ": " + question.getQuestionText());

        List<String> options = question.getOptions();
        char option = 'A';

        for (String op : options) {
            System.out.println(option + ") " + op);
            option++;
        }
    }//j

    
    private void submitExam(String studentId, String examId) 
    //.{

        System.out.println("\n===============================");
        System.out.println("   Exam Submitted Successfully ");
        System.out.println("===============================");
        System.out.println("Student ID: " + studentId);
        System.out.println("Exam ID   : " + examId);
        System.out.println("All answers were saved safely!");
    }
}//