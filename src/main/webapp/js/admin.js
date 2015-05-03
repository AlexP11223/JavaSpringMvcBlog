$(document).ready(function() {
    $("#postsContainer").on('click', 'a[data-action="hidePost"]', function(event){
        event.preventDefault();

        var postTitle = getPostTitle(this);

        var btn = $(this);

        var loadingIndicator = btn.closest('.post').find('.postaction-loading-indicator');

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

    $("#postsContainer").on('click', 'a[data-action="unhidePost"]', function(event){
        event.preventDefault();

        var btn = $(this);

        var loadingIndicator = btn.closest('.post').find('.postaction-loading-indicator');

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
});

