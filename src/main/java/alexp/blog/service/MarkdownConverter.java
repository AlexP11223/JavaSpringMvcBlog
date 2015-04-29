package alexp.blog.service;

import com.github.rjeschke.txtmark.Processor;

public class MarkdownConverter {

    public static String toHtml(String input) {
        return Processor.process(input, true);
    }
}
