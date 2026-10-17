package com.example.DevTutor.controller;

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
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE //the HTTP format used to upload files.
    )
    public Map<String, Integer> uploadPdf(
            @RequestParam("file") MultipartFile file) { //- MultipartFile → gives Java access to that file.

        int chunks = documentService.uploadPdf(file);

        return Map.of("chunksStored", chunks);
    }
}