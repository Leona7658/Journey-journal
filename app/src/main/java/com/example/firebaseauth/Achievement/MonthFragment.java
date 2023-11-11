package com.example.firebaseauth.Achievement;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.firebaseauth.R;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MonthFragment extends Fragment {
    private HorizontalBarChart distanceBarChart;
    private HorizontalBarChart timeBarChart;
    private HorizontalBarChart tripBarChart;
    private HorizontalBarChart tagBarChart;

    private TextView distText;
    private TextView timeText;
    private TextView tripText;
    private TextView tagText;

    private TextView distCompare;
    private TextView timeCompare;
    private TextView tripCompare;
    private TextView tagCompare;
    private QueryFactory queryFactory;
    private CalendarUtil calendarUtil = new CalendarUtil();

    private ListenerRegistration registration1;
    private ListenerRegistration registration2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_month, container, false);

        distanceBarChart = view.findViewById(R.id.achievement_month_distance_bar_chart);
        timeBarChart = view.findViewById(R.id.achievement_month_time_bar_chart);
        tripBarChart = view.findViewById(R.id.achievement_month_trip_bar_chart);
        tagBarChart = view.findViewById(R.id.achievement_month_tag_bar_chart);

        distText = view.findViewById(R.id.achievement_month_distance_data);
        timeText = view.findViewById(R.id.achievement_month_time_data);
        tripText = view.findViewById(R.id.achievement_month_trip_data);
        tagText = view.findViewById(R.id.achievement_month_tag_data);

        distCompare = view.findViewById(R.id.achievement_month_distance_compare);
        timeCompare = view.findViewById(R.id.achievement_month_time_compare);
        tripCompare = view.findViewById(R.id.achievement_month_trip_compare);
        tagCompare = view.findViewById(R.id.achievement_month_tag_compare);

        // Distances section
        // Data
        ArrayList<BarEntry> entries_distance = new ArrayList<>();
        entries_distance.add(new BarEntry(2f, 3309f)); // variable
        entries_distance.add(new BarEntry(0f, 1960f)); // variable

        BarDataSet barDataSet_distance = new BarDataSet(entries_distance, "");
        setDataSetStyle(barDataSet_distance);


        BarData barData_distance = new BarData(barDataSet_distance);
        barData_distance.setBarWidth(1f);
        distanceBarChart.setData(barData_distance);

        // Styling
        distanceBarChart.getDescription().setEnabled(false);

        YAxis yAxis = distanceBarChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(false);

        distanceBarChart.getAxisRight().setEnabled(false);
        distanceBarChart.getXAxis().setEnabled(false);
        distanceBarChart.getLegend().setEnabled(false);

        // Custom Y-axis labels
        YAxis yAxisRight = distanceBarChart.getAxisRight();
        yAxisRight.setValueFormatter(new IndexAxisValueFormatter(new String[]{"2022", "2023"}));
        yAxisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisRight.setLabelCount(2, true);

        distanceBarChart.setTouchEnabled(false);
        distanceBarChart.invalidate();


        // Time section
        // Data
        ArrayList<BarEntry> entries_time = new ArrayList<>();
        entries_time.add(new BarEntry(2f, 16f)); // variable
        entries_time.add(new BarEntry(0f, 8f)); // variable

        BarDataSet barDataSet_time = new BarDataSet(entries_time, "");
        setDataSetStyle(barDataSet_time);

        BarData barData_time = new BarData(barDataSet_time);
        barData_time.setBarWidth(1f);
        timeBarChart.setData(barData_time);

        // Styling
        timeBarChart.getDescription().setEnabled(false);

        YAxis yAxis_time = timeBarChart.getAxisLeft();
        yAxis_time.setAxisMinimum(0f);
        yAxis_time.setDrawGridLines(false);
        yAxis_time.setDrawLabels(false);

        timeBarChart.getAxisRight().setEnabled(false);
        timeBarChart.getXAxis().setEnabled(false);
        timeBarChart.getLegend().setEnabled(false);

        // Custom Y-axis labels
        YAxis yAxisRight_time = timeBarChart.getAxisRight();
        yAxisRight_time.setValueFormatter(new IndexAxisValueFormatter(new String[]{"2022", "2023"}));
        yAxisRight_time.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisRight_time.setLabelCount(2, true);

        timeBarChart.setTouchEnabled(false);
        timeBarChart.invalidate();


        // Trips section
        // Data
        ArrayList<BarEntry> entries_trip = new ArrayList<>();
        entries_trip.add(new BarEntry(2f, 4f)); // variable
        entries_trip.add(new BarEntry(0f, 1f)); // variable

        BarDataSet barDataSet_trip = new BarDataSet(entries_trip, "");
        setDataSetStyle(barDataSet_trip);

        BarData barData_trip = new BarData(barDataSet_trip);
        barData_trip.setBarWidth(1f);
        tripBarChart.setData(barData_trip);

        // Styling
        tripBarChart.getDescription().setEnabled(false);

        YAxis yAxis_trip = tripBarChart.getAxisLeft();
        yAxis_trip.setAxisMinimum(0f);
        yAxis_trip.setDrawGridLines(false);
        yAxis_trip.setDrawLabels(false);

        tripBarChart.getAxisRight().setEnabled(false);
        tripBarChart.getXAxis().setEnabled(false);
        tripBarChart.getLegend().setEnabled(false);

        // Custom Y-axis labels
        YAxis yAxisRight_trip = tripBarChart.getAxisRight();
        yAxisRight_trip.setValueFormatter(new IndexAxisValueFormatter(new String[]{"2022", "2023"}));
        yAxisRight_trip.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisRight_trip.setLabelCount(2, true);

        tripBarChart.setTouchEnabled(false);
        tripBarChart.invalidate();


        // Tags section
        // Data
        ArrayList<BarEntry> entries_tag = new ArrayList<>();
        entries_tag.add(new BarEntry(2f, 12f)); // variable
        entries_tag.add(new BarEntry(0f, 4f)); // variable

        BarDataSet barDataSet_tag = new BarDataSet(entries_tag, "");
        setDataSetStyle(barDataSet_tag);


        BarData barData_tag = new BarData(barDataSet_tag);
        barData_tag.setBarWidth(1f);
        tagBarChart.setData(barData_tag);

        // Styling
        tagBarChart.getDescription().setEnabled(false);

        YAxis yAxis_tag = tagBarChart.getAxisLeft();
        yAxis_tag.setAxisMinimum(0f);
        yAxis_tag.setDrawGridLines(false);
        yAxis_tag.setDrawLabels(false);

        tagBarChart.getAxisRight().setEnabled(false);
        tagBarChart.getXAxis().setEnabled(false);
        tagBarChart.getLegend().setEnabled(false);

        // Custom Y-axis labels
        YAxis yAxisRight_tag = tagBarChart.getAxisRight();
        yAxisRight_tag.setValueFormatter(new IndexAxisValueFormatter(new String[]{"2022", "2023"}));
        yAxisRight_tag.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisRight_tag.setLabelCount(2, true);

        tagBarChart.setTouchEnabled(false);
        tagBarChart.invalidate();

        // query
        queryFactory = new QueryFactory(requireActivity(), distanceBarChart, timeBarChart,
                tripBarChart, tagBarChart, distText, timeText, tripText, tagText,
                distCompare, timeCompare, tripCompare, tagCompare);
        Date[] lastMonth = calendarUtil.getLastMonth(Calendar.getInstance());
        Date[] currentMonth = calendarUtil.getCurrentMonth(Calendar.getInstance());

        // data observer
        registration1 = queryFactory.onTripChange(currentMonth[0], currentMonth[1], lastMonth[0], lastMonth[1], "month");
        registration2 = queryFactory.onDetailChange(currentMonth[0], currentMonth[1], lastMonth[0], lastMonth[1], "month");


        return view;

    }
    public void setDataSetStyle(BarDataSet barDataSet) {
        barDataSet.setColors(ContextCompat.getColor(requireContext(), R.color.orange), ContextCompat.getColor(requireContext(), R.color.light_grey));
        barDataSet.setValueTextSize(10f);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setDrawValues(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (registration1 != null) {
            registration1.remove();
        }
        if (registration2 != null) {
            registration2.remove();
        }

    }

}
