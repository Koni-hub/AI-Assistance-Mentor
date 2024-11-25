package com.ai.mentor_assistant.Models;

import java.util.List;

// ChatResponse model
public class ChatResponse {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "candidates=" + candidates +
                '}';
    }

    // Inner Candidate class
    public static class Candidate {
        private Content content;

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Candidate{" +
                    "content=" + content +
                    '}';
        }
    }

    // Inner Content class
    public static class Content {
        private List<Part> parts;

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "parts=" + parts +
                    '}';
        }
    }

    // Inner Part class
    public static class Part {
        private String text;

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "Part{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }
}