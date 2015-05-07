$(document).ready(function() {
    var commentsContainer = $('.comments');

    if (location.hash == '#comments') {
        commentsContainer.get(0).scrollIntoView();
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

    // delete expired delete/edit buttons
    setInterval(function() {
        $('[data-maxdate]').each(function() {
            var maxTime = $(this).attr('data-maxdate');
            if (maxTime != '') {
                var currTime = new Date().getTime();

                if (currTime > parseInt(maxTime)) {
                    $(this).remove();
                }
            }
        });
    }, 1000);

    commentsContainer.on('click', 'a[data-action="deleteComment"]', function(event){
        event.preventDefault();

        var btn = $(this);

        var loadingIndicator = btn.closest('.comment').find('.commentaction-loading-indicator');

        bootbox.dialog({
            title: 'Delete comment',
            message: 'Are you sure you want to delete this comment?',
            buttons: {
                cancel: {
                    label: 'Cancel'
                },
                main: {
                    label: 'Delete',
                    className: 'btn-danger',
                    callback: function() {
                        loadingIndicator.show();

                        $.ajax({
                            type: 'post',
                            url: btn.attr('data-href'),
                            success: function (data) {
                                if (data == 'ok') {
                                    // reload comments list
                                    $.ajax({
                                        url: window.commentsReloadUrl,
                                        success: function (s) {
                                            var $commentList = $('#commentList');

                                            $commentList.html(s);
                                        }
                                    });
                                }
                                else if (data == 'expired') {
                                    loadingIndicator.hide();

                                    showErrorDialog('You not allowed to delete this comment anymore. Deletion allowed only for 10 minutes.');
                                }
                                else {
                                    loadingIndicator.hide();

                                    showErrorDialog('Error: ' + data + '. Try reloading page.');
                                }
                            },
                            error: function () {
                                loadingIndicator.hide();

                                showErrorDialog('Failed to send request. Try reloading page.');
                            }
                        });
                    }
                }
            }
        });
    });
});