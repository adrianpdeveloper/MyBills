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

    //Timestamp del dia de hoy
    public static Timestamp getTodayTimestamp() {
        return new Timestamp(new Date());
    }

    //Timestamp del lunes de esta semana
    public static Timestamp getMondayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del domingo de esta semana
    public static Timestamp getSundayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.add(Calendar.DATE, 7); // Avanzar al próximo domingo
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del domingo de la semana anterior
    public static Timestamp getPreviousSundayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Obtener el día de la semana actual
        int daysToSubtract = currentDayOfWeek - Calendar.SUNDAY; // Calcular los días para retroceder
        if (currentDayOfWeek == Calendar.SUNDAY) {
            daysToSubtract = 7; // Si hoy es domingo, retrocede 7 días para obtener el domingo anterior
        }
        calendar.add(Calendar.DATE, -daysToSubtract); // Retroceder al domingo anterior
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del primer dia del mes
    public static Timestamp getFirstDayOfMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Establecer el día del mes en 1
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del ultimo dia del mes anterior
    public static Timestamp getLastDayOfPreviousMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1); // Retroceder al mes anterior
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); // Establecer el último día del mes
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del ultimo dia del mes
    public static Timestamp getLastDayOfMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); // Establece el día del mes al último día del mes
        resetTime(calendar); // Reiniciar las horas, minutos, segundos y milisegundos
        return new Timestamp(calendar.getTime());
    }

    // Reiniciar las horas, minutos, segundos y milisegundos
    private static void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    //Convierte de string a timestamp
    public static Timestamp stringToTimestamp(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
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

    //Convierte de timestamp a string
    public static String timestampToString(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(timestamp.toDate());
    }
}
