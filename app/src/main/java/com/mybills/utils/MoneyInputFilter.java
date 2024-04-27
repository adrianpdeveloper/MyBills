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
        // Check if the source contains a decimal separator
        boolean hasDecimalSeparator = dest.toString().contains(",");

        // If the source contains a decimal separator and it's being added after the allowed number of decimal digits,
        // we don't allow the addition
        if (hasDecimalSeparator && dest.toString().substring(dest.toString().indexOf(",") + 1).length() >= decimal) {
            return "";
        }

        return null;
    }
}
