package com.example.DevTutor.service.serviceInterface;

import org.springframework.web.multipart.MultipartFile;
import com.example.DevTutor.dto.DocumentUploadResponse;

public interface DocumentService {
    //MultipartFile represents a file received through an HTTP upload.
    DocumentUploadResponse  uploadPdf(Long conversationId, MultipartFile file);
}