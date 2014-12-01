package com.devliu.v2ex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;


public class NodesActivity extends Activity {

    GridView mGridView;
    NodeAdapter mNodeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodes);
        mNodeAdapter = new NodeAdapter();
        mGridView = (GridView) findViewById(R.id.node_gridview);
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
        requestNode();
    }

    private void requestNode() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.BASE_URL + "nodes/all.json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                mNodeAdapter.update(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
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
                holder.nodeTextView = (TextView) convertView.findViewById(R.id.node_textview);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            JSONObject jsonObject = (JSONObject) getItem(position);
            holder.nodeTextView.setText(jsonObject.optString("title"));
            return convertView;
        }

        public void update(JSONArray jsonArray) {
            mJsonArray = jsonArray;
            notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView nodeTextView;
        }
    }
}
