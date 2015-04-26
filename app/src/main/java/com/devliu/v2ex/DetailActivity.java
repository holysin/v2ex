package com.devliu.v2ex;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetailActivity extends Activity {

    ListView mListView;
    View mHeader;
    DetailJSONAdapter mDetailJSONAdapter;
    SwipeRefreshLayout mSwipeLayout;
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

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestDetail();
            }
        });
        mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mSwipeLayout.setRefreshing(true);
        requestDetail();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void setupHeaderView() {
        if (mJson == null) return;

        mHeader = getLayoutInflater().inflate(R.layout.topic_row, null);
        ImageView avatar = (ImageView) mHeader.findViewById(R.id.avatar);
        TextView titleTextView = (TextView) mHeader.findViewById(R.id.text_title);
        TextView contentTextView = (TextView) mHeader.findViewById(R.id.text_content);
        TextView authorTextView = (TextView) mHeader.findViewById(R.id.text_author);
        TextView timeTextView = (TextView) mHeader.findViewById(R.id.text_timeline);
        TextView repliesTextView = (TextView) mHeader.findViewById(R.id.text_replies);
        TextView nodeTextView = (TextView) mHeader.findViewById(R.id.text_node);

        titleTextView.setText(mJson.optString("title"));
        authorTextView.setText(mJson.optJSONObject("member").optString("username"));
        String imageURL = "http:" + mJson.optJSONObject("member").optString("avatar_large");
        ImageLoader.getInstance().displayImage(imageURL, avatar);

        contentTextView.setMaxLines(Integer.MAX_VALUE);
        contentTextView.setTextSize(16);
        contentTextView.setLineSpacing(3f, 1.2f);
        contentTextView.setMovementMethod(LinkMovementMethod.getInstance());

        String content = mJson.optString("content_rendered")
                .replace("href=\"/member/", "href=\"v2ex://member/")
                .replace("href=\"/i/", "href=\"https://i.v2ex.co/");
        Spanned spanned = Html.fromHtml(content, new AsyncImageGetter(this, contentTextView), null);
        SpannableStringBuilder htmlSpannable;
        if(spanned instanceof SpannableStringBuilder){
            htmlSpannable = (SpannableStringBuilder) spanned;
        } else {
            htmlSpannable = new SpannableStringBuilder(spanned);
        }

        ImageSpan[] spans = htmlSpannable.getSpans(0, htmlSpannable.length(), ImageSpan.class);
        final ArrayList<String> imageUrls = new ArrayList<String>();
        final ArrayList<String> imagePositions = new ArrayList<String>();
        for(ImageSpan currentSpan : spans){
            final String imageUrl = currentSpan.getSource();
            final int start = htmlSpannable.getSpanStart(currentSpan);
            final int end   = htmlSpannable.getSpanEnd(currentSpan);
            imagePositions.add(start + "," + end);
            imageUrls.add(imageUrl);
        }
        contentTextView.setText(spanned);

        repliesTextView.setText(mJson.optString("replies") + "个回复");

        nodeTextView.setText(mJson.optJSONObject("node").optString("title"));

        if (mJson.has("created")) {
            long created = mJson.optLong("created") * 1000;
            long now = System.currentTimeMillis();
            long difference = now - created;
            CharSequence text = (difference >= 0 &&  difference<= DateUtils.MINUTE_IN_MILLIS) ?
                    getString(R.string.just_now):
                    DateUtils.getRelativeTimeSpanString(
                            created,
                            now,
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_RELATIVE);
            timeTextView.setText(text);
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
                mSwipeLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }
}
