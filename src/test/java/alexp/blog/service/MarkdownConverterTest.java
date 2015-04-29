package alexp.blog.service;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MarkdownConverterTest {

    @Test
    public void shouldReturnHtmlAndEscapeScript() {
        String html = MarkdownConverter.toHtml("hello **world**! <script>alert('pwnd');</script>");

        assertThat(html, containsString("<strong>"));
        assertThat(html, not(containsString("<script>")));
    }
}