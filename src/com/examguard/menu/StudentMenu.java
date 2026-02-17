package com.examguard.menu;

import com.examguard.model.Result;
import com.examguard.service.EvaluationService;
import com.examguard.service.ExamService;

public class StudentMenu {

    public void showMenu(ExamService examService) {

        if (examService.getExam() == null) {
            System.out.println("No exam available.");
            return;
        }

        EvaluationService evaluationService = new EvaluationService();
        Result result = evaluationService.conductExam(
                examService.getExam().getQuestions()
        );

        result.displayResult();
    }
}
