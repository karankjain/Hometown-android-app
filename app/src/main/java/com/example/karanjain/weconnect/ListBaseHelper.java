package com.example.karanjain.weconnect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karanjain on 4/12/17.
 */

public class ListBaseHelper extends BaseAdapter {
    private static ArrayList<UserDetails> searchArrayList;

    private LayoutInflater mInflater;
    private Context context;

    public ListBaseHelper(Context context, ArrayList<UserDetails> results) {
        searchArrayList = results;
        this.context= context;
        mInflater = LayoutInflater.from(context);
    }

    public void addListItemToAdapter (List<UserDetails> list){
        searchArrayList.addAll(list);
    }

    public int getCount() {
        return searchArrayList.size();
    }

    public Object getItem(int position) {
        return searchArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView txtNickname;
        TextView txtCountry;
        Button chatButton;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_row_view, null);
            holder = new ViewHolder();
            holder.txtNickname = (TextView) convertView.findViewById(R.id.nickName);
            holder.txtCountry = (TextView) convertView
                    .findViewById(R.id.country);
            holder.chatButton = (Button) convertView.findViewById(R.id.chat);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtNickname.setText(searchArrayList.get(position).getId() + ":  " + searchArrayList.get(position).getNickName() + " - " + searchArrayList.get(position).getYear());
        holder.txtCountry.setText(searchArrayList.get(position).getCountry() + ", " +searchArrayList.get(position).getState()
                + " " + searchArrayList.get(position).getCity());

        final Bundle data = new Bundle();
        data.putString("nickname", holder.txtNickname.getText().toString());
        holder.chatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtras(data);
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}
