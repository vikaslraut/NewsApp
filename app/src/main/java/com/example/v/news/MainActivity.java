package com.example.v.news;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<NewsDAO>> {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String NEWS_REQUEST_URL = "http://content.guardianapis.com/search";
    private static final int DEFAULT_PAGE = 1;
    //Constants required in web request
    private final static String URL_QUERY_KEY = "q";
    private final static String URL_QUERY_PARAMETER = "android";
    private final static String URL_ORDERBY_KEY = "order-by";
    private final static String URL_ORDERBY_PARAMETER = "newest";
    private final static String URL_TAGS_KEY = "show-tags";
    private final static String URL_TAGS_PARAMETER = "contributor";
    private final static String URL_API_KEY = "api-key";
    private final static String URL_API_PARAMETER = "test";
    private final static String URL_PAGE_KEY = "page";
    //Constants required in json parsing
    private static final String JSON_ROOT_OBJECT_TAG = "response";
    private static final String JSON_ROOT_ARRAY_TAG = "results";
    private static final String JSON_NEWS_TAGS = "tags";
    private static final String JSON_NEWS_TITLE = "webTitle";
    private static final String JSON_NEWS_SECTION = "sectionName";
    private static final String JSON_NEWS_DATE = "webPublicationDate";
    private static final String JSON_NEWS_WEB_URL = "webUrl";

    public static int currentPage = DEFAULT_PAGE;
    @BindView(R.id.news_list)
    ListView newsList;
    @BindView(R.id.no_network)
    View noNetwork;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.nextPage)
    Button nextPage;
    @BindView(R.id.prevPage)
    Button prevPage;
    @BindView(R.id.navigationBar)
    View navigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /**
         * If network available request news data
         * Else display no connection
         */
        if (Utils.networkAvailable(this)) {
            navigationBar.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(0, null, MainActivity.this);
            nextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentPage++;
                    getLoaderManager().restartLoader(0, null, MainActivity.this);
                    if (currentPage > DEFAULT_PAGE) {
                        prevPage.setEnabled(true);
                    }

                }
            });

            prevPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentPage != DEFAULT_PAGE) {
                        currentPage--;
                        getLoaderManager().restartLoader(0, null, MainActivity.this);
                    } else {
                        prevPage.setEnabled(false);
                    }
                }
            });
        } else {
            nextPage.setVisibility(View.GONE);
            prevPage.setVisibility(View.GONE);
            noNetwork.setVisibility(View.VISIBLE);
            newsList.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

    }

    //Build url
    private URL buildURL(int pageNumber) {
        Uri baseUrl = Uri.parse(NEWS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUrl.buildUpon();
        uriBuilder.appendQueryParameter(URL_QUERY_KEY, URL_QUERY_PARAMETER);
        uriBuilder.appendQueryParameter(URL_ORDERBY_KEY, URL_ORDERBY_PARAMETER);
        uriBuilder.appendQueryParameter(URL_TAGS_KEY, URL_TAGS_PARAMETER);
        uriBuilder.appendQueryParameter(URL_API_KEY, URL_API_PARAMETER);
        uriBuilder.appendQueryParameter(URL_PAGE_KEY, String.valueOf(pageNumber));
        return Utils.createURL(uriBuilder.toString());
    }


    public void updateListUI(ArrayList<NewsDAO> newsDAOs) {
        Log.d(TAG, "updateListUI: ");
        progressBar.setVisibility(View.GONE);
        if (newsDAOs.isEmpty()) {
            newsList.setEmptyView(findViewById(R.id.emptyState));
            return;
        }
        NewsAdapter newsAdapter = new NewsAdapter(this, newsDAOs);
        newsList.setAdapter(newsAdapter);
    }

    //Loader
    @Override
    public Loader<ArrayList<NewsDAO>> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader: loader created");
        return new NewsLoader(MainActivity.this, buildURL(currentPage));
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<NewsDAO>> loader, ArrayList<NewsDAO> newsDAOs) {
        Log.d(TAG, "onLoadFinished: loader finished");
        if (newsDAOs == null)
            return;
        updateListUI(newsDAOs);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<NewsDAO>> loader) {
        Log.d(TAG, "onLoaderReset: loader reset");
        currentPage = DEFAULT_PAGE;
    }

    //Loader class
    public static class NewsLoader extends AsyncTaskLoader<ArrayList<NewsDAO>> {

        URL url;

        public NewsLoader(Context context, URL url) {
            super(context);
            this.url = url;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public ArrayList<NewsDAO> loadInBackground() {
            Log.d(TAG, "loadInBackground: started");
            String jsonResponse = null;
            try {
                jsonResponse = Utils.makeHttpRequest(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return extractNewsListFromJson(jsonResponse);
        }

        /**
         * Method to parse JSON response and generate of ArrayList<newsDAO> type
         *
         * @param response
         * @return
         */
        private ArrayList<NewsDAO> extractNewsListFromJson(String response) {
            if (TextUtils.isEmpty(response))
                return null;

            ArrayList<NewsDAO> tempList = new ArrayList<>();
            //StringBuilder authorList = new StringBuilder();

            try {
                JSONObject rootJsonObject = new JSONObject(response);
                JSONObject responseJsonObject = rootJsonObject.getJSONObject(JSON_ROOT_OBJECT_TAG);
                JSONArray newsResults = responseJsonObject.getJSONArray(JSON_ROOT_ARRAY_TAG);
                Log.d(TAG, "extractNewsListFromJson: root array length : " + newsResults.length());

                for (int i = 0; i < newsResults.length(); i++) {
                    ArrayList<String> authors = new ArrayList<>();
                    JSONObject news = newsResults.getJSONObject(i);
                    JSONArray tagsArray = news.getJSONArray(JSON_NEWS_TAGS);
                    if (tagsArray != null) {
                        for (int j = 0; j < tagsArray.length(); ++j) {
                            JSONObject tag = tagsArray.getJSONObject(j);
                            authors.add(j, tag.getString(JSON_NEWS_TITLE));
                        }
                    }
                    tempList.add(new NewsDAO(news.getString(JSON_NEWS_TITLE), news.getString(JSON_NEWS_SECTION), news.getString(JSON_NEWS_DATE), authors, news.getString(JSON_NEWS_WEB_URL)));
                }
            } catch (JSONException e) {
                Log.e(TAG, "extractBooksFromJson: Problem parsing json ", e);
            }
            return tempList;
        }
    }
}
