package com.example.stamplib.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.stamplib.R;

public class SeriesCursorAdapter extends CursorAdapter {

    private final LayoutInflater inflater;

    public SeriesCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.item_series, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleView = view.findViewById(R.id.seriesTitle);
        TextView yearView = view.findViewById(R.id.seriesYear);
        ImageView imageView = view.findViewById(R.id.seriesImage);

        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));

        titleView.setText(title);
        yearView.setText(String.valueOf(year));

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(context)
                    .load("https://YOUR_URL.com" + imagePath)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder);
        }
    }
}
