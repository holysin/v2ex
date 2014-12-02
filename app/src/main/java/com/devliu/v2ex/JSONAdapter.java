package com.devliu.v2ex;

import android.content.Context;
import android.graphics.Bitmap;
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

        String title = "";
        if (jsonObject.has("title")) {
            title = jsonObject.optString("title");
        }

        holder.titleTextView.setText(title);
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
    }
}
