package com.boss.bosssearchservice.util;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;

import java.time.LocalDateTime;
import java.util.List;

public class CanalColumnUtil {
    public static String getString(List<Column> columns, String name) {
        return columns.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .map(Column::getValue)
                .orElse(null);
    }

    public static Long getLong(List<Column> columns, String name) {
        String value = getString(columns, name);
        return value == null ? null : Long.valueOf(value);
    }

    public static LocalDateTime getTime(List<Column> columns, String name) {
        String value = getString(columns, name);
        return value == null ? null : LocalDateTime.parse(value.replace(" ", "T"));
    }
}
