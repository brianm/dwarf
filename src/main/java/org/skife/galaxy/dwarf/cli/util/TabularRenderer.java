package org.skife.galaxy.dwarf.cli.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

public class TabularRenderer
{
    public static void render(List<List<String>> rows)
    {
        if (rows.isEmpty()) {
            return;
        }

        if (System.console() != null) {

            List<String> first = rows.get(0);
            int[] widths = new int[first.size()];

            for (List<String> row : rows) {
                int idx = 0;
                for (String value : row) {
                    widths[idx] = widths[idx] > value.length() ? widths[idx] : value.length();
                    idx++;
                }
            }

            for (List<String> row : rows) {
                int idx = 0;
                List<String> new_row = Lists.newArrayListWithExpectedSize(row.size());
                for (String value : row) {
                    new_row.add(Strings.padEnd(value, widths[idx], ' '));
                    idx++;
                }
                System.out.println(Joiner.on("  ").join(new_row));
            }
        }
        else {
            for (List<String> row : rows) {
                System.out.println(Joiner.on("\t").join(row));
            }
        }

    }
}
