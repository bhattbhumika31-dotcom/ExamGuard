package com.examguard.service;

import com.examguard.model.Question;
import com.examguard.model.Result;
import java.util.ArrayList;
import java.util.Scanner;

public class EvaluationService {

    public Result conductExam(ArrayList<Question> questions) {
        Scanner sc = new Scanner(System.in);
        int score = 0;

        for (Question q : questions) {
            System.out.println(q.getQuestionText());
            System.out.println("A. " + q.getOptionA());
            System.out.println("B. " + q.getOptionB());
            System.out.println("C. " + q.getOptionC());
            System.out.println("D. " + q.getOptionD());

            System.out.print("Your Answer: ");
            String ans = sc.nextLine();

            if (ans.equalsIgnoreCase(q.getCorrectAnswer())) {
                score++;
            }
        }

        return new Result(score, questions.size());
    }
}
