package com.example.recipeplatform.util;

import java.util.Locale;

public final class TextNormalizer {
    private TextNormalizer() {}

    public static String normalize(String value) {
        if (value == null) return null;
        String text = value.trim().replaceAll("\\s+", " ");
        if (text.isEmpty()) return text;
        StringBuilder result = new StringBuilder(text.length());
        boolean capitalize = true;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (capitalize && Character.isLetter(current)) {
                result.append(String.valueOf(current).toUpperCase(Locale.ROOT));
                capitalize = false;
            } else {
                result.append(current);
            }
            if (current == '.' || current == '!' || current == '?') {
                capitalize = true;
            }
        }
        return result.toString();
    }
}
