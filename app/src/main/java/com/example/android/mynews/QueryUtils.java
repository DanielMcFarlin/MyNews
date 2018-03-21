package com.example.android.mynews;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Requesting and receiving news data from The Guardian.
 */
public final class QueryUtils {

    // Tag for the log messages
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    // Create a private constructor
    private QueryUtils() {
    }

    // Query the Guardian data set and return a list of link Article objects.
    static List<Article> getArticleData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL
        // Receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of Articles
        // Return the list of Articles
        return extractFromJson(jsonResponse);
    }

    // Returns new URL object from the given string URL.
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    // Make an HTTP request to the given URL and return a String.
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        // Open the Url connection
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(1000); //ms
            urlConnection.setConnectTimeout(1500); //ms
            // Using GET because we are trying to retrieve info from their servers
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (code 200),
            // Read the input stream
            // Parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = fromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem getting the Guardian JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String
     * using InputStreamReader and then BufferedReader
     */
    private static String fromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    // Method used to change the Date format
    private static String formatDate(String rawDate) {
        String originalDateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(originalDateFormat, Locale.US);
        try {
            Date parsedDate = dateFormatter.parse(rawDate);
            String fixedDatePattern = "MMM d, yyy";
            SimpleDateFormat finalDateFormatter = new SimpleDateFormat(fixedDatePattern, Locale.US);
            return finalDateFormatter.format(parsedDate);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing JSON date: ", e);
            return "";
        }
    }

    /**
     * Return a list of {@link Article} objects from JSON response.
     */
    private static List<Article> extractFromJson(String articleJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(articleJSON)) {
            return null;
        }
        List<Article> articles = new ArrayList<>();

        try { // Start a new JSON Object to add to
            JSONObject startJsonObject = new JSONObject(articleJSON);

            // Open the JSON object with the key "response"
            JSONObject response = startJsonObject.getJSONObject("response");

            // Open the JSON array with the key "results" inside of "response" object
            JSONArray result = response.getJSONArray("results");

            // Inside the "results" array loop to get the following strings  values
            for (int i = 0; i < result.length(); i++) {
                JSONObject currentArticle = result.getJSONObject(i);
                String title = currentArticle.getString("webTitle");
                String section = currentArticle.getString("sectionName");
                String date = currentArticle.getString("webPublicationDate");
                date = formatDate(date);
                String url = currentArticle.getString("webUrl");
                JSONArray tagsArr = currentArticle.getJSONArray("tags");
                String author = "";

                if (tagsArr.length() == 0) {
                    author = "No Specified Author";
                } else {
                    for (int l = 0; l < tagsArr.length(); l++) {
                        JSONObject firstAuthor = tagsArr.getJSONObject(l);
                        author += firstAuthor.getString("webTitle") + " | ";
                    }
                }

                // Now add all those strings values found into the Article Object
                articles.add(new Article(title, author, section, date, url));

            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }
        return articles;
    }

}