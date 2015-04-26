package com.devliu.v2ex;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.graphics.PixelFormat;

import com.devliu.v2ex.pinyin.PinyinAlpha;
import com.devliu.v2ex.pinyin.PinyinComparator;

import com.etsy.android.grid.StaggeredGridView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class NodesActivity extends Activity implements AlphaView.OnAlphaChangedListener{
    StaggeredGridView mGridView;
    NodeAdapter mNodeAdapter;
    AlphaView mAlphaView;
    TextView mOverlay;
    SwipeRefreshLayout mSwipeLayout;
    WindowManager mWindowManager;
    HashMap<String, Integer> mAlphaPosition = new HashMap<String, Integer>();
    static String TAG = "NodesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);
        setContentView(R.layout.activity_nodes);
        mNodeAdapter = new NodeAdapter();
        mAlphaView = (AlphaView) findViewById(R.id.alpha_view);
        mAlphaView.setOnAlphaChangedListener(this);
        mGridView = (StaggeredGridView) findViewById(R.id.grid_all_node);
        mGridView.setAdapter(mNodeAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject jsonObject = (JSONObject) mNodeAdapter.getItem(position);
                Intent intent = new Intent(NodesActivity.this, TopicActivity.class);
                intent.putExtra(TopicActivity.JSON_KEY, jsonObject.toString());
                startActivity(intent);
            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);
        mOverlay = (TextView) inflater.inflate(R.layout.overlay, null);
        mOverlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mOverlay, lp);

        requestNode();

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestNode();
            }
        });
        mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void requestNode() {
        AsyncHttpClient client = new AsyncHttpClient();
        mAlphaView.setVisibility(View.GONE);
        setProgressBarIndeterminateVisibility(true);

        client.get(Constants.BASE_URL + "nodes/all.json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                setProgressBarIndeterminateVisibility(false);
                super.onSuccess(statusCode, headers, response);
                mSwipeLayout.setRefreshing(false);
                mNodeAdapter.update(response);
                mAlphaView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                setProgressBarIndeterminateVisibility(false);
                super.onFailure(statusCode, headers, responseString, throwable);
                mSwipeLayout.setRefreshing(false);
            }
        });
    }

    private class NodeAdapter extends BaseAdapter {

        JSONArray mJsonArray;

        public NodeAdapter() {
            mJsonArray = new JSONArray();
        }

        @Override
        public int getCount() {
            return mJsonArray.length();
        }

        @Override
        public Object getItem(int position) {
            return mJsonArray.optJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.node_cell, null);
                holder.nodeTextView = (TextView) convertView.findViewById(R.id.node_title);
                holder.nodeSummaryTextView = (TextView) convertView.findViewById(R.id.node_summary);
                holder.nodeTopicsTextView = (TextView) convertView.findViewById(R.id.node_topics);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            JSONObject jsonObject = (JSONObject) getItem(position);
            String title = jsonObject.optString("title");
            String header = jsonObject.optString("header");
            String topics = jsonObject.optString("topics");
            holder.nodeTextView.setText(title);

            if (!header.equals("null")) {
                holder.nodeSummaryTextView.setVisibility(View.VISIBLE);
                holder.nodeSummaryTextView.setText(header);
            } else {
                holder.nodeSummaryTextView.setVisibility(View.GONE);
            }
            holder.nodeTopicsTextView.setText(topics + " 个主题");
            return convertView;
        }

        public void update(JSONArray jsonArray) {
            TreeMap<String, List<JSONObject>> lists = new TreeMap <String, List<JSONObject>>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.optJSONObject(i);
                String alpha = PinyinAlpha.getFirstChar(obj.optString("title"));
                if(!lists.containsKey(alpha)){
                    List<JSONObject> list = new ArrayList<JSONObject>();
                    list.add(obj);
                    lists.put(alpha, list);
                } else{
                    lists.get(alpha).add(obj);
                }
            }

            PinyinComparator comparator = new PinyinComparator();
            List<JSONObject> values = new ArrayList<JSONObject>();
            Iterator iter = lists.entrySet().iterator();
            int offset = 0;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String)entry.getKey();
                List<JSONObject> val = (List<JSONObject>)entry.getValue();
                Collections.sort(val, comparator);
                values.addAll(val);
                mAlphaPosition.put(key, offset);
                offset += val.size();
            }
            android.util.Log.i(TAG, "pos=" + mAlphaPosition.toString());

            mJsonArray = new JSONArray(values);
            notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView nodeTextView;
            TextView nodeSummaryTextView;
            TextView nodeTopicsTextView;
        }
    }

    private Handler handler = new Handler();
    private Runnable overlayThread = new Runnable() {
        @Override
        public void run() {
            mOverlay.setVisibility(View.GONE);
        }
    };

    @Override
    public void OnAlphaChanged(String s, int index) {
        if (s != null && s.trim().length() > 0) {
            mOverlay.setText(s);
            mOverlay.setVisibility(View.VISIBLE);
            handler.removeCallbacks(overlayThread);
            handler.postDelayed(overlayThread, 500);
            if (mAlphaPosition.get(s) != null) {
                int position = mAlphaPosition.get(s);
                mGridView.setNeedSync(true);
                mGridView.setSelection(position);
                mGridView.smoothScrollBy(100, 10);
            }
        }
    }
}
