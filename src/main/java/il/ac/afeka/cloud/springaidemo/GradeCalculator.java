package il.ac.afeka.cloud.springaidemo;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class GradeCalculator {
    public record Request(int examScore, int projectScore, int homeworkScore) {}
    public record Response(double finalGrade, String status) {}

    @Tool(description = "Calculate final grade based on exam, project, and homework scores")
    public Response calculate(Request request) {
        // Logic: 60% Exam, 30% Project, 10% Homework
        double finalGrade = (request.examScore * 0.6) +
                (request.projectScore * 0.3) +
                (request.homeworkScore * 0.1);

        String status = finalGrade >= 60 ? "PASS" : "FAIL";
        return new Response(finalGrade, status);
    }
}
