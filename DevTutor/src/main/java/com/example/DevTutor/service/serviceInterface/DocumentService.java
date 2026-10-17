package com.example.DevTutor.service.serviceInterface;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    //MultipartFile represents a file received through an HTTP upload.
    int uploadPdf(Long conversationId, MultipartFile file);}