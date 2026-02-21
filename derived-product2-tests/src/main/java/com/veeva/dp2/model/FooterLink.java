package com.veeva.dp2.model;

public class FooterLink {

    private String text;
    private String href;
    /*private String category;*/

    // Constructor
    public FooterLink(String text, String href/*, String category*/) {
        this.text = text;
        this.href = href;
        /*this.category = category;*/
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

   /* public String getCategory() {
        return category;
    }*/

    /*public void setCategory(String category) {
        this.category = category;
    }*/

    // toString() method
    @Override
    public String toString() {
        return "FooterLink{" +
                "text='" + text + '\'' +
                ", href='" + href + '\'' +
                '}';
    }
}
