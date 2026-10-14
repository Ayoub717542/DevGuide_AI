package com.example.DevTutor.service.serviceInterface;

import java.io.IOException;

public interface ChatService {
    String reply(String message) throws IOException;
}