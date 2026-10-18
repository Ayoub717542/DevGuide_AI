package com.example.DevTutor.controller;

import com.example.DevTutor.dto.DocumentUploadResponse;
import com.example.DevTutor.service.serviceInterface.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentUploadResponse uploadPdf(
            @RequestParam Long conversationId,
            @RequestParam("file") MultipartFile file) {

        return documentService.uploadPdf(conversationId, file);
    }
}