package com.example.firebaseauth.Trip;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.firebaseauth.R;

public class TagList extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public TagList(Context context, String[] values) {
        super(context, R.layout.tag_list, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.tag_list, parent, false);

        TextView textView = rowView.findViewById(R.id.itemText);
        View circleView = rowView.findViewById(R.id.itemIcon);

        // Modify the circle color and text based on your requirements
        textView.setText(values[position]);

        ShapeDrawable circleDrawable = new ShapeDrawable(new OvalShape());
        circleDrawable.getPaint().setColor(getColorForPosition(position));  // Method to get color
        circleView.setBackground(circleDrawable);

        return rowView;
    }

    private int getColorForPosition(int position) {
        // Return your colors here, could be hard-coded, could be dynamic
        // This is a simple example:
        switch(position) {
            case 0: return context.getColor(R.color.tag_red);
            case 1: return context.getColor(R.color.tag_green);
            case 2: return context.getColor(R.color.tag_blue);
            case 3: return context.getColor(R.color.tag_yellow);
            case 4: return context.getColor(R.color.tag_pink);
            case 5: return context.getColor(R.color.tag_brown);
            default: return context.getColor(R.color.grey);
        }
    }
}