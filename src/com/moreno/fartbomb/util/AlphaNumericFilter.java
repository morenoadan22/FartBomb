package com.moreno.fartbomb.util;

import android.text.*;

public class AlphaNumericFilter implements InputFilter {

    /**
     * Filter that allows only alpha numberic characters [a-zA-Z] || [0-9]
     * 
     * @returns an empty {@link CharSequence} if it does not match the regex expression
     * 
     * @see android.text.InputFilter#filter(java.lang.CharSequence, int, int, android.text.Spanned, int, int)
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Character.valueOf('-');
        Character.valueOf('_');
        for (int i = start; i < end; i++) {
            if (!Character.isLetterOrDigit(source.charAt(i)) && source.charAt(i) != '-' && source.charAt(i) != '_') {
                return "";
            }
        }
        return null;
    }

}
