package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.entity.GeneratedDocument;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.GeneratedDocumentRepository;
import com.example.DevTutor.service.serviceInterface.GeneratedDocumentService;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;

import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GeneratedDocumentServiceImpl implements GeneratedDocumentService {
    private final GeneratedDocumentRepository repository;
    private final ConversationRepository conversationRepository;
    @Override
    public GeneratedDocument createDocument(
            String title,
            String html,
            String format,
            Long conversationId
    ) {

        if (title == null || title.isBlank()
                || title.length() > 255
                || html == null || html.isBlank()
                || format == null || format.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A title of 1–255 characters, HTML and format are required."
            );
        }


        String normalizedFormat = format.trim()
                .toUpperCase(Locale.ROOT);
        html = html.replace("&nbsp;", "&#160;");
        // Escape any '&' that does not start an entity like &amp; or &#160;
        html = html.replaceAll("&(?!#?\\w+;)", "&amp;");
        byte[] data = switch (normalizedFormat) {
            case "PDF" -> generatePdf(html);
            case "DOCX" -> generateWord(html);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF and DOCX are supported."
            );
        };

        GeneratedDocument document = new GeneratedDocument();
        document.setTitle(title);
        document.setFormat(normalizedFormat);
        document.setData(data);
        document.setConversation(conversationRepository.getReferenceById(conversationId));

        return repository.save(document);
    }

    private byte[] generatePdf(String html) {
        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            // Supply the HTML containing content and styling.
            builder.withHtmlContent(html, null);

            // Write the PDF into memory.
            builder.toStream(output);
            builder.run();

            return output.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not convert HTML to PDF.", e
            );
        }
    }

    private byte[] generateWord(String html) {
        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            // Create an empty Word document.
            WordprocessingMLPackage word =
                    WordprocessingMLPackage.createPackage();

            // Convert XHTML into Word elements.
            XHTMLImporterImpl importer =
                    new XHTMLImporterImpl(word);

            word.getMainDocumentPart()
                    .getContent()
                    .addAll(importer.convert(html, null));

            // Write the Word file into memory.
            word.save(output);

            return output.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not convert HTML to Word.", e
            );
        }
    }
}