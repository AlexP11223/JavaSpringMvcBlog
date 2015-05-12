$(document).ready(function() {
    var commentsContainer = $('.comments');

    if (location.hash == '#comments') {
        commentsContainer.get(0).scrollIntoView();
    }

    function addedCommentHighlight(newCommentId) {
        var newComment = $('.comment[data-commentId=' + newCommentId + ']');

        newComment.addClass('new-comment', 0);

        scrollToViewIfNotVisible(newComment);
    }

    var commentForm = $('#commentForm');

    var commentsFormValidationRules = {
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
    };

    if (commentForm.length) {
        var converter = Markdown.getSanitizingConverter();

        var editor = new Markdown.Editor(converter);

        editor.run();

        commentForm.validate(commentsFormValidationRules);

        // ajax submit comment form
        commentForm.on('submit', function(e) {
            e.preventDefault();

            var form = $(this);

            var commentErrorLabel = form.find('label.request-error');
            var buttons = form.find('button');
            var commentLoadingIndicator = form.find('.loading-indicator');

            var data = form.serializeArray();

            commentErrorLabel.hide();

            if (form.valid()) {
                buttons.prop('disabled', true);
                commentLoadingIndicator.show();

                $.ajax({
                    type: 'post',
                    dataType: 'json',
                    url: form.attr('action'),
                    data: data,
                    success: function (data) {
                        if (data.status == 'ok') {
                            form[0].reset();
                            form.find('.wmd-preview').empty();

                            var newCommentId = data.id;

                            // reload comments list
                            $.ajax({
                                url: window.commentsReloadUrl,
                                success: function (s) {
                                    var commentList = $('#commentList');

                                    commentList.html(s);

                                    addedCommentHighlight(newCommentId);
                                },
                                complete: function() {
                                    buttons.prop('disabled', false);
                                    commentLoadingIndicator.hide();
                                },
                                error: function() {
                                    showErrorDialog('Failed to send reload request. Try reloading page.');
                                }
                            });
                        }
                        else {
                            buttons.prop('disabled', false);
                            commentLoadingIndicator.hide();
                            commentErrorLabel.text(data.message);
                            commentErrorLabel.show();
                        }
                    },
                    error: function () {
                        buttons.prop('disabled', false);
                        commentLoadingIndicator.hide();
                        commentErrorLabel.text('Failed to send request.');
                        commentErrorLabel.show();
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

        var loadingIndicator = btn.closest('.comment').find('.loading-indicator');

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

                                    showErrorDialog('You are not allowed to delete this comment anymore. Deletion allowed only for 10 minutes.');
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

    commentsContainer.on('click', 'a[data-action="editComment"]', function(event){
        event.preventDefault();

        var btn = $(this);

        var comment = btn.closest('.comment');

        var otherForms = $('#commentList').find('form');
        otherForms.remove();

        var commentId = comment.attr('data-commentId');

        var sourceUrl = window.commentSourceUrl.replace('COMMENT_ID', commentId);

        var commentLoadingIndicator = comment.find('.loading-indicator');

        commentLoadingIndicator.show();

        $.ajax({
            url: sourceUrl,
            success: function (s) {
                commentLoadingIndicator.hide();

                var commentText = s;

                var idSuffix = '-' + new Date().getTime();

                var editorFormHtml = '<form action="' + btn.attr('data-href') + '" class="comment-form" method="post">' +
                    '<div class="form-group wmd-panel">' +
                    '    <div id="wmd-button-bar' + idSuffix + '"></div>' +
                    '    <textarea class="wmd-input" id="wmd-input' + idSuffix + '" name="commentText"></textarea>' +
                    '    <div id="wmd-preview' + idSuffix + '" class="wmd-panel wmd-preview"></div>' +
                    '</div>' +
                    '<button type="submit" class="btn btn-default">Save</button>' +
                    '<button type="button" onclick="$(this).closest(\'form\').remove()" class="btn btn-default">Cancel</button>' +
                    '<div><label class="error request-error"></label></div>' +
                    '</form>';

                comment.children('.post-actions').after(editorFormHtml);

                var form = comment.find('form');

                form.find('textarea').val(commentText);

                var converter = Markdown.getSanitizingConverter();

                var editor = new Markdown.Editor(converter, idSuffix);

                editor.run();

                form.validate(commentsFormValidationRules);

                form.find('textarea')[0].focus();

                form.on('submit', function(e) {
                    e.preventDefault();

                    var form = $(this);

                    var commentErrorLabel = form.find('label.request-error');
                    var buttons = form.find('button');
                    var cancelBtn = form.find('button[type="button"]');
                    var commentLoadingIndicator = form.find('.loading-indicator');

                    var data = form.serializeArray();

                    commentErrorLabel.hide();

                    if (form.valid()) {
                        buttons.prop('disabled', true);
                        commentLoadingIndicator.show();

                        $.ajax({
                            type: 'post',
                            url: form.attr('action'),
                            data: data,
                            success: function (data) {
                                if (data == 'ok') {
                                    form.remove();

                                    // reload comments list
                                    $.ajax({
                                        url: window.commentsReloadUrl,
                                        success: function (s) {
                                            var commentList = $('#commentList');

                                            commentList.html(s);
                                        },
                                        error: function() {
                                            showErrorDialog('Failed to send request. Try reloading page.');
                                        }
                                    });
                                }
                                else {
                                    commentLoadingIndicator.hide();

                                    if (data == 'expired') {
                                        showErrorDialog('You are not allowed to edit this comment anymore. Edition allowed only for 3 hours.');

                                        cancelBtn.prop('disabled', false);
                                    }
                                    else {
                                        buttons.prop('disabled', false);
                                        commentErrorLabel.text(data);
                                        commentErrorLabel.show();
                                    }
                                }
                            },
                            error: function () {
                                buttons.prop('disabled', false);
                                commentLoadingIndicator.hide();
                                commentErrorLabel.text('Failed to send request.');
                                commentErrorLabel.show();
                            }
                        });
                    }
                });
            },
            error: function() {
                commentLoadingIndicator.hide();
                showErrorDialog('Failed to send request. Try reloading page.');
            }
        });
    });

    commentsContainer.on('click', 'a[data-action="addReply"]', function(event){
        event.preventDefault();

        var btn = $(this);

        var comment = btn.closest('.comment');

        var commentId = comment.attr('data-commentId');

        var otherForms = $('#commentList').find('form');
        otherForms.remove();

        var idSuffix = '-' + new Date().getTime();

        var editorFormHtml = '<form action="' + btn.attr('data-href') + '" class="comment-form" method="post">' +
            '<div class="form-group wmd-panel">' +
            '    <div id="wmd-button-bar' + idSuffix + '"></div>' +
            '    <textarea class="wmd-input" id="wmd-input' + idSuffix + '" name="commentText"></textarea>' +
            '    <div id="wmd-preview' + idSuffix + '" class="wmd-panel wmd-preview"></div>' +
            '</div>' +
            '<input type="hidden" name="parentId" value="' + commentId + '"/>' +
            '<button type="submit" class="btn btn-default">Send</button>' +
            '<div><label class="error request-error"></label></div>' +
            '</form>';

        comment.children('.post-actions').after(editorFormHtml);

        var form = comment.find('form');

        var converter = Markdown.getSanitizingConverter();

        var editor = new Markdown.Editor(converter, idSuffix);

        editor.run();

        form.validate(commentsFormValidationRules);

        form.find('textarea')[0].focus();

        form.on('submit', function(e) {
            e.preventDefault();

            var form = $(this);

            var commentErrorLabel = form.find('label.request-error');
            var buttons = form.find('button');
            var commentLoadingIndicator = form.find('.loading-indicator');

            var data = form.serializeArray();

            commentErrorLabel.hide();

            if (form.valid()) {
                buttons.prop('disabled', true);
                commentLoadingIndicator.show();

                $.ajax({
                    type: 'post',
                    dataType: 'json',
                    url: form.attr('action'),
                    data: data,
                    success: function (data) {
                        if (data.status == 'ok') {
                            form.remove();

                            var newCommentId = data.id;

                            // reload comments list
                            $.ajax({
                                url: window.commentsReloadUrl,
                                success: function (s) {
                                    var commentList = $('#commentList');

                                    commentList.html(s);

                                    addedCommentHighlight(newCommentId);
                                },
                                error: function() {
                                    showErrorDialog('Failed to send reload request. Try reloading page.');
                                }
                            });
                        }
                        else {
                            commentLoadingIndicator.hide();
                            buttons.prop('disabled', false);
                            commentErrorLabel.text(data.message);
                            commentErrorLabel.show();
                        }
                    },
                    error: function () {
                        buttons.prop('disabled', false);
                        commentLoadingIndicator.hide();
                        commentErrorLabel.text('Failed to send request.');
                        commentErrorLabel.show();
                    }
                });
            }
        });
    });
});