package com.mybills.utils;

import com.google.firebase.Timestamp;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        //Reinicia las horas, minutos, segundos y milisegundos
        resetTime(calendar);
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del domingo de esta semana
    public static Timestamp getSundayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.add(Calendar.DATE, 7); // Avanzar al próximo domingo

        //Reinicia las horas, minutos, segundos y milisegundos
        resetTime(calendar);
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

        //Reinicia las horas, minutos, segundos y milisegundos
        resetTime(calendar);
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del primer dia del mes
    public static Timestamp getFirstDayOfMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); //Establece el día del mes en 1

        //Reinicia las horas, minutos, segundos y milisegundos
        resetTime(calendar);
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del ultimo dia del mes anterior
    public static Timestamp getLastDayOfPreviousMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();

        //Retroceder al mes anterior
        calendar.add(Calendar.MONTH, -1);

        //Establecer el último día del mes
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        //Reinicia las horas, minutos, segundos y milisegundos
        resetTime(calendar);
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del ultimo dia del mes actual
    public static Timestamp getLastDayOfMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();

        //Establece el día del mes al último día del mes
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        //Reinicia las horas, minutos, segundos y milisegundos
        resetTime(calendar);
        return new Timestamp(calendar.getTime());
    }

    //Timestamp del ultimo dia del mes otorgado
    public static Timestamp getLastDayOfXMonthTimestamp(String month) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
            Date date = sdf.parse(month);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

            return new Timestamp(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Timestamp del ultimo dia del mes anterior al otorgado
    public static Timestamp getLastDayOfPreviousXMonthTimestamp(String month) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
            Date date = sdf.parse(month);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            //Resta un mes al mes actual
            calendar.add(Calendar.MONTH, -1);

            //Establece el día en el último día del mes
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));


            return new Timestamp(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Timestamp del dia anterior al actual
    public static Timestamp getDayBefore(Timestamp timestamp) {
        Date date = timestamp.toDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(Calendar.DAY_OF_MONTH, -1);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        Date fechaAnterior = calendar.getTime();

        return new Timestamp(fechaAnterior);
    }

    //Timestamp del dia posterior al actual
    public static Timestamp getDayAfter(Timestamp timestamp) {
        Date date = timestamp.toDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(Calendar.DAY_OF_MONTH, 1);

        //Establece la hora, minuto, segundo y milisegundo en 0 para obtener el primer minuto del día siguiente
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date fechaSiguiente = calendar.getTime();

        //Convierte la fecha al timestamp de Firebase y devolverlo
        return new Timestamp(fechaSiguiente);
    }


    public static String getCurrentMonthYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date()); //Establece la fecha actual

        //Obtiene el mes y el año actual
        int month = calendar.get(Calendar.MONTH) + 1; //Sumar 1 porque los meses en Calendar van de 0 a 11
        int year = calendar.get(Calendar.YEAR);

        // Crear un string en formato "mes/año"
        String formattedDate = month + "/" + year;

        // Crear un objeto Timestamp con la fecha actual
        return formattedDate;
    }

    public static String convertMonthYear(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(inputDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int month = calendar.get(Calendar.MONTH); // 0-based index
            int year = calendar.get(Calendar.YEAR);

            DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
            String monthName = symbols.getMonths()[month];

            return monthName + " de " + year;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Reinicia las horas, minutos, segundos y milisegundos
    private static void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    //Convierte de String a Timestamp
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

    //Convierte de Timestamp a string formato dd/MM/yyyy
    public static String timestampToStringLong(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(timestamp.toDate());
    }

    //Convierte de Timestamp a string dd/MM/yy
    public static String timestampToStringShort(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        return sdf.format(timestamp.toDate());
    }
}
