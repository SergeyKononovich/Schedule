package com.grsu.konon.schedule.Helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.grsu.konon.schedule.R;

public class SpecialAdapter extends SimpleAdapter {
    private int color1;
    private int color2;

    public SpecialAdapter(Context context, List<Map<String, Object>> items, int resource, String[] from, int[] to) {
        super(context, items, resource, from, to);
        color1 = context.getResources().getColor(R.color.scheduleColor1);
        color2 = context.getResources().getColor(R.color.scheduleColor2);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (position % 2 == 0)
            view.setBackgroundColor(color1);
        else
            view.setBackgroundColor(color2);
        return view;
    }
}