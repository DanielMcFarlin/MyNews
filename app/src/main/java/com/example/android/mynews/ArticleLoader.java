package com.example.android.mynews;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

// Loads a list of articles by using an AsyncTask
public class ArticleLoader extends AsyncTaskLoader<List<Article>> {

    // LogTag Used For Troubleshooting
    private static final String LOG_TAG = ArticleLoader.class.getName();
    // Query URL
    private String mUrl;

    /**
     * Constructs a new {@link ArticleLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public ArticleLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    // This is on a background thread.
    @Override
    public List<Article> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        // Perform the network request
        // Parse the response
        // Extract a list of articles.
        return QueryUtils.getArticleData(mUrl);
    }
}
