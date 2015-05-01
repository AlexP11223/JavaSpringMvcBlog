package alexp.blog.service;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MarkdownConverterTest {

    @Test
    public void shouldReturnHtmlAndEscapeScript() {
        String html = MarkdownConverter.toHtml("hello **world**! <script>alert('pwnd');</script>");

        assertThat(html, containsString("<strong>"));
        assertThat(html, not(containsString("<script>")));
    }

    @Test
    public void shouldExtractLinks() {
        String input = "Species and their hybrids, How simply are these facts! How\n" +
                "strange that the pollen of each But we may thus have\n" +
                "[succeeded][1] in selecting so many exceptions to this rule.\n" +
                "but the species would not all the same species living on the\n" +
                "White Mountains, in the arctic regions of that large island.\n" +
                "The exceptions which are now large, and triumphant, and\n" +
                "which are known to every naturalist: scarcely a single\n" +
                "[character][2] in the descendants of the Glacial period,\n" +
                "would have been of use to the plants, have been accumulated\n" +
                "and if, in both regions.\n" +
                "\n" +
                "Supposed to be extinct and unknown, form. We have seen that\n" +
                "it yields readily, when subjected as [under confinement][3],\n" +
                "to new and improved varieties will have been much\n" +
                "\n" +
                "\n" +
                "  [1]: http://www.google.com\n" +
                "  [2]: http://docs.python.org/library/index.html\n" +
                "  [3]: http://www.kungfugrippe.com/";

        List<String> links = MarkdownConverter.extractLinks(input);

        assertThat(links, hasItems("  [1]: http://www.google.com",
                "  [2]: http://docs.python.org/library/index.html",
                "  [3]: http://www.kungfugrippe.com/"));
    }

    @Test
    public void shouldReturnEmptyLinksWhenInputHasNoLinks() {
        String input = "text";

        List<String> links = MarkdownConverter.extractLinks(input);

        assertThat(links.size(), is(equalTo(0)));
    }
}