package com.mybills.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;

public class MoneyInputFilter implements InputFilter {

    private final int decimal;

    public MoneyInputFilter(int decimal) {
        this.decimal = decimal;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        //Formatea los importes
        boolean hasDecimalSeparator = dest.toString().contains(",");

        if (hasDecimalSeparator && dest.toString().substring(dest.toString().indexOf(",") + 1).length() >= decimal) {
            return "";
        }

        return null;
    }
}
