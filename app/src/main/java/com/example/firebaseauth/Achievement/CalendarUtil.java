package com.example.firebaseauth.Achievement;

import java.util.Calendar;
import java.util.Date;

public class CalendarUtil {

    public CalendarUtil() {

    }
    // get last week dates
    public Date[] getLastWeek(Calendar mCalendar) {
        // Monday
        mCalendar.add(Calendar.DATE, -7);
        Date mDateMonday = mCalendar.getTime();

        // Sunday
        mCalendar.add(Calendar.DATE, 6);
        Date mDateSunday = mCalendar.getTime();

        Date[] lastWeek= {atStartOfDay(mDateMonday), atEndOfDay(mDateSunday)};

        return lastWeek;
    }

    // get current week dates
    public Date[] getCurrentWeek(Calendar mCalendar) {
        // 1 = Sunday, 2 = Monday, etc.
        int day_of_week = mCalendar.get(Calendar.DAY_OF_WEEK);

        int monday_offset;
        if (day_of_week == 1) {
            monday_offset = -6;
        } else
            monday_offset = (2 - day_of_week); // need to minus back
        mCalendar.add(Calendar.DAY_OF_YEAR, monday_offset);

        Date mDateMonday = mCalendar.getTime();

        // return 6 the next days of current day (object cal save current day)
        mCalendar.add(Calendar.DAY_OF_YEAR, 6);
        Date mDateSunday = mCalendar.getTime();

        Date[] currentWeek = {atStartOfDay(mDateMonday), atEndOfDay(mDateSunday)};
        return currentWeek;
    }

    // get start and end date for last month
    public Date[] getLastMonth(Calendar mCalendar) {
        // add -1 month to previous month
        mCalendar.add(Calendar.MONTH, -1);
        // set DATE to 1, so first date of previous month
        mCalendar.set(Calendar.DATE, 1);

        Date firstDateOfPreviousMonth = mCalendar.getTime();

        // set actual maximum date of previous month
        mCalendar.set(Calendar.DATE, mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        //read it
        Date lastDateOfPreviousMonth = mCalendar.getTime();

        Date[] lastMonth = {atStartOfDay(firstDateOfPreviousMonth), atEndOfDay(lastDateOfPreviousMonth)};
        return lastMonth;
    }

    // get start and end date of current month
    public Date[] getCurrentMonth(Calendar mCalendar) {
        mCalendar.setTime(new Date());

        mCalendar.add(Calendar.MONTH, 0);
        mCalendar.set(Calendar.DATE, mCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date monthFirstDay = mCalendar.getTime();

        mCalendar.set(Calendar.DATE, mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date monthLastDay = mCalendar.getTime();

        Date[] currentMonth = {atStartOfDay(monthFirstDay), atEndOfDay(monthLastDay)};
        return currentMonth;
    }

    public Date[] getYesterday(Calendar mCalendar) {
        mCalendar.add(Calendar.DATE, -1);
        Date[] yesterday = {atStartOfDay(mCalendar.getTime()), atEndOfDay(mCalendar.getTime())};
        return yesterday;
    }

    public Date[] getToday(Calendar mCalendar) {
        Date[] today = {atStartOfDay(mCalendar.getTime()), atEndOfDay(mCalendar.getTime())};

        return today;
    }

    public Date atEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public Date atStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public String getDayOFWeek(Calendar mCalendar, Date date) {
        mCalendar.setTime(date);
        int day = mCalendar.get(Calendar.DAY_OF_WEEK);
        switch(day){
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";

        }

        return "Wrong Day";
    }

}
