package alexp.blog.service;

import com.github.rjeschke.txtmark.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownConverter {

    public static String toHtml(String input) {
        return Processor.process(input, true);
    }

    public static List<String> extractLinks(String input) {
        Matcher m = Pattern.compile("\\s?\\s?\\[([^^\\]]+)\\]:\\s+(.+)$", Pattern.MULTILINE)
                .matcher(input);

        List<String> links = new ArrayList<>();
        while (m.find()) {
            links.add(m.group());
        }

        return links;
    }
}
