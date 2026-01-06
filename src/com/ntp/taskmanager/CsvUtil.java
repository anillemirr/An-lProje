package com.ntp.taskmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Basit CSV yardımcı sınıfı.
 *
 * <p>
 * - Virgül içeren alanları (",") tırnak ile sarılmış olarak destekler.
 * - Çift tırnak kaçışını ("") destekler.
 * </p>
 */
public final class CsvUtil {

    private CsvUtil() { }

    /**
     * CSV alanını güvenli şekilde yazar (escape).
     * Virgül veya tırnak içeriyorsa alanı tırnaklar.
     */
    public static String escape(String value) {
        if (value == null) return "";
        boolean needQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String v = value.replace("\"", "\"\"");
        return needQuotes ? "\"" + v + "\"" : v;
    }

    /**
     * Tek bir CSV satırını alanlara böler (parse).
     * Örn: a,"b,c","d""e" -> [a, b,c, d"e]
     */
    public static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        if (line == null) return fields;

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // Eğer tırnak içindeysek ve sonraki karakter de tırnak ise => escaped quote ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // bir sonraki tırnağı atla
                } else {
                    inQuotes = !inQuotes; // quote aç/kapat
                }
            } else if (ch == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        fields.add(current.toString());
        return fields;
    }
}
