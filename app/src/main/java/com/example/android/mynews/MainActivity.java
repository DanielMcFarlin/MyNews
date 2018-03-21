package com.example.android.mynews;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<Article>>, SharedPreferences.OnSharedPreferenceChangeListener {

    // Initialize all static Strings used in MainActivity
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String GUARD_URL = "http://content.guardianapis.com/search?";
    private static final String API_KEY_TITLE = "api-key";
    private static final String API_KEY = "e64ad6a3-0b7a-4a46-9732-0d3a64883272";

    // Set and Loader ID to refer back to when Loader is Initialized
    private static final int ARTICLE_LOADER_ID = 1;

    // Using ButterKnife library to reduce repetitious code
    @BindView(R.id.empty_state)
    TextView mEmptyView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgress;
    @BindView(R.id.list_items)
    ListView articleListView;

    /**
     * Adapter for the list of articles
     */
    private ArticleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Unbinder unbinder = ButterKnife.bind(this);

        // Set the ListView to Empty
        articleListView.setEmptyView(mEmptyView);

        // Create a new adapter with empty list of articles as input
        mAdapter = new ArticleAdapter(this, new ArrayList<Article>());

        // Set the adapter on the ListView
        // so list can populate the UI
        articleListView.setAdapter(mAdapter);

        // Get PreferenceManager to change the search
        SharedPreferences prefsSearch = PreferenceManager.getDefaultSharedPreferences(this);
        prefsSearch.registerOnSharedPreferenceChangeListener(this);

        // Set OnClickListener so to open browser to URL for more information
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Article currentArticle = mAdapter.getItem(position);

                // Convert the String URL into a URI object.
                assert currentArticle != null;
                Uri articleUri = Uri.parse(String.valueOf(currentArticle.getUrl()));

                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, articleUri);

                startActivity(websiteIntent);
            }
        });

        // Get the ConnectivityManager to check state of network.
        ConnectivityManager conMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the current data network
        assert conMgr != null;
        NetworkInfo network = conMgr.getActiveNetworkInfo();

        // If there is a network connection then get the data
        if (network != null && network.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();

            // 1) Initialize the loader
            // 2) Pass in the int ID constant defined above and pass in null for the bundle.
            // 3) Pass in this activity for the LoaderCallbacks parameter
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            // hide loading indicator
            View loading = findViewById(R.id.progress_bar);
            loading.setVisibility(View.GONE);

            // Update empty state with "no_internet" error message
            mEmptyView.setText(R.string.no_internet);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String searchKey) {
        if (searchKey.equals(getString((R.string.search_key)))) {
            // Clear the ListView for new search
            mAdapter.clear();

            // Hide the EmptyView
            mEmptyView.setVisibility(View.GONE);

            // Show the loading indicator to show progress being made
            View loading = findViewById(R.id.progress_bar);
            loading.setVisibility(View.VISIBLE);

            // Restart the loader to re-query the Guardian as the search settings has been changed.
            getLoaderManager().restartLoader(ARTICLE_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        // When searchPrefs are changed the loader will be created again after updated info
        SharedPreferences searchPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String searchedText = searchPrefs.getString(
                getString(R.string.search_key),
                getString(R.string.search_default));

        // Static string referred to at the top to build upon to create Uri.
        Uri baseUri = Uri.parse(GUARD_URL);
        Uri.Builder builder = baseUri.buildUpon();

        builder.appendQueryParameter("format", "json")
                .appendQueryParameter("show-references", "author")
                .appendQueryParameter("show-tags", "contributor")
                .appendQueryParameter("q", searchedText)
                .appendQueryParameter(API_KEY_TITLE, API_KEY);

        return new ArticleLoader(this, builder.build().toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        // Hide loading indicator
        View loadingIndicator = findViewById(R.id.progress_bar);
        loadingIndicator.setVisibility(View.GONE);
        // Set EmptyView to no articles found error message
        mEmptyView.setText(R.string.no_articles);
        // Clear the adapter
        mAdapter.clear();

        // Get the ConnectivityManager to check state of network.
        ConnectivityManager conMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the current data network
        assert conMgr != null;
        NetworkInfo network = conMgr.getActiveNetworkInfo();
        // If there is a network connection then get the data
        if (network == null) {
            loadingIndicator.setVisibility(View.GONE);
            // Set EmptyView to no articles found error message
            mEmptyView.setText(R.string.no_internet);
            // Clear the adapter
            mAdapter.clear();
        }

        // If there is a valid list of articles,
        // Then add them to the list.
        if (articles != null && !articles.isEmpty()) {
            mAdapter.addAll(articles);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        // Loader reset, so clear data.
        mAdapter.clear();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem searchItem) {
        int id = searchItem.getItemId();
        if (id == R.id.action_search) {
            Intent settingsIntent = new Intent(this, SearchActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(searchItem);
    }
}