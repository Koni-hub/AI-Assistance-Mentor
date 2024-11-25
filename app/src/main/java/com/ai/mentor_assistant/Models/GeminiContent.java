package com.ai.mentor_assistant.Models;

import java.util.ArrayList;
import java.util.List;

// GeminiContent model
public class GeminiContent {
    private List<Part> parts;

    public GeminiContent(String text) {
        this.parts = new ArrayList<>();
        this.parts.add(new Part(text));
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }
}