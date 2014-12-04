package com.devliu.v2ex;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends Activity {

    ListView mListView;
    View mHeader;
    DetailJSONAdapter mDetailJSONAdapter;
    public JSONObject mJson;
    public static final String JSON_KEY = "json_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        String jsonString = getIntent().getStringExtra(JSON_KEY);
        try {
            mJson = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDetailJSONAdapter = new DetailJSONAdapter(this, getLayoutInflater());
        mListView = (ListView) findViewById(R.id.detail_listview);
        setupHeaderView();
        mListView.setAdapter(mDetailJSONAdapter);
        requestDetail();
    }

    public void setupHeaderView() {
        mHeader = getLayoutInflater().inflate(R.layout.detail_header, null);
        TextView title = (TextView) mHeader.findViewById(R.id.title);
        TextView username = (TextView) mHeader.findViewById(R.id.username);
        ImageView avatar = (ImageView) mHeader.findViewById(R.id.avatar);
        TextView content = (TextView) mHeader.findViewById(R.id.content);

        if (mJson != null) {
            title.setText(mJson.optString("title"));
            username.setText(mJson.optJSONObject("member").optString("username"));
            content.setText(mJson.optString("content"));
            String imageURL = "http:" + mJson.optJSONObject("member").optString("avatar_large");
            ImageLoader.getInstance().displayImage(imageURL, avatar);
        }
        mListView.addHeaderView(mHeader);
    }

    public void requestDetail() {
        if (mJson == null) {
            return;
        }
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams parameters = new RequestParams();
        parameters.put("topic_id", mJson.optString("id"));

        client.get(Constants.BASE_URL + "replies/show.json", parameters, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("v2ex", response.toString());
                mDetailJSONAdapter.update(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });


    }


}
