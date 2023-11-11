package com.example.firebaseauth.Achievement;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.firebaseauth.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WeekFragment extends Fragment {
    private BarChart distanceBarChart;
    private BarChart timeBarChart;
    private BarChart tripBarChart;
    private BarChart tagBarChart;

    private TextView distText;
    private TextView timeText;
    private TextView tripText;
    private TextView tagText;

    private TextView distCompare;
    private TextView timeCompare;
    private TextView tripCompare;
    private TextView tagCompare;


    private int currentTags = 0;
    private int previousTags = 0;
    private int tripCount = 0;
    private int prevTripCount = 0;

    private ListenerRegistration registration1;

    private ListenerRegistration registration2;




    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CalendarUtil calendarUtil = new CalendarUtil();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);

        distanceBarChart = view.findViewById(R.id.achievement_week_distance_bar_chart);
        timeBarChart = view.findViewById(R.id.achievement_week_time_bar_chart);
        tripBarChart = view.findViewById(R.id.achievement_week_trip_bar_chart);
        tagBarChart = view.findViewById(R.id.achievement_week_tag_bar_chart);

        distText = view.findViewById(R.id.achievement_week_distance_data);
        timeText = view.findViewById(R.id.achievement_week_time_data);
        tripText = view.findViewById(R.id.achievement_week_trip_data);
        tagText = view.findViewById(R.id.achievement_week_tag_data);

        distCompare = view.findViewById(R.id.achievement_week_distance_compare);
        timeCompare = view.findViewById(R.id.achievement_week_time_compare);
        tripCompare = view.findViewById(R.id.achievement_week_trip_compare);
        tagCompare = view.findViewById(R.id.achievement_week_tag_compare);


        // Distance
        // Sample data
        ArrayList<BarEntry> entries_distance = new ArrayList<>();
        entries_distance.add(new BarEntry(1, 2500));
        entries_distance.add(new BarEntry(2, 3200));
        entries_distance.add(new BarEntry(3, 1800));
        entries_distance.add(new BarEntry(4, 3500));
        entries_distance.add(new BarEntry(5, 2900));
        entries_distance.add(new BarEntry(6, 1100));
        entries_distance.add(new BarEntry(7, 2700));

        BarDataSet barDataSet_distance = new BarDataSet(entries_distance, "Steps");
        barDataSet_distance.setColor(ContextCompat.getColor(requireContext(), R.color.lighter_orange));

        ArrayList<IBarDataSet> dataSets_distance = new ArrayList<>();
        dataSets_distance.add(barDataSet_distance);

        BarData data_distance = new BarData(dataSets_distance);
        distanceBarChart.setData(data_distance);

        // Average line
        float average_distance_this = 3118; // variable
        float average_distance_last = 2000; // variable
        LimitLine avgDistanceThis = new LimitLine(average_distance_this, String.valueOf(average_distance_this) + "km");
        LimitLine avgDistanceLast = new LimitLine(average_distance_last, String.valueOf(average_distance_last) + "km");

        setLimitLineStyle(avgDistanceThis, avgDistanceLast);

        distanceBarChart.getAxisLeft().addLimitLine(avgDistanceThis);

        distanceBarChart.getAxisLeft().addLimitLine(avgDistanceLast);

        // X-axis labels
        String[] days = {"", "M", "Tu", "W", "Th", "F", "Sa", "Su"};
        XAxis xAxis_distance = distanceBarChart.getXAxis();
        xAxis_distance.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis_distance.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Styling
        distanceBarChart.getAxisRight().setEnabled(false);
        distanceBarChart.getDescription().setEnabled(false);
        distanceBarChart.getLegend().setEnabled(false);
        distanceBarChart.setTouchEnabled(false);
        distanceBarChart.invalidate();


        // Time
        // Sample data
        ArrayList<BarEntry> entries_time = new ArrayList<>();
        entries_time.add(new BarEntry(1, 2500));
        entries_time.add(new BarEntry(2, 3200));
        entries_time.add(new BarEntry(3, 1800));
        entries_time.add(new BarEntry(4, 3500));
        entries_time.add(new BarEntry(5, 2900));
        entries_time.add(new BarEntry(6, 1100));
        entries_time.add(new BarEntry(7, 2700));

        BarDataSet barDataSet_time = new BarDataSet(entries_time, "Steps");
        barDataSet_time.setColor(ContextCompat.getColor(requireContext(), R.color.lighter_orange));

        ArrayList<IBarDataSet> dataSets_time = new ArrayList<>();
        dataSets_time.add(barDataSet_time);

        BarData data_time = new BarData(dataSets_time);
        timeBarChart.setData(data_time);

        // Average line
        float average_time_this = 3118; // variable
        float average_time_last = 2000; // variable
        LimitLine avgTimeThis = new LimitLine(average_time_this, String.valueOf(average_time_this) + " minutes");
        LimitLine avgTimeLast = new LimitLine(average_time_last, String.valueOf(average_time_last) + " minutes");
        setLimitLineStyle(avgTimeThis, avgTimeLast);

        timeBarChart.getAxisLeft().addLimitLine(avgTimeThis);

        timeBarChart.getAxisLeft().addLimitLine(avgTimeLast);

        // X-axis labels
        XAxis xAxis_time = timeBarChart.getXAxis();
        xAxis_time.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis_time.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Styling
        timeBarChart.getAxisRight().setEnabled(false);
        timeBarChart.getDescription().setEnabled(false);
        timeBarChart.getLegend().setEnabled(false);
        timeBarChart.setTouchEnabled(false);
        timeBarChart.invalidate();


        // Trips
        // Sample data
        ArrayList<BarEntry> entries_trip = new ArrayList<>();
        entries_trip.add(new BarEntry(1, 2500));
        entries_trip.add(new BarEntry(2, 3200));
        entries_trip.add(new BarEntry(3, 1800));
        entries_trip.add(new BarEntry(4, 3500));
        entries_trip.add(new BarEntry(5, 2900));
        entries_trip.add(new BarEntry(6, 1100));
        entries_trip.add(new BarEntry(7, 2700));

        BarDataSet barDataSet_trip = new BarDataSet(entries_trip, "");
        barDataSet_trip.setColor(ContextCompat.getColor(requireContext(), R.color.lighter_orange));

        ArrayList<IBarDataSet> dataSets_trip = new ArrayList<>();
        dataSets_trip.add(barDataSet_trip);

        BarData data_trip = new BarData(dataSets_trip);
        tripBarChart.setData(data_trip);

        // Average line
        float average_trip_this = 3118; // variable
        float average_trip_last = 2000; // variable
        LimitLine avgTripThis = new LimitLine(average_trip_this, String.valueOf(average_trip_this));
        LimitLine avgTripLast = new LimitLine(average_trip_last, String.valueOf(average_trip_last));
        setLimitLineStyle(avgTripThis, avgTripLast);


        tripBarChart.getAxisLeft().addLimitLine(avgTripThis);

        tripBarChart.getAxisLeft().addLimitLine(avgTripLast);

        // X-axis labels
        XAxis xAxis_trip = tripBarChart.getXAxis();
        xAxis_trip.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis_trip.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Styling
        tripBarChart.getAxisRight().setEnabled(false);
        tripBarChart.getDescription().setEnabled(false);
        tripBarChart.getLegend().setEnabled(false);
        tripBarChart.setTouchEnabled(false);
        tripBarChart.invalidate();


        // Tags
        // Sample data
        ArrayList<BarEntry> entries_tag = new ArrayList<>();
        entries_tag.add(new BarEntry(1, 2500));
        entries_tag.add(new BarEntry(2, 3200));
        entries_tag.add(new BarEntry(3, 1800));
        entries_tag.add(new BarEntry(4, 3500));
        entries_tag.add(new BarEntry(5, 2900));
        entries_tag.add(new BarEntry(6, 1100));
        entries_tag.add(new BarEntry(7, 2700));

        BarDataSet barDataSet_tag = new BarDataSet(entries_tag, "Steps");
        barDataSet_tag.setColor(ContextCompat.getColor(requireContext(), R.color.lighter_orange));

        ArrayList<IBarDataSet> dataSets_tag = new ArrayList<>();
        dataSets_tag.add(barDataSet_tag);

        BarData data_tag = new BarData(dataSets_tag);
        tagBarChart.setData(data_tag);

        // Average line
        float average_tag_this = 3118; // variable
        float average_tag_last = 2000; // variable
        LimitLine avgTagThis = new LimitLine(average_tag_this, String.valueOf(average_tag_this) + "tags");
        LimitLine avgTagLast = new LimitLine(average_tag_last, String.valueOf(average_tag_last) + " tags");
        setLimitLineStyle(avgTagThis, avgTagLast);

        tagBarChart.getAxisLeft().addLimitLine(avgTagThis);

        tagBarChart.getAxisLeft().addLimitLine(avgTagLast);

        // X-axis labels
        XAxis xAxis_tag = tagBarChart.getXAxis();
        xAxis_tag.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis_tag.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Styling
        tagBarChart.getAxisRight().setEnabled(false);
        tagBarChart.getDescription().setEnabled(false);
        tagBarChart.getLegend().setEnabled(false);
        tagBarChart.setTouchEnabled(false);
        tagBarChart.invalidate();

        // get yesterday date range
        Date[] lastWeek = calendarUtil.getLastWeek(Calendar.getInstance());
        Date[] currWeek = calendarUtil.getCurrentWeek(Calendar.getInstance());
        // data render and query
        // data observer
        registration1 = onTripChange(currWeek[0], currWeek[1], lastWeek[0], lastWeek[1]);
        registration2 = onDetailChange(currWeek[0], currWeek[1], lastWeek[0], lastWeek[1]);

        return view;
    }

    public void setLimitLineStyle(LimitLine lineThis, LimitLine lineLast) {
        lineThis.setLineColor(ContextCompat.getColor(requireContext(), R.color.orange));
        lineThis.setLineWidth(2f);
        lineThis.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
        lineThis.setTextSize(12f);

        lineLast.setLineColor(ContextCompat.getColor(requireContext(), R.color.light_grey));
        lineLast.setLineWidth(2f);
        lineLast.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_grey));
        lineLast.setTextSize(12f);
    }


    public String getEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getEmail();
    }

    public void mapToArrayData(HashMap<String, Float> map, ArrayList<BarEntry> array) {
        for (Map.Entry<String, Float> entry : map.entrySet()) {
            String day = entry.getKey();
            float data = entry.getValue();
            updateWeekDataArray(array, day, data);
        }

        addEmptyDataArray(array);

    }

    public void addEmptyDataArray(ArrayList<BarEntry> array) {
        ArrayList<Integer> numbers = new ArrayList<>(
                Arrays.asList(1,2,3,4,5,6,7));
        for (BarEntry entry: array) {
            if (numbers.contains((int) entry.getX())) {
                numbers.remove(Integer.valueOf((int) entry.getX()));
            }
        }
        for (Integer num: numbers) {
            array.add(new BarEntry(num, 0));
        }
    }

    public void updateWeekDataArray(ArrayList<BarEntry> array, String day, float data) {
        switch(day){
            case "Monday":
                array.add(new BarEntry(1, data));
                break;
            case "Tuesday":
                array.add(new BarEntry(2, data));
                break;
            case "Wednesday":
                array.add(new BarEntry(3, data));
                break;
            case "Thursday":
                array.add(new BarEntry(4, data));
                break;
            case "Friday":
                array.add(new BarEntry(5, data));
                break;
            case "Saturday" :
                array.add(new BarEntry(6, data));
                break;
            case "Sunday":
                array.add(new BarEntry(7, data));
                break;
        }
    }
    public ListenerRegistration onDetailChange(Date startDate, Date endDate, Date prevStart, Date prevEnd) {
        CollectionReference queryRef = db.collection("TripDetail");
        // Create a query against the collection.
        ListenerRegistration registration = queryRef.where(Filter.and(
                        Filter.equalTo("email", getEmail()),
                        Filter.or(
                                Filter.and(
                                        Filter.greaterThanOrEqualTo("date", startDate),
                                        Filter.lessThan("date", endDate)),
                                Filter.and(Filter.greaterThanOrEqualTo("date", prevStart),
                                        Filter.lessThan("date", prevEnd))
                        )
                ))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("QueryError", "Listen failed.", e);
                            return;
                        }
                        tagBarChart.getAxisLeft().removeAllLimitLines();
                        tagBarChart.postInvalidate();
                        previousTags = 0;
                        currentTags = 0;
                        ArrayList<BarEntry> tagArray = new ArrayList<>();

                        HashMap<String, Float> tagMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : value) {
                            Date date = document.getTimestamp("date").toDate();
                            String day = calendarUtil.getDayOFWeek(Calendar.getInstance(), date);

                            // date is before current start date
                            if (date.compareTo(startDate) < 0) {
                                if (document.getData().get("tag") != "") {
                                    previousTags += 1;
                                }
                            } else {
                                // after or equal to current date
                                if (document.getData().get("tag") != "") {
                                    currentTags += 1;
                                    if (tagMap.containsKey(day)) {
                                        tagMap.put(day, tagMap.get(day)+1);
                                    } else {
                                        tagMap.put(day, 1f);
                                    }

                                }

                            }
                        }
                        // prepare bar chart data
                        mapToArrayData(tagMap, tagArray);
                        tagText.setText(String.valueOf(currentTags));
                        if (currentTags > previousTags) {
                            tagCompare.setText("This week, you're posting more tags when travelling than last week.");
                        } else {
                            tagCompare.setText("This week, you're posting less tags  when travelling than last week.");
                        }

                        float prevAverageTag = (float) previousTags/7;
                        float currAverageTag = 0;
                        if (tagMap.size() >0) {
                            currAverageTag = currentTags/tagMap.size();
                        }
                        LimitLine avgTagThis = new LimitLine(currAverageTag, String.valueOf(currAverageTag) + " Tags");
                        LimitLine avgTagLast = new LimitLine(prevAverageTag, String.format("%.1f",prevAverageTag) + " Tags");
                        setLimitLineStyle(avgTagThis, avgTagLast);

                        // tag bar chart
                        BarDataSet tagBarDataSet = new BarDataSet(tagArray, "Steps");
                        tagBarDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.lighter_orange));

                        ArrayList<IBarDataSet> tagDataSet = new ArrayList<>();
                        tagDataSet.add(tagBarDataSet);

                        BarData tagData = new BarData(tagDataSet);
                        tagBarChart.setData(tagData);

                        // add average line
                        tagBarChart.getAxisLeft().addLimitLine(avgTagThis);
                        tagBarChart.getAxisLeft().addLimitLine(avgTagLast);
                        tagBarChart.postInvalidate();
                    }
                });
        return registration;

    }

    public ListenerRegistration onTripChange(Date startDate, Date endDate, Date prevStart, Date prevEnd) {
        CollectionReference queryRef = db.collection("Trip");
        // Create a query against the collection.
        ListenerRegistration registration = queryRef.where(Filter.and(
                        Filter.equalTo("email", getEmail()),
                        Filter.or(
                                Filter.and(
                                        Filter.greaterThanOrEqualTo("date", startDate),
                                        Filter.lessThan("date", endDate)),
                                Filter.and(Filter.greaterThanOrEqualTo("date", prevStart),
                                        Filter.lessThan("date", prevEnd))
                        )
                ))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        clearAverageLines();

                        tripCount = 0;
                        prevTripCount = 0;
                        float currDist = 0;
                        float prevDist = 0;
                        float currTime = 0;
                        float prevTime = 0;
                        ArrayList<BarEntry> distArray = new ArrayList<>();
                        ArrayList<BarEntry> timeArray = new ArrayList<>();
                        ArrayList<BarEntry> tripArray = new ArrayList<>();

                        HashMap<String, Float> distMap = new HashMap<>();
                        HashMap<String, Float> timeMap = new HashMap<>();
                        HashMap<String, Float> tripMap = new HashMap<>();

                        for (QueryDocumentSnapshot document : value) {
                            Date date = document.getTimestamp("date").toDate();
                            String day = calendarUtil.getDayOFWeek(Calendar.getInstance(), date);

                            // date is before current start date
                            if (date.compareTo(startDate) < 0) {
                                // distance is in kilometers
                                if (document.get("distance")!= null) {
                                    prevDist += (double) document.getData().get("distance");
                                }
                                if (document.get("time")!= null) {
                                    // time is in millisecond
                                    prevTime += (long) document.getData().get("time");
                                }
                                prevTripCount += 1;
                            } else {
                                if (document.getData().get("distance")!= null) {
                                    // after or equal to current date
                                    currDist += (double) document.getData().get("distance");
                                    if (distMap.containsKey(day)) {
                                        distMap.put(day, (float) (distMap.get(day)+(double) document.getData().get("distance")));
                                    } else{
                                        distMap.put(day, (float) (0f+(double) document.getData().get("distance")));
                                    }
                                }
                                if (document.getData().get("time")!= null) {
                                    float timeNow = (long) document.getData().get("time");
                                    float mins = timeNow / 60000;
                                    // time is in millisecond
                                    currTime += timeNow;
                                    if (timeMap.containsKey(day)) {

                                        timeMap.put(day, timeMap.get(day)+mins);
                                    } else {
                                        timeMap.put(day, mins);
                                    }
                                }
                                tripCount += 1;
                                if (tripMap.containsKey(day)) {
                                    tripMap.put(day, tripMap.get(day)+1);
                                } else {
                                    tripMap.put(day, 1f);

                                }

                            }

                        }
                        // prepare data for bar chart
                        mapToArrayData(distMap, distArray);
                        mapToArrayData(timeMap, timeArray);
                        mapToArrayData(tripMap, tripArray);

                        float currMinis = currTime / 60000;
                        float prevMinis = prevTime / 60000;

                        distText.setText(String.format("%.2f", currDist));
                        timeText.setText(String.format("%.2f", currMinis));
                        tripText.setText(String.valueOf(tripCount));
                        setCompareText(currDist, prevDist, currMinis, prevMinis, tripCount, prevTripCount);


                        // average line for current and last week
                        float prevAverageDist = prevDist / 7;
                        float currAverageDist = 0;
                        if (distMap.size() > 0) {
                            currAverageDist = currDist / distMap.size();
                        }
                        LimitLine avgDistThis = new LimitLine(currAverageDist, String.format("%.1f", currAverageDist) + " km");
                        LimitLine avgDistLast = new LimitLine(prevAverageDist, String.format("%.1f", prevAverageDist) + " km");
                        setLimitLineStyle(avgDistThis, avgDistLast);
                        // time average
                        float prevAverageTime = prevMinis / 7;
                        float currAverageTime = 0;
                        if (timeMap.size() > 0) {
                            currAverageTime = currMinis / timeMap.size();
                        }

                        LimitLine avgTimeThis = new LimitLine(currAverageTime, String.format("%.1f", currAverageTime) + " mins");
                        LimitLine avgTimeLast = new LimitLine(prevAverageTime, String.format("%.1f", prevAverageTime) + " mins");
                        setLimitLineStyle(avgTimeThis, avgTimeLast);
                        // trip average
                        float prevAverageTrip = (float) prevTripCount / 7;
                        float currAverageTrip = 0;
                        if (tripMap.size() > 0) {
                            currAverageTrip = tripCount / tripMap.size();
                        }

                        LimitLine avgTripThis = new LimitLine(currAverageTrip, String.valueOf(currAverageTrip) + " trips");
                        LimitLine avgTripLast = new LimitLine(prevAverageTrip, String.format("%.1f",prevAverageTrip) + " trips");
                        setLimitLineStyle(avgTripThis, avgTripLast);

                        // distance bar chart
                        BarDataSet distBarDataSet = new BarDataSet(distArray, "Steps");
                        distBarDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.lighter_orange));

                        ArrayList<IBarDataSet> distDataset = new ArrayList<>();
                        distDataset.add(distBarDataSet);

                        BarData distData = new BarData(distDataset);
                        distanceBarChart.setData(distData);

                        // time bar chart
                        BarDataSet timeBarDataSet = new BarDataSet(timeArray, "Steps");
                        timeBarDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.lighter_orange));

                        ArrayList<IBarDataSet> timeDataSet = new ArrayList<>();
                        timeDataSet.add(timeBarDataSet);

                        BarData timeData = new BarData(timeDataSet);
                        timeBarChart.setData(timeData);

                        // trip bar chart
                        BarDataSet tripBarDataSet = new BarDataSet(tripArray, "Steps");
                        tripBarDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.lighter_orange));

                        ArrayList<IBarDataSet> tripDataSet = new ArrayList<>();
                        tripDataSet.add(tripBarDataSet);

                        BarData tripData = new BarData(tripDataSet);
                        tripBarChart.setData(tripData);

                        // add average line
                        distanceBarChart.getAxisLeft().addLimitLine(avgDistThis);
                        distanceBarChart.getAxisLeft().addLimitLine(avgDistLast);
                        distanceBarChart.postInvalidate();//refresh

                        timeBarChart.getAxisLeft().addLimitLine(avgTimeThis);
                        timeBarChart.getAxisLeft().addLimitLine(avgTimeLast);
                        timeBarChart.postInvalidate();

                        tripBarChart.getAxisLeft().addLimitLine(avgTripThis);
                        tripBarChart.getAxisLeft().addLimitLine(avgTripLast);
                        tripBarChart.postInvalidate();

                    }
                });
        return registration;

    }

    public void clearAverageLines() {
        distanceBarChart.getAxisLeft().removeAllLimitLines();
        distanceBarChart.postInvalidate();//refresh

        timeBarChart.getAxisLeft().removeAllLimitLines();
        timeBarChart.postInvalidate();

        tripBarChart.getAxisLeft().removeAllLimitLines();
        tripBarChart.postInvalidate();

    }

    public void setCompareText(float currDist, float prevDist, float currMinis,
                               float prevMinis, long tripCount, long prevTripCount) {

        if (currDist > prevDist) {
            distCompare.setText("This week, you're travelling more distance than last week.");
        } else {
            distCompare.setText("This week, you're travelling less distance than last week.");
        }

        if (currMinis > prevMinis) {
            timeCompare.setText("This week, you're spending more time on travelling than last week.");
        } else {
            timeCompare.setText("This week, you're spending less time on travelling than last week.");
        }

        if (tripCount > prevTripCount) {
            tripCompare.setText("This week, you're having more trips than last week.");
        } else {
            tripCompare.setText("This week, you're having less trips than last week.");
        }


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
