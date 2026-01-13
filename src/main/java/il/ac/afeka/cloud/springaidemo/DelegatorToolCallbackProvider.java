package il.ac.afeka.cloud.springaidemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // This import was likely missing
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Arrays;

public class DelegatorToolCallbackProvider implements ToolCallbackProvider {

    private final ToolCallbackProvider delegate;
    private final ResponseConverter.Format format;

    public DelegatorToolCallbackProvider(ToolCallbackProvider delegate, ResponseConverter.Format format) {
        this.delegate = delegate;
        this.format = format;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return Arrays.stream(this.delegate.getToolCallbacks())
                .map(original -> new DelegatorToolCallback(original, this.format))
                .toArray(ToolCallback[]::new);
    }

    // Inner Class: The actual interceptor
    public static class DelegatorToolCallback implements ToolCallback {
        private final ToolCallback original;
        private final ResponseConverter.Format format;

        // Define the mapper here so we can use it
        private final ObjectMapper jsonMapper = new ObjectMapper();

        public DelegatorToolCallback(ToolCallback original, ResponseConverter.Format format) {
            this.original = original;
            this.format = format;
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return original.getToolDefinition();
        }

        @Override
        public String call(String toolInput) {
            // 1. Run the original tool
            String jsonResult = original.call(toolInput);

            // 2. Convert to target format (Raw CSV)
            String rawCsv = ResponseConverter.convert(jsonResult, this.format);

            // 3. LOGGING (Show the Raw CSV for the Demo)
            System.out.println("\n=================================================");
            System.out.println("OPTIMIZATION DEMO: Flexible Data Formats");
            System.out.println("=================================================");
            System.out.println("BEFORE (JSON): " + jsonResult);
            System.out.println("AFTER  (CSV):  " + rawCsv);
            System.out.println("Size Reduction: " + jsonResult.length() + " chars -> " + rawCsv.length() + " chars");
            System.out.println("=================================================\n");

            // 4. SAFE WRAP (The Fix!)
            // We wrap the CSV string in JSON quotes so the framework doesn't crash.
            try {
                return jsonMapper.writeValueAsString(rawCsv);
            } catch (JsonProcessingException e) {
                return rawCsv; // Fallback
            }
        }
    }
}