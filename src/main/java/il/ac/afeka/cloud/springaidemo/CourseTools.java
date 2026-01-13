package il.ac.afeka.cloud.springaidemo;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CourseTools {

    @Tool(description = "Calculate the final grade for the Cloud Computing course based on exam, project, and homework scores.")
    /*public String calculateFinalGrade(int exam, int project, int homework) {
        System.out.println("in calculateFinalGrade");
        // Weights from syllabus: 60% Exam, 30% Project, 10% Homework
        double finalGrade = (exam * 0.6) + (project * 0.3) + (homework * 0.1);
        // Passing criteria: Minimum 60
        String result = finalGrade >= 60 ? "PASS" : "FAIL";
        return String.format("Final Grade: %.2f. Status: %s", finalGrade, result);
    }*/
    public Map<String, Object> calculateFinalGrade(int exam, int project, int homework) {
        // Weights
        double examWeight = 0.6;
        double projectWeight = 0.3;
        double homeworkWeight = 0.1;

        double finalGrade = (exam * 0.6) + (project * 0.3) + (homework * 0.1);
        String status = finalGrade >= 60 ? "PASS" : "FAIL";
        // --- THE KEY CHANGE ---
        // Return a Map (JSON Object) instead of a String.
        // This creates the "structure" that CSV can optimize.
        return Map.of(
                "final_grade", finalGrade,
                "student_status", status,
                "breakdown_exam", exam,
                "breakdown_project", project,
                "breakdown_hw", homework
        );
    }
}