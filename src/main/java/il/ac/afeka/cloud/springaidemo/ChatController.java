package il.ac.afeka.cloud.springaidemo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    private String getContext(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(3)
                .build();

        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

        return similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String query) {
        String context = getContext(query); // Using the helper [cite: 66]

        return chatClient.prompt()
                .user(u -> u.text("""
                        You are a course assistant. Answer based ONLY on the context.
                        
                        Context:
                        {context}
                        
                        Question:
                        {query}
                        """)
                        .param("context", context)
                        .param("query", query))
                .call()
                .content();
    }

    @GetMapping("/course-info")
    public CourseInfo getCourseInfo() {
        String context = getContext("course name, ID, coordinator, and grading");

        return chatClient.prompt()
                .user(u -> u.text("""
                        Extract information from the context and return as JSON.
                        Context:
                        {context}
                        """)
                        .param("context", context))
                .call()
                .entity(CourseInfo.class); // Spring AI Structured Output [cite: 31]
    }

    @GetMapping("/course-summary")
    public String getCourseSummary() {

        CourseInfo info = getCourseInfo();

        return String.format(
                "The course '%s' (ID: %s) is led by %s. " +
                        "To pass, you need a minimum of 60. The breakdown is: Final %d%%, Project %d%%.",
                info.getCourseName(),
                info.getCourseId(),
                info.getCoordinator(),
                info.getGrading().getFinalExam(),
                info.getGrading().getProject()
        );
    }
}
