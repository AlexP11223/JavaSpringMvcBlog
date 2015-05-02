$(document).ready(function() {
    if (location.hash == '#comments') {
        $('.comments').get(0).scrollIntoView();
    }

    var $commentForm = $('#commentForm');

    if ($commentForm.length) {
        var converter = Markdown.getSanitizingConverter();

        var editor = new Markdown.Editor(converter);

        editor.run();

        $commentForm.validate({
            rules: {
                commentText: {
                    required: true
                }
            },
            messages: {
                commentText: {
                    required: "Empty comments not allowed"
                }
            }
        });

        var $commentErrorLabel = $('#commentError');
        var $submitCommentBtn = $commentForm.find(":submit");
        var $commentLoadingIndicator = $('#commentLoadingIndicator');

        // ajax submit comment form
        $commentForm.on('submit', function(e) {
            e.preventDefault();

            var data =  $commentForm.serializeArray();

            $commentErrorLabel.hide();

            if ($commentForm.valid()) {
                $submitCommentBtn.prop('disabled', true);
                $commentLoadingIndicator.show();

                $.ajax({
                    type: 'post',
                    url: $commentForm.attr('action'),
                    data: data,
                    success: function (data) {
                        if (data == 'ok') {
                            $commentForm[0].reset();
                            $('#wmd-preview').empty();

                            // reload comments list
                            $.ajax({
                                url: window.commentsReloadUrl,
                                success: function (s) {
                                    var $commentList = $('#commentList');

                                    $commentList.html(s);

                                    $commentList.children().last()[0].scrollIntoView();
                                    window.scrollBy(0, -100);
                                },
                                complete: function() {
                                    $submitCommentBtn.prop('disabled', false);
                                    $commentLoadingIndicator.hide();
                                }
                            });
                        }
                        else {
                            $submitCommentBtn.prop('disabled', false);
                            $commentLoadingIndicator.hide();
                            $commentErrorLabel.text(data);
                            $commentErrorLabel.show();
                        }
                    },
                    error: function () {
                        $submitCommentBtn.prop('disabled', true);
                        $commentLoadingIndicator.hide();
                        $commentErrorLabel.text('Failed to send request.');
                        $commentErrorLabel.show();
                    }
                });
            }
        });
    }
});