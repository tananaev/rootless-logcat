package com.tananaev.logcat;

public class Line {

    private String level;
    private String content;

    public Line(String content) {
        level = "DEBUG";
        this.content = content;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
