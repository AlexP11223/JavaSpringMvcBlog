$(document).ready(function() {
    var converter = Markdown.getSanitizingConverter();

    var editor = new Markdown.Editor(converter);

    editor.run();

    $("#profileForm").validate({
        rules: {
            websiteLink: {
                url: true
            },
            aboutText: {
                maxlength: 1000
            }
        }
    });
});
