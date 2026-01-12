package il.ac.afeka.cloud.springaidemo;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class CourseTools {

    @Tool(description = "Calculate the final grade for the Cloud Computing course based on exam, project, and homework scores.")
    public String calculateFinalGrade(int exam, int project, int homework) {
        // Weights from syllabus: 60% Exam, 30% Project, 10% Homework
        double finalGrade = (exam * 0.6) + (project * 0.3) + (homework * 0.1);
        // Passing criteria: Minimum 60
        String result = finalGrade >= 60 ? "PASS" : "FAIL";
        return String.format("Final Grade: %.2f. Status: %s", finalGrade, result);
    }
}