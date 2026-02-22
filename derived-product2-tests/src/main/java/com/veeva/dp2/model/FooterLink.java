package com.veeva.dp2.model;

public class FooterLink {

    private String text;
    private String href;

    // Constructor
    public FooterLink(String text, String href) {
        this.text = text;
        this.href = href;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
    // toString() method
    @Override
    public String toString() {
        return "FooterLink{" +
                "text='" + text + '\'' +
                ", href='" + href + '\'' +
                '}';
    }
}
