package com.devliu.v2ex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class TopicActivity extends Activity {
    ListView mListView;
    JSONAdapter mAdapter;
    JSONObject mJsonObject;
    SwipeRefreshLayout mSwipeLayout;
    boolean mIsLoading;
    SparseArrayCompat aa;

    public static final String JSON_KEY = "json_key";
    private static String TAG = "TopicActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupImageLoader();

        Intent intent = getIntent();
        if (intent != null) {
            String jsonString = getIntent().getStringExtra(JSON_KEY);
            if (jsonString != null) {
                try {
                    mJsonObject = new JSONObject(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mJsonObject != null) {
            setTitle(mJsonObject.optString("title"));
        } else {
            setTitle(R.string.latest_topic);
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);
        setContentView(R.layout.activity_topic);
        mListView = (ListView) findViewById(R.id.topic_listview);
        mAdapter = new JSONAdapter(getApplicationContext(), getLayoutInflater());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject topic = (JSONObject) mAdapter.getItem(position);
                if (topic != null) {
                    Intent intent = new Intent(TopicActivity.this, DetailActivity.class);
                    intent.putExtra(DetailActivity.JSON_KEY, topic.toString());
                    startActivity(intent);
                }
            }
        });

        requestTopic();

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestTopic();
            }
        });
        mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mJsonObject == null) {
            getMenuInflater().inflate(R.menu.menu_topic, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.all_nodes) {
            Intent intent = new Intent(this, NodesActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void setupImageLoader() {

        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "imageloader/Cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .diskCacheFileCount(1000)
                .diskCacheSize(50 * 1024 * 1024)
                .build();


        ImageLoader.getInstance().init(config);
    }

    private void requestTopic() {

        if (mIsLoading) {
            return;
        }
        mIsLoading = true;

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "topics/latest.json";
        RequestParams params = null;
        if (mJsonObject != null) {
            url = "topics/show.json";
            params = new RequestParams();
            params.put("node_id", mJsonObject.optString("id"));
            Log.i(TAG, Constants.BASE_URL + url +params.toString());
        }

        setProgressBarIndeterminateVisibility(true);
        client.setConnectTimeout(5000);
        Log.i(TAG, Constants.BASE_URL + url);
        client.get(Constants.BASE_URL + url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, "onSuccess(int, Header[], String)");
                setProgressBarIndeterminateVisibility(false);
                mSwipeLayout.setRefreshing(false);
                mIsLoading = false;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.i(TAG,"onSuccess(int, Header[], JSONArray)");
                setProgressBarIndeterminateVisibility(false);
                mAdapter.updateData(response);
                mSwipeLayout.setRefreshing(false);
                mIsLoading = false;
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.w(TAG, "onFailure(int, Header[], Throwable, JSONObject)", throwable);
                setProgressBarIndeterminateVisibility(false);
                Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                mIsLoading = false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                Log.w(TAG,"onFailure(int, Header[], String, Throwable)", throwable);
                Log.w(TAG, error);
                setProgressBarIndeterminateVisibility(false);
                Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                mIsLoading = false;
            }
        });
    }

}
