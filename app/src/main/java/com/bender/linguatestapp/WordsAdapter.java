package com.bender.linguatestapp;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bender.linguatestapp.models.Word;

import java.util.ArrayList;

public class WordsAdapter extends BaseAdapter {

    private ArrayList<Word> mDataset;
    private Activity activity;

    public WordsAdapter(Activity activity, ArrayList<Word> data) {
        this.activity = activity;
        if (data != null) this.mDataset = data;
        else this.mDataset = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.list_word, parent, false);
            holder = new Holder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        final Word word = mDataset.get(position);
        holder.left.setText(word.getInputWord());
        holder.right.setText(word.getTranslatedWord());

        return convertView;
    }

    private class Holder {
        public TextView left, right;

        public Holder(View view) {
            left = (TextView) view.findViewById(R.id.left);
            right = (TextView) view.findViewById(R.id.right);
        }
    }
}
