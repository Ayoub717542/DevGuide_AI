package com.example.DevTutor.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
@Converter
public class GitHubTokenConverter implements AttributeConverter<String, String> {

    private final TextEncryptor encryptor;

    public GitHubTokenConverter(@Value("${app.github-token.enc-password}") String password,
                                @Value("${app.github-token.enc-salt}") String salt) {
        this.encryptor = Encryptors.delux(password, salt);
    }

    @Override
    public String convertToDatabaseColumn(String token) {
        return token == null ? null : encryptor.encrypt(token);
    }

    @Override
    public String convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : encryptor.decrypt(dbValue);
    }
}
