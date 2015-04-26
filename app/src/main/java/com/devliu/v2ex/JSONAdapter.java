package com.devliu.v2ex;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by liuyue on 11/30/14.
 */
public class JSONAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;
    DisplayImageOptions mOptions;

    public JSONAdapter(Context context, LayoutInflater layoutInflater) {
        mContext = context;
        mInflater = layoutInflater;
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
            convertView = mInflater.inflate(R.layout.topic_row, null);
            holder = new ViewHolder();
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.text_title);
            holder.contentTextView = (TextView) convertView.findViewById(R.id.text_content);
            holder.authorTextView = (TextView) convertView.findViewById(R.id.text_author);
            holder.timeTextView = (TextView) convertView.findViewById(R.id.text_timeline);
            holder.repliesTextView = (TextView) convertView.findViewById(R.id.text_replies);
            holder.nodeTextView = (TextView) convertView.findViewById(R.id.text_node);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject jsonObject = (JSONObject) getItem(position);

        if (jsonObject.has("member")) {
            String imageID = jsonObject.optJSONObject("member").optString("avatar_large");
            String imageURL = "http:" + imageID;

            if (mOptions == null) {
                mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
            }
            ImageLoader.getInstance().displayImage(imageURL, holder.avatar, mOptions);	//
        } else {
            holder.avatar.setImageResource(R.drawable.avatar);
        }

        if (jsonObject.has("title")) {
            holder.titleTextView.setText(jsonObject.optString("title"));
        }

        if (jsonObject.has("content")) {
            holder.contentTextView.setText(jsonObject.optString("content"));
        }

        if (jsonObject.has("member")) {
            String text = jsonObject.optJSONObject("member").optString("username");
            holder.authorTextView.setText(text);
        }

        if (jsonObject.has("created")) {
            long created = jsonObject.optLong("created") * 1000;
            long now = System.currentTimeMillis();
            long difference = now - created;
            CharSequence text = (difference >= 0 &&  difference<= DateUtils.MINUTE_IN_MILLIS) ?
                    mContext.getString(R.string.just_now):
                    DateUtils.getRelativeTimeSpanString(
                            created,
                            now,
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_RELATIVE);
            holder.timeTextView.setText(text);
        }

        if (jsonObject.has("replies")) {
            holder.repliesTextView.setText(jsonObject.optString("replies") + "个回复");
        }

        if (jsonObject.has("node")) {
            final JSONObject obj = jsonObject.optJSONObject("node");
            holder.nodeTextView.setText(obj.optString("title"));

            holder.nodeTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, TopicActivity.class);
                    intent.putExtra(TopicActivity.JSON_KEY, obj.toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
        }

        return convertView;
    }

    public void updateData(JSONArray jsonArray) {
        // update the adapter's dataset
        mJsonArray = jsonArray;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public ImageView avatar;
        public TextView titleTextView;
        public TextView contentTextView;
        public TextView authorTextView;
        public TextView repliesTextView;
        public TextView timeTextView;
        public TextView nodeTextView;
    }
}
