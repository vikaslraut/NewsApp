package com.example.v.news;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Custom news adapter class
 */
public class NewsAdapter extends ArrayAdapter {

    private static final String TAG = NewsAdapter.class.getSimpleName();
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String DATE_NOT_AVAILABLE = "Not Available";
    private static final String TENSE_PAST = " ago";
    private static final String TENSE_FUTURE = " from now";
    private static final String PLURAL_FORM = "s";

    private static final String SECONDS = "sec";
    private static final long SECONDS_LENGTH = 60;
    private static final String MINUTES = "min";
    private static final long MINUTES_LENGTH = 60;
    private static final String HOUR = "hour";
    private static final long HOUR_LENGTH = 24;
    private static final String DAY = "day";
    private static final long DAY_LENGTH = 7;
    private static final String WEEK = "week";
    private static final long WEEK_LENGTH = (long) 4.35;
    private static final String MONTH = "month";
    private static final long MONTH_LENGTH = 12;
    private static final String YEAR = "year";
    private static final long YEAR_LENGTH = 365;
    private static final long DIFFERENCE = 0;

    public NewsAdapter(Context context, ArrayList<NewsDAO> newsArrayList) {
        super(context, 0, newsArrayList);
    }

    public static String dateFormatter(String date) {
        Date dateFrom, dateNow;
        SimpleDateFormat sdformatter = new SimpleDateFormat(DATE_FORMAT);
        String now = sdformatter.format(new Date());
        String[] periods = {SECONDS, MINUTES, HOUR, DAY, WEEK, MONTH, YEAR};
        long[] lengths = {SECONDS_LENGTH, MINUTES_LENGTH, HOUR_LENGTH, DAY_LENGTH, WEEK_LENGTH, MONTH_LENGTH, YEAR_LENGTH};
        String tense;
        long difference = DIFFERENCE;
        if (date.isEmpty() || date == null) {
            return DATE_NOT_AVAILABLE;
        }

        try {
            dateFrom = sdformatter.parse(date);
            dateNow = sdformatter.parse(now);
        } catch (ParseException e) {
            Log.d(TAG, "dateFormatter: error while converting string to date");
            return DATE_NOT_AVAILABLE;
        }

        if (dateNow.getTime() > dateFrom.getTime()) {
            tense = TENSE_PAST;
            difference = dateNow.getTime() - dateFrom.getTime();
        } else {
            tense = TENSE_FUTURE;
            difference = dateFrom.getTime() - dateNow.getTime();
        }


        difference = difference / 1000;
        int j;
        for (j = 0; difference >= lengths[j] && j < lengths.length - 1; j++) {
            difference = difference / lengths[j];
        }

        difference = Math.round(difference);
        if (difference != 1) {
            periods[j] = periods[j] + PLURAL_FORM;
        }
        return difference + " " + periods[j] + tense;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final NewsDAO news = (NewsDAO) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_news, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.headingTextView.setText(news.getNewsHeading());
        viewHolder.sectionTextView.setText(news.getSectionName());
        viewHolder.dateTextView.setText(dateFormatter(news.getPublishDate()));

        viewHolder.author.setText(news.getPreetyAuthorsList());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openWebIntent = new Intent(Intent.ACTION_VIEW);
                openWebIntent.setData(Uri.parse(news.getWebUrl()));
                getContext().startActivity(openWebIntent);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.heading)
        TextView headingTextView;
        @BindView(R.id.section)
        TextView sectionTextView;
        @BindView(R.id.date)
        TextView dateTextView;
        @BindView(R.id.author)
        TextView author;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
