package com.example.DevTutor.controller;

import com.example.DevTutor.entity.GeneratedDocument;
import com.example.DevTutor.repository.GeneratedDocumentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class GeneratedDocumentController {

    private final GeneratedDocumentRepository repository;

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {

        GeneratedDocument document = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Document not found"
                ));

        boolean isPdf = "PDF".equals(document.getFormat());

        String extension = isPdf ? "pdf" : "docx";

        String contentType = isPdf
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument"
                + ".wordprocessingml.document";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"document-"
                                + id + "." + extension + "\""
                )
                .body(document.getData());
    }
}