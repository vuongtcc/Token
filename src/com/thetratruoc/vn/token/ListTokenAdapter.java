package com.thetratruoc.vn.token;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Vuong
 * Date: 29/07/2013
 * Time: 18:04
 */
public class ListTokenAdapter extends BaseAdapter {

    private ArrayList<TokenItem> listToken;

    private LayoutInflater layoutInflater;

    public ListTokenAdapter(Context context, ArrayList listData) {
        this.listToken = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listToken.size();
    }

    @Override
    public Object getItem(int position) {
        return listToken.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_token, null);
            holder = new ViewHolder();
            holder.platform = (TextView) convertView.findViewById(R.id.platform);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.platform.setText(listToken.get(position).getPlatform());
        return convertView;
    }

    static class ViewHolder {
        TextView platform;
    }

}