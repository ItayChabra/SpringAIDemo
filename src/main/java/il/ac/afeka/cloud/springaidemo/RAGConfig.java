package il.ac.afeka.cloud.springaidemo;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

@Configuration
public class RAGConfig {

    private static final Logger logger = Logger.getLogger(RAGConfig.class.getName());

    // 1. Inject Syllabus
    @Value("classpath:syllabus.txt")
    private Resource syllabusResource;

    // 2. Inject Harry Potter
    @Value("classpath:harry_potter.txt")
    private Resource harryPotterResource;

    @Value("${app.vectorstore.path:vectorstore.json}")
    private String vectorStorePath;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File storeFile = new File(vectorStorePath);

        if (storeFile.exists()) {
            logger.info("Loading existing vector store from: " + storeFile.getAbsolutePath());
            simpleVectorStore.load(storeFile);
        } else {
            logger.info("No existing vector store found. A new one will be created.");
        }
        return simpleVectorStore;
    }

    @Bean
    public CommandLineRunner ingestData(VectorStore vectorStore) {
        return args -> {
            try {
                File storeFile = new File(vectorStorePath);

                // Check if store already exists
                if (storeFile.exists()) {
                    logger.info("Skipping ingestion as vector store was loaded from file.");
                    return;
                }

                logger.info("Starting DUAL ingestion pipeline (Syllabus + Harry Potter)...");

                // --- PROCESS 1: SYLLABUS ---
                if (syllabusResource.exists()) {
                    logger.info("Processing Syllabus...");
                    TextReader reader = new TextReader(syllabusResource);
                    List<Document> docs = reader.get();

                    // Use standard splitting for syllabus
                    TokenTextSplitter splitter = new TokenTextSplitter();
                    vectorStore.add(splitter.apply(docs));
                    logger.info("Syllabus added!");
                } else {
                    logger.warning("syllabus.txt missing!");
                }

                // --- PROCESS 2: HARRY POTTER ---
                if (harryPotterResource.exists()) {
                    logger.info("Processing Harry Potter...");
                    TextReader reader = new TextReader(harryPotterResource);
                    List<Document> docs = reader.get();

                    // Use specialized splitting for the book (smaller chunks)
                    TokenTextSplitter bookSplitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
                    vectorStore.add(bookSplitter.apply(docs));
                    logger.info("Harry Potter added!");
                } else {
                    logger.warning("harry_potter.txt missing!");
                }

                // --- SAVE ---
                if (vectorStore instanceof SimpleVectorStore) {
                    ((SimpleVectorStore) vectorStore).save(storeFile);
                    logger.info("Vector store persisted to: " + storeFile.getAbsolutePath());
                }

            } catch (Exception e) {
                logger.severe("ERROR: Ingestion pipeline failed: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}