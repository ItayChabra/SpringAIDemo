package il.ac.afeka.cloud.springaidemo;

import lombok.Data;
import java.util.List;

@Data
public class CourseInfo {
    private String courseName;
    private String courseId;
    private int credits;
    private String coordinator;
    private List<String> prerequisites;
    private GradingBreakdown grading;

    @Data
    public static class GradingBreakdown {
        private int finalExam;
        private int project;
        private int homework;
    }
}