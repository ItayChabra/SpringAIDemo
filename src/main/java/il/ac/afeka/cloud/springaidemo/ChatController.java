package il.ac.afeka.cloud.springaidemo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final CourseTools courseTools;
    private final ChatModel chatModel;
    @Value("classpath:prompts/course-assistant.st")
    private Resource promptTemplate;

    public ChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, CourseTools courseTools
    , ChatModel chatModel) {
        this.vectorStore = vectorStore;
        this.courseTools = courseTools;
        this.chatModel = chatModel;
        this.chatClient = chatClientBuilder
                .defaultTools(courseTools) // This registers all @Tool methods in the class
                .build();
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
                        "To pass, you need a minimum of 60. The breakdown is: Final %d%%, Project %d%%, Homework %d%%.",
                info.getCourseName(),
                info.getCourseId(),
                info.getCoordinator(),
                info.getGrading().getFinalExam(),
                info.getGrading().getProject(),
                info.getGrading().getHomework()
        );
    }

    @GetMapping("/calculate")
    public String calculate(@RequestParam String query) {
        return chatClient.prompt(query)
                .call()
                .content();
    }

    @GetMapping(value = "/ask-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@RequestParam String query) {
        String context = getContext(query);

        return chatClient.prompt()
                .user(u -> u.text("""
                        Context: {context}
                        Question: {query}
                        """)
                        .param("context", context)
                        .param("query", query))
                .stream()
                .content();
    }

    @GetMapping("/ask-with-template")
    public String askWithTemplate(@RequestParam String query) {
        String context = getContext(query);

        return chatClient.prompt()
                .user(u -> u.text(promptTemplate)
                        .param("courseName", "Cloud Computing")
                        .param("context", context)
                        .param("question", query))
                .call()
                .content();
    }

    @GetMapping("/calculate-optimized")
    public String calculateOptimized(@RequestParam String query) {
        var csvToolProvider = new DelegatorToolCallbackProvider(
                () -> ToolCallbacks.from(courseTools),
                ResponseConverter.Format.CSV
        );

        ChatClient optimizedClient = ChatClient.builder(this.chatModel)
                .defaultToolCallbacks(csvToolProvider.getToolCallbacks())
                .build();

        return optimizedClient.prompt()
                .user(query + "\n\nIMPORTANT: Use the tool to calculate the grade, then explicitly write the result to the user.")
                .call()
                .content();
    }
}
