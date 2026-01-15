package il.ac.afeka.cloud.springaidemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;

public class ResponseConverter {

    public enum Format {
        JSON, CSV, YAML
    }

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final CsvMapper csvMapper = new CsvMapper();
    private static final YAMLMapper yamlMapper = new YAMLMapper();

    public static String convert(String jsonResponse, Format format) {
        try {
            // Parse the input JSON String into a Tree Node
            JsonNode jsonNode = jsonMapper.readTree(jsonResponse);

            // Switch based on the requested format
            switch (format) {
                case CSV:
                    return jsonToCsv(jsonNode);
                case YAML:
                    return jsonToYaml(jsonNode);
                case JSON:
                default:
                    return jsonResponse;
            }
        } catch (IOException e) {
            System.err.println("Conversion Error: " + e.getMessage());
            return jsonResponse; // Fallback to JSON if conversion fails
        }
    }

    private static String jsonToYaml(JsonNode jsonNode) throws IOException {
        return yamlMapper.writeValueAsString(jsonNode);
    }

    private static String jsonToCsv(JsonNode jsonNode) throws IOException {
        // CsvMapper expects an Array (List of Rows).
        // If our tool returned a single Object, we wrap it in an Array.
        if (!jsonNode.isArray()) {
            jsonNode = jsonMapper.createArrayNode().add(jsonNode);
        }

        // Build the CSV Schema (Columns) based on the first item's keys
        JsonNode firstElement = jsonNode.get(0);
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();

        if (firstElement != null && firstElement.isObject()) {
            firstElement.fieldNames().forEachRemaining(schemaBuilder::addColumn);
        }

        // Create the CSV string with a Header row
        CsvSchema schema = schemaBuilder.build().withHeader();
        return csvMapper.writer(schema).writeValueAsString(jsonNode);
    }
}