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
    private String previousContentTitle;  // used for caching
    @Ignore
    private String title;  // used for caching

    @Ignore
    private String previousContentTags;  // used for caching
    @Ignore
    private List<String> tags;  // used for caching

    public List<String> getTags() {
        if (content == null || !content.equals(previousContentTitle)) {
            previousContentTitle = content;

            List<String> matches = new ArrayList<>();

            if (content != null) {
                Matcher matcher = Pattern.compile("#\\w+").matcher(content);

                while (matcher.find()) {
                    matches.add(matcher.group());
                }
            }

            tags = matches;
        }

        return tags;
    }

    public String getTitle() {
        if (content == null || !content.equals(previousContentTags)) {
            previousContentTags = content;

            if (content == null) {
                title = EMPTY_TITLE;
            } else {
                Matcher matcher = Pattern.compile("# .+\n").matcher(content);
                title = matcher.find() ? matcher.group().substring(1).trim() : EMPTY_TITLE;
            }
        }

        return title;
    }
}
