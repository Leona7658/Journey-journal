package com.example.firebaseauth.Achievement;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.firebaseauth.R;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
import java.util.Date;

public class QueryFactory {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private HorizontalBarChart distanceBarChart;
    private HorizontalBarChart timeBarChart;
    private HorizontalBarChart tripBarChart;
    private HorizontalBarChart tagBarChart;

    private TextView distText;
    private TextView timeText;
    private TextView tripText;
    private TextView tagText;
    private Context context;

    private TextView distCompare;
    private TextView timeCompare;
    private TextView tripCompare;
    private TextView tagCompare;

    private int currentTags = 0;
    private int previousTags = 0;
    private int tripCount = 0;
    private int prevTripCount = 0;

    private int detailCount = 0;
    private int prevDetails = 0;


    public QueryFactory(Context context, HorizontalBarChart distanceBarChart, HorizontalBarChart timeBarChart,
                        HorizontalBarChart tripBarChart, HorizontalBarChart tagBarChart, TextView distText,
                        TextView timeText, TextView tripText, TextView tagText, TextView distCompare,
                        TextView timeCompare, TextView tripCompare, TextView tagCompare) {
        this.distanceBarChart = distanceBarChart;
        this.timeBarChart = timeBarChart;
        this.tripBarChart = tripBarChart;
        this.tagBarChart = tagBarChart;
        this.context = context;
        this.distText = distText;
        this.timeText = timeText;
        this.tripText = tripText;
        this.tagText = tagText;
        this.distCompare = distCompare;
        this.timeCompare = timeCompare;
        this.tripCompare = tripCompare;
        this.tagCompare = tagCompare;
    }


    public String getEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getEmail();
    }

    public void setDataSetStyle(BarDataSet barDataSet) {
        barDataSet.setColors(ContextCompat.getColor(context, R.color.orange), ContextCompat.getColor(context, R.color.light_grey));
        barDataSet.setValueTextSize(10f);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setDrawValues(true);
    }

    public ListenerRegistration onDetailChange(Date startDate, Date endDate, Date prevStart, Date prevEnd, String range) {
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
                            return;
                        }
                        previousTags = 0;
                        currentTags = 0;
                        for (QueryDocumentSnapshot document : value) {
                            Date date = document.getTimestamp("date").toDate();

                            // date is before current start date
                            if (date.compareTo(startDate) < 0) {
                                if (document.getData().get("tag") != "") {
                                    previousTags += 1;
                                }
                            } else {
                                // after or equal to current date
                                if (document.getData().get("tag") != "") {
                                    currentTags += 1;
                                }
                            }
                        }
                        tagText.setText(String.valueOf(currentTags));
                        if (currentTags > previousTags) {
                            if (range=="day") {
                                tagCompare.setText("Today, you're posting more tags when travelling than yesterday");
                            } else {
                                tagCompare.setText("This "+ range+", you're posting more tags when travelling than last "+range+".");
                            }
                        } else {
                            if (range=="day") {
                                tagCompare.setText("Today, you're posting less tags when travelling than yesterday");
                            } else {
                                tagCompare.setText("This "+ range+", you're posting less tags when travelling than last "+range+".");
                            }
                        }

                        // create data entry: Tag
                        ArrayList<BarEntry> tagEntry = new ArrayList<>();
                        tagEntry.add(new BarEntry(2f, currentTags)); // variable
                        tagEntry.add(new BarEntry(0f, previousTags)); // variable
                        // insert to bar data set: Tag
                        BarDataSet tagDataset = new BarDataSet(tagEntry, "");
                        setDataSetStyle(tagDataset);
                        // tag bar chart
                        BarData tagBarData = new BarData(tagDataset);
                        tagBarData.setBarWidth(1f);
                        tagBarChart.setData(tagBarData);
                        tagBarChart.postInvalidate();//refresh

                    }
                });
        return registration;

    }

    public ListenerRegistration onTripChange(Date startDate, Date endDate, Date prevStart, Date prevEnd, String range) {
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
                            Log.w("QueryError", "Listen failed.", e);
                            return;
                        }
                        tripCount = 0;
                        prevTripCount = 0;
                        float currDist = 0;
                        float prevDist = 0;
                        float currTime = 0;
                        float prevTime = 0;

                        for (QueryDocumentSnapshot document : value) {
                            Date date = document.getTimestamp("date").toDate();
                            // date is before current start date
                            if (date.compareTo(startDate) < 0) {
                                // distance is in kilometers
                                if (document.getData().get("distance") != null) {
                                    prevDist += (double) document.getData().get("distance");
                                }
                                if (document.getData().get("time") != null) {
                                    prevTime += (long) document.getData().get("time");

                                }
                                prevTripCount += 1;

                            } else {
                                if (document.getData().get("distance") != null) {
                                    currDist += (double) document.getData().get("distance");
                                }
                                if (document.getData().get("time")!= null) {
                                    currTime += (long) document.getData().get("time");

                                }
                                tripCount += 1;
                            }
                        }

                        // convert millisecond to hours
                        float currMinis = (float) currTime / 60000;
                        float prevMinis = (float) prevTime / 60000;

                        distText.setText(String.format("%.2f", currDist));
                        timeText.setText(String.format("%.2f", currMinis));
                        tripText.setText(String.valueOf(tripCount));

                        setCompareText(currDist, prevDist, currMinis, prevMinis, tripCount,
                                prevTripCount, range);

                        // create data entry: distance, time, trip
                        ArrayList<BarEntry> distEntry = new ArrayList<>();
                        distEntry.add(new BarEntry(2f, currDist)); // variable
                        distEntry.add(new BarEntry(0f, prevDist)); // variable

                        ArrayList<BarEntry> timeEntry = new ArrayList<>();
                        timeEntry.add(new BarEntry(2f, currMinis)); // variable
                        timeEntry.add(new BarEntry(0f, prevMinis)); // variable

                        ArrayList<BarEntry> tripEntry = new ArrayList<>();
                        tripEntry.add(new BarEntry(2f, tripCount)); // variable
                        tripEntry.add(new BarEntry(0f, prevTripCount)); // variable
                        // insert to bar data set: distance, time, trip
                        BarDataSet distDataSet = new BarDataSet(distEntry, "");
                        BarDataSet timeDataSet = new BarDataSet(timeEntry, "");
                        BarDataSet tripDataSet = new BarDataSet(tripEntry, "");

                        setDataSetStyle(distDataSet);
                        setDataSetStyle(timeDataSet);
                        setDataSetStyle(tripDataSet);
                        // distance bar char
                        BarData distBarData = new BarData(distDataSet);
                        distBarData.setBarWidth(1f);
                        distanceBarChart.setData(distBarData);
                        distanceBarChart.postInvalidate();//refresh
                        // time bar chart
                        BarData timeBarData = new BarData(timeDataSet);
                        timeBarData.setBarWidth(1f);
                        timeBarChart.setData(timeBarData);
                        timeBarChart.postInvalidate();//refresh
                        // trip bar chart
                        BarData tripBarData = new BarData(tripDataSet);
                        tripBarData.setBarWidth(1f);
                        tripBarChart.setData(tripBarData);
                        tripBarChart.postInvalidate();//refresh


                    }
                });
        return registration;

    }

    public void setCompareText(float currDist, float prevDist, float currMinis, float prevMinis,
                               long tripCount, long prevTripCount, String range) {

        if (currDist > prevDist) {
            if (range=="day") {
                distCompare.setText("Today, you're travelling more distance than yesterday.");
            } else {
                distCompare.setText("This " + range+", you're travelling more distance than last "+range+ ".");
            }
        } else {
            if (range=="day") {
                distCompare.setText("Today, you're travelling less distance than yesterday.");
            } else {
                distCompare.setText("This " + range+", you're travelling less distance than last "+range+ ".");
            }
        }

        if (currMinis > prevMinis) {
            if (range == "day") {
                timeCompare.setText("Today, you're spending more time on travelling than yesterday.");

            } else {
                timeCompare.setText("This " + range+", you're spending more time on travelling than last " +range+".");
            }
        } else {
            if (range == "day") {
                timeCompare.setText("Today, you're spending less time on travelling than yesterday.");

            } else {
                timeCompare.setText("This " + range+", you're spending less time on travelling than last " +range+".");
            }
        }

        if (tripCount > prevTripCount) {
            if (range=="day") {
                tripCompare.setText("Today, you're having more trips than yesterday.");
            } else {
                tripCompare.setText("This " + range+", you're having more trips than last "+range+".");
            }
        } else {
            if (range=="day") {
                tripCompare.setText("Today, you're having less trips than yesterday.");
            } else {
                tripCompare.setText("This " + range+", you're having less trips than last "+range+".");
            }
        }


    }


}
