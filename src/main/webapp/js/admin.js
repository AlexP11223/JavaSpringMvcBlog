$(document).ready(function() {
    var postsContainer = $("#postsContainer");

    postsContainer.on('click', 'a[data-action="hidePost"]', function(event){
        event.preventDefault();

        var postTitle = getPostTitle(this);

        var btn = $(this);

        var loadingIndicator = btn.closest('.post').find('.postaction-loading-indicator');

        var delBtn = btn.closest('.post-actions').find('a[data-action="deletePost"]');

        bootbox.dialog({
            title: 'Hide post',
            message: 'Are you sure you want to hide post <b>' + postTitle + '</b> from other users?',
            buttons: {
                cancel: {
                    label: 'Cancel'
                },
                main: {
                    label: 'Hide',
                    className: 'btn-primary',
                    callback: function() {
                        loadingIndicator.show();

                        $.ajax({
                            type: 'post',
                            url: btn.attr('data-href'),
                            success: function (data) {
                                loadingIndicator.hide();

                                if (data == 'ok') {
                                    btn.attr('data-action', 'unhidePost');
                                    btn.attr('data-href', btn.attr('data-href').replace('hide', 'unhide'));
                                    btn.html('unhide');

                                    btn.closest('.post').find('.post-content').append('<div class="hidden-post"><span>Not visible for users</span></div>');

                                    delBtn.show();
                                }
                                else {
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

    postsContainer.on('click', 'a[data-action="unhidePost"]', function(event){
        event.preventDefault();

        var btn = $(this);

        var loadingIndicator = btn.closest('.post').find('.postaction-loading-indicator');

        var delBtn = btn.closest('.post-actions').find('a[data-action="deletePost"]');

        loadingIndicator.show();

        $.ajax({
            type: 'post',
            url: btn.attr('data-href'),
            success: function (data) {
                loadingIndicator.hide();

                if (data == 'ok') {
                    btn.attr('data-action', 'hidePost');
                    btn.attr('data-href', btn.attr('data-href').replace('unhide', 'hide'));
                    btn.html('hide');

                    btn.closest('.post').find('.hidden-post').remove();

                    delBtn.hide();
                }
                else {
                    showErrorDialog('Error: ' + data + '. Try reloading page.');
                }
            },
            error: function () {
                loadingIndicator.hide();

                showErrorDialog('Failed to send request. Try reloading page.');
            }
        });
    });

    postsContainer.on('click', 'a[data-action="deletePost"]', function(event){
        event.preventDefault();

        var postTitle = getPostTitle(this);
        var postId = getPostId(this);

        var btn = $(this);

        var loadingIndicator = btn.closest('.post').find('.postaction-loading-indicator');

        bootbox.dialog({
            title: 'Delete post',
            message: 'Are you sure you want to delete post <b>' + postTitle + '</b>? You will not be able to recover it.',
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
                                loadingIndicator.hide();

                                if (data == 'ok') {
                                    btn.closest('.post').remove();

                                    if (window.location.href.indexOf('posts/' + postId) > -1) {
                                        window.location.href = window.location.href.replace('/' + postId, '');
                                    }
                                }
                                else {
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

