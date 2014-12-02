package com.devliu.v2ex;

import android.content.Context;
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

/**
 * Created by liuyue on 12/1/14.
 */
public class DetailJSONAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;
    DisplayImageOptions mOptions;

    public DetailJSONAdapter(Context context, LayoutInflater layoutInflater) {
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
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.detail_row, null);
            viewHolder = new ViewHolder();
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        JSONObject jsonObject = (JSONObject) getItem(position);
        if (jsonObject.has("content")) {
            viewHolder.title.setText(jsonObject.optString("content"));
        }

        if (jsonObject.has("member")) {
            String imageURL = "http:" + jsonObject.optJSONObject("member").optString("avatar_large");
            if (mOptions == null) {
                mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
            }
            ImageLoader.getInstance().displayImage(imageURL, viewHolder.avatar, mOptions);
        }
        return convertView;
    }

    public void update(JSONArray jsonArray) {
        mJsonArray = jsonArray;
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        ImageView avatar;
        TextView title;
    }

}
