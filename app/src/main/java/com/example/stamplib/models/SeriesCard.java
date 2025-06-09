package com.example.stamplib.models;

public class SeriesCard {
    private final String title;
    private final String imagePath;
    private final String progress;

    private final int seriesId;

    public SeriesCard(int seriesId, String title, String imagePath, String progress) {
        this.seriesId = seriesId;
        this.title = title;
        this.imagePath = imagePath;
        this.progress = progress;
    }
    public int getSeriesId() {
        return seriesId;
    }

    public String getTitle() {
        return title;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getProgress() {
        return progress;
    }
}
