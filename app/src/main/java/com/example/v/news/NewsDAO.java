package com.example.v.news;

import java.util.ArrayList;

/**
 * News data object model
 */
public class NewsDAO {
    private final static String PRETTY_DIVIDER = ", ";
    private static final String PRETTY_SEPARATOR = " - ";
    private String newsHeading;
    private String sectionName;
    private String publishDate;
    private ArrayList<String> authors;
    private String webUrl;

    public NewsDAO(String newsHeading, String sectionName, String publishDate, ArrayList<String> authors, String webUrl) {
        this.newsHeading = newsHeading;
        this.sectionName = sectionName;
        this.publishDate = publishDate;
        this.authors = authors;
        this.webUrl = webUrl;
    }

    public String getNewsHeading() {
        return newsHeading;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public String getWebUrl() {
        return webUrl;
    }

    /**
     * Method to return printable string of authors
     * @return
     */
    public String getPreetyAuthorsList() {
        StringBuilder authors = new StringBuilder();
        ArrayList<String> authorList = getAuthors();
        if (authorList.isEmpty())
            return "";
        for (int i = 0; i < authorList.size(); i++) {
            authors.append(authorList.get(i));
            if (i + 1 < authorList.size()) {
                authors.append(PRETTY_DIVIDER);
            }
        }
        authors.append(PRETTY_SEPARATOR);
        return authors.toString();
    }
}
