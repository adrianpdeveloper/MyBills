package com.mybills.utils;

import android.util.Log;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormater {
    public DateFormater() {
    }

    public static Timestamp getTodayTimestamp() {
        return new Timestamp(new Date());
    }

    public static Timestamp getMondayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    public static Timestamp getSundayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.add(Calendar.DATE, 7); // Avanzar al próximo domingo
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    public static Timestamp getFirstDayOfMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Establecer el día del mes en 1
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    public static Timestamp getLastDayOfMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); // Establece el día del mes al último día del mes
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }
    private static void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static Timestamp stringToTimestamp(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                return new Timestamp(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String timestampToString(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(timestamp.toDate());
    }
}
