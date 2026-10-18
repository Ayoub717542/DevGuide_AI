package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.service.serviceInterface.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final VectorStore vectorStore;

    @Override
    public int uploadPdf(Long conversationId, MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a PDF file.");
        }

        PagePdfDocumentReader reader =
                new PagePdfDocumentReader(file.getResource()); //Gives the reader access to the uploaded file

        List<Document> pages = reader.get(); //Extracts PDF text into Document objects

        TokenTextSplitter splitter = //uses token counts to limit passage sizes.
                TokenTextSplitter.builder().build(); //Divides that text into smaller passages, called chunks

        List<Document> chunks = splitter.apply(pages); //divides that text into smaller Spring AI Document objects.

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "No readable text was found in this PDF.");
        }

        for (Document chunk : chunks) {
            chunk.getMetadata().put(
                    "conversationId",
                    conversationId.toString()
            );
            chunk.getMetadata().put(
                    "filename",
                    file.getOriginalFilename()
            );
        }

        vectorStore.add(chunks);

        vectorStore.add(chunks); //Generates embeddings and stores the passages

        return chunks.size(); //Returns how many chunks were stored
    }
}
