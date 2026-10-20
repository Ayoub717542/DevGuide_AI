package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.service.serviceInterface.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.DevTutor.dto.DocumentUploadResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final VectorStore vectorStore;
    private final ConversationRepository conversationRepository;

    @Override
    public DocumentUploadResponse uploadPdf(Long conversationId, MultipartFile file){
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a PDF file.");
        }
        if (conversationRepository.findById(conversationId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        PagePdfDocumentReader reader =
                new PagePdfDocumentReader(file.getResource()); //Gives the reader access to the uploaded file

        List<Document> pages;
        try {
            pages = reader.get(); //Extracts PDF text into Document objects
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not read this PDF. Make sure it is a valid, unencrypted PDF.");
        }

        TokenTextSplitter splitter = //uses token counts to limit passage sizes.
                TokenTextSplitter.builder().build(); //Divides that text into smaller passages, called chunks

        List<Document> chunks = splitter.apply(pages); //divides that text into smaller Spring AI Document objects.

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "No readable text was found in this PDF.");
        }
        String documentId = UUID.randomUUID().toString();

        for (Document chunk : chunks) {
            chunk.getMetadata().put(
                    "conversationId",
                    conversationId.toString()
            );

            chunk.getMetadata().put(
                    "documentId", documentId);

            chunk.getMetadata().put(
                    "filename",
                    file.getOriginalFilename()
            );
        }

        vectorStore.add(chunks); //Generates embeddings and stores the passages

        return new DocumentUploadResponse(
                documentId,
                file.getOriginalFilename(),
                chunks.size()
        );
        //Returns how many chunks were stored
    }
}
