package com.example.DevTutor.controller;

import com.example.DevTutor.service.TrelloOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/trello")
@RequiredArgsConstructor
public class TrelloController {

    private final TrelloOAuthService trelloOAuthService;

    @Value("${app.trello.done-url}")
    private String doneUrl;

    @GetMapping("/connect")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, String> connect(Authentication authentication) {
        return Map.of("url", trelloOAuthService.authorizeUrl(authentication.getName()));
    }

    @GetMapping("/callback")
    public void callback(@RequestParam(required = false) String code,
                         @RequestParam(required = false) String state,
                         HttpServletResponse response) throws IOException {
        boolean connected = code != null && trelloOAuthService.finishConnect(code, state);
        response.sendRedirect(doneUrl + (connected ? "#trello=connected" : "#trello=error"));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(Authentication authentication) {
        trelloOAuthService.disconnect(authentication.getName());
    }
}
