package com.example.android.mynews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ArticleAdapter extends ArrayAdapter<Article> {

    // LogTag Used For Troubleshooting
    public static String LOG_TAG = ArrayAdapter.class.getSimpleName();

    ArticleAdapter(Context context, List<Article> articles) {
        super(context, 0, articles);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.article_list_items, parent, false);

            //Find all the views in put into ViewHolder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) listItemView.findViewById(R.id.article_name);
            viewHolder.author = (TextView) listItemView.findViewById(R.id.author_name);
            viewHolder.section = (TextView) listItemView.findViewById(R.id.section);
            viewHolder.date = (TextView) listItemView.findViewById(R.id.date);
            viewHolder.image = (ImageView) listItemView.findViewById(R.id.image);
            listItemView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) listItemView.getTag();
        Article currentArticle = getItem(position);

        // If the currentArticle is not null,
        // set the text to all data specificed here
        assert currentArticle != null;
        holder.title.setText(currentArticle.getTitle());
        holder.author.setText(currentArticle.getAuthor());
        holder.section.setText(currentArticle.getSection());
        holder.date.setText(currentArticle.getDate());
        holder.image.setImageResource(R.drawable.guardian_news_icon);

        return listItemView;
    }

    //Create ViewHolder to refer to
    static class ViewHolder {
        public TextView title;
        TextView author;
        TextView section;
        TextView date;
        ImageView image;
    }
}

