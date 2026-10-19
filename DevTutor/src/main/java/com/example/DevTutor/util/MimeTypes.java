package com.example.DevTutor.util;

import java.util.Locale;
import org.springframework.util.MimeType;

public final class MimeTypes {

    private MimeTypes() {}

    public static boolean isImage(String filename) {
        String name = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        return name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".webp");
    }

    public static MimeType ofFilename(String filename) {
        String name = filename == null
                ? ""
                : filename.toLowerCase(Locale.ROOT);

        if (name.endsWith(".png")) {
            return MimeType.valueOf("image/png");
        }

        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return MimeType.valueOf("image/jpeg");
        }

        if (name.endsWith(".webp")) {
            return MimeType.valueOf("image/webp");
        }

        throw new IllegalArgumentException(
                "Only PNG, JPEG and WebP images are allowed.");
    }
}