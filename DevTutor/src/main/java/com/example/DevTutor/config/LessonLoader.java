package com.example.DevTutor.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LessonLoader implements CommandLineRunner {

    private final VectorStore vectorStore;

    @Override
    public void run(String... args) throws Exception {

        String lesson = new ClassPathResource("lessons/java-basics.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        Document document = new Document(lesson);

        vectorStore.add(List.of(document));
    }
}