package com.example.stamplib.models;

import java.util.List;

public class AnalyzeResponse {
    private String cropped_path;
    private List<String> results;

    public String getCropped_path() {
        return cropped_path;
    }

    public List<String> getResults() {
        return results;
    }
}
