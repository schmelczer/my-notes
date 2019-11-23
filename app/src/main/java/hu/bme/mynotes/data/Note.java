package hu.bme.mynotes.data;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Entity(tableName = "notes")
public class Note {
    @Ignore
    private static String EMPTY_TITLE = "No title given";
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    public Long id;
    @ColumnInfo(name = "content")
    public String content;
    @Ignore
    private String title;
    @Ignore
    private List<String> tags;

    public List<String> getTags() {
        if (tags != null) {
            return tags;
        }

        if (content == null) {
            return new ArrayList<>();
        }

        List<String> matches = new ArrayList<>();
        Matcher matcher = Pattern.compile("#\\w+").matcher(content);

        while (matcher.find()) {
            matches.add(matcher.group());
        }

        tags = matches;

        return matches;
    }

    public String getTitle() {
        if (title != null) {
            return title;
        }

        if (content == null) {
            return EMPTY_TITLE;
        }

        Matcher matcher = Pattern.compile("# .+\n").matcher(content);
        String result = matcher.find() ? matcher.group().substring(1).trim() : EMPTY_TITLE;
        title = result;
        return result;
    }
}
