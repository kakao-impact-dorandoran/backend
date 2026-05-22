package com.dorandoran.backend.global.security;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ForbiddenWordFilter {

    private static final List<String> BLACKLIST = List.of(
            "씨발", "시발", "ㅅㅂ", "병신", "ㅂㅅ", "개새끼", "좆", "fuck", "shit", "bitch"
    );

    public boolean contains(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String normalized = input.toLowerCase();
        for (String word : BLACKLIST) {
            if (normalized.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
