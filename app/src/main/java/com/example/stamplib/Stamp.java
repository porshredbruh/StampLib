package com.example.stamplib;

public class Stamp {
    private long id;
    private int seriesId;
    private String name;
    private int year;
    private String imagePath;

    public Stamp(long id, int seriesId, String name, int year, String imagePath) {
        this.id = id;
        this.seriesId = seriesId;
        this.name = name;
        this.year = year;
        this.imagePath = imagePath;
    }

    public long getId() { return id; }
    public int getSeriesId() { return seriesId; }
    public String getName() { return name; }
    public int getYear() { return year; }
    public String getImagePath() { return imagePath; }
}