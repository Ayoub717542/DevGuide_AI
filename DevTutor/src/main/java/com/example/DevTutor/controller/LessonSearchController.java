package com.example.DevTutor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonSearchController {

    private final VectorStore vectorStore;

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public List<String> search(@RequestParam String question) {

        List<Document> results =
                vectorStore.similaritySearch(question);

        return results.stream()
                .map(Document::getText)
                .toList();
    }
}