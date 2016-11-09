package com.sdklite.aapt;

import java.util.Comparator;

/**
 * This class is translated from android framework source code
 * 
 * @author johnsonlee
 *
 */
abstract class Unicode {

    public static int utf8_to_utf16_length(final byte[] u8) {
        int u16len = 0;

        for (int i = 0, n = u8.length; i < n;) {
            u16len++;

            final int u8len = utf8_codepoint_len(u8[i]);
            final int codepoint = utf8_to_utf32_codepoint(u8, u8len);

            if (codepoint > 0xffff) {
                u16len++;
            }

            i += u8len;
        }

        return u16len;
    }

    public static int utf8_to_utf32_codepoint(final byte[] src, final int len) {
        int unicode = 0;

        switch (len) {
        case 1:
            unicode = src[0];
            break;
        case 2:
            unicode = src[0] & 0x1f;
            unicode = utf8_shift_and_mask(unicode, src[1]);
            break;
        case 3:
            unicode = src[0] & 0x0f;
            unicode = utf8_shift_and_mask(unicode, src[1]);
            unicode = utf8_shift_and_mask(unicode, src[2]);
            break;
        case 4:
            unicode = src[0] & 0x07;
            unicode = utf8_shift_and_mask(unicode, src[1]);
            unicode = utf8_shift_and_mask(unicode, src[2]);
            unicode = utf8_shift_and_mask(unicode, src[3]);
            break;
        default:
            return 0xffff;
        }

        return unicode;
    }

    public static final int utf8_shift_and_mask(final int codepoint, final byte b) {
        int cp = codepoint;
        cp <<= 6;
        cp |= 0x3f & b;
        return cp;
    }

    public static int utf8_codepoint_len(final byte b) {
        return ((0xe5000000 >> ((b >> 3) & 0x1e)) & 3) + 1;
    }

    public static int strcmp16(final char[] s1, final char[] s2) {
        int d = 0;

        for (int i = 0, n = Math.min(s1.length, s2.length); i < n; i++) {
            if (0 != (d = s1[i] - s2[i])) {
                break;
            }
        }

        return d;
    }

    public static int strcmp16(final String s1, final String s2) {
        return strcmp16(s1.toCharArray(), s2.toCharArray());
    }

    public static final Comparator<CharSequence> UTF16_COMPARATOR = new Comparator<CharSequence>() {
        @Override
        public int compare(final CharSequence s1, final CharSequence s2) {
            return strcmp16(s1.toString(), s2.toString());
        }
    };

    private Unicode() {
    }

}
