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
 * Date: 06/08/2013
 * Time: 08:44
 */
public class ListTutorialAdapter extends BaseAdapter {

    private ArrayList<Step> listStep;

    private LayoutInflater layoutInflater;

    public ListTutorialAdapter(Context context, ArrayList listData) {
        this.listStep = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listStep.size();
    }

    @Override
    public Object getItem(int position) {
        return listStep.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_tutorial, null);
            holder = new ViewHolder();
            holder.step = (TextView) convertView.findViewById(R.id.step);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.step.setText(listStep.get(position).getMassage());
        return convertView;
    }

    static class ViewHolder {
        TextView step;
    }

}