package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.entity.GeneratedDocument;

public interface GeneratedDocumentService {

    GeneratedDocument createDocument(
            String title,
            String html,
            String format
    );
}