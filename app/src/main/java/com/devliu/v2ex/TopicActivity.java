package com.devliu.v2ex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class TopicActivity extends Activity {
    ListView mListView;
    JSONAdapter mAdapter;
    JSONObject mJsonObject;

    public static final String JSON_KEY = "json_key";

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
            setTitle(R.string.hot_topic);
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

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "topics/hot.json";
        RequestParams params = null;
        if (mJsonObject != null) {
            url = "topics/show.json";
            params = new RequestParams();
            params.put("node_id", mJsonObject.optString("id"));
        }

        setProgressBarIndeterminateVisibility(true);
        client.get(Constants.BASE_URL + url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                setProgressBarIndeterminateVisibility(false);
                mAdapter.updateData(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                setProgressBarIndeterminateVisibility(false);
                Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
