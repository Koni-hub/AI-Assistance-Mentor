package com.ai.mentor_assistant.Models;

import java.util.ArrayList;
import java.util.List;

// ChatRequest model
public class ChatRequest {
    private String model;
    private List<GeminiContent> contents;

    public ChatRequest(String model, GeminiContent content) {
        this.model = model;
        this.contents = new ArrayList<>();
        this.contents.add(content);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<GeminiContent> getContents() {
        return contents;
    }

    public void setContents(List<GeminiContent> contents) {
        this.contents = contents;
    }
}