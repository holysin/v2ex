package com.devliu.v2ex;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
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

import java.util.ArrayList;

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
            viewHolder.content = (TextView) convertView.findViewById(R.id.content);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            viewHolder.replier = (TextView) convertView.findViewById(R.id.replier);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        JSONObject jsonObject = (JSONObject) getItem(position);
        if (jsonObject.has("content_rendered")) {
            String content = jsonObject.optString("content_rendered")
                    .replace("href=\"/member/", "href=\"v2ex://member/")
                    .replace("href=\"/i/", "href=\"https://i.v2ex.co/");
            Spanned spanned = Html.fromHtml(content, new AsyncImageGetter(mContext, viewHolder.content), null);
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

            viewHolder.content.setText(spanned);
            viewHolder.content.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (jsonObject.has("member")) {
            String imageURL = "http:" + jsonObject.optJSONObject("member").optString("avatar_large");
            if (mOptions == null) {
                mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
            }
            ImageLoader.getInstance().displayImage(imageURL, viewHolder.avatar, mOptions);

            viewHolder.replier.setText(jsonObject.optJSONObject("member").optString("username"));
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
            viewHolder.time.setText(text);
        }

        return convertView;
    }

    public void update(JSONArray jsonArray) {
        mJsonArray = jsonArray;
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        ImageView avatar;
        TextView content;
        TextView replier;
        TextView time;
    }

}
