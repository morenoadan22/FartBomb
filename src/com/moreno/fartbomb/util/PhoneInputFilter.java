package com.moreno.fartbomb.util;

import android.text.*;

public class PhoneInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            if (!Character.isDigit(source.charAt(i))) {
                return "";
            }
        }
        return null;
    }

}
