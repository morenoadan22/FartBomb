package com.moreno.fartbomb.util;

import android.text.*;

public class EmailFilter implements InputFilter {

    private static char[] emailSymbols = new char[] { '!'// 33
            , '#'// 35
            , '$'// 36
            , '%'// 37
            , '&'// 38
            , 39// '
            , 42// *
            , 43// +
            , '-'// 45
            , '='// 61
            , '?'// 63
            , '^'// 94
            , '_'// 95
            , '`'// 96
            , '{'// 123
            , '|'// 124
            , '}'// 125
            , '~' // 126
    };

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
            if (!Character.isLetterOrDigit(source.charAt(i)) && !isEmailSymbol(source.charAt(i))) {
                return "";
            }
        }
        return null;
    }

    private boolean isEmailSymbol(char c) {
        boolean isInTable = false;
        for (char symbol : emailSymbols) {
            if (symbol == c) {
                isInTable = true;
                break;
            }
        }

        return isInTable;
    }
}
