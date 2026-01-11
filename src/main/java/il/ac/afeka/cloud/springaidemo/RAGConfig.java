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

    @Value("classpath:syllabus.txt")
    private Resource syllabus;

    // Path for local persistence of the vector index
    @Value("${app.vectorstore.path:vectorstore.json}")
    private String vectorStorePath;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();

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
                if (storeFile.exists()) {
                    logger.info("Skipping ingestion as vector store was loaded from file.");
                    return;
                }

                logger.info("Starting ingestion pipeline...");

                // 1. Read
                if (!syllabus.exists()) {
                    logger.severe("syllabus.txt not found in classpath!");
                    return;
                }
                TextReader reader = new TextReader(syllabus);
                List<Document> documents = reader.get();
                logger.info("Read " + documents.size() + " documents from syllabus.");

                // 2. Split
                TokenTextSplitter splitter = new TokenTextSplitter();
                List<Document> splitDocs = splitter.apply(documents);
                logger.info("Split documents into " + splitDocs.size() + " chunks.");

                // 3. Store (Embedding happens here)
                logger.info("Embedding and storing chunks (this may take time)...");
                vectorStore.add(splitDocs);
                logger.info("INGESTION SUCCESS: Syllabus loaded into memory!");

                // 4. Persist
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