package com.example.android.mynews;

/**
 * An {@link Article} object contains
 * information related to a single article.
 */
public class Article {
    // Elements grabbed for each article
    private String mTitle;
    private String mAuthor;
    private String mSection;
    private String mDate;
    private String mUrl;

    /**
     * Constructs a new {@link Article} object.
     *
     * @param title   is the title of the article
     * @param author   is the author of the article
     * @param section is the section the article is found in
     * @param date    is the date article was published/edited
     * @param url     is the website URL to find more details on article
     */
    public Article(String title, String author, String section,
                   String date, String url) {
        mTitle = title;
        mAuthor = author;
        mSection = section;
        mDate = date;
        mUrl = url;
    }

    //Public getters used
    public String getTitle() {
        return mTitle;
    }

    String getAuthor() {
        return mAuthor;
    }

    String getSection() {
        return mSection;
    }

    String getDate() {
        return mDate;
    }

    String getUrl() {
        return mUrl;
    }
}

