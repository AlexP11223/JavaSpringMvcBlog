function getPostId(el) {
    return $(el).closest('.post').attr('data-post-id');
}

function getPostTitle(el) {
    return $(el).closest('.post').attr('data-post-title');
}

function showErrorDialog(text) {
    bootbox.dialog({
        message: text,
        buttons: {
            ok: {
                label: 'OK'
            }
        }
    });
}

$(document).ready(function() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });


    $.ajax({
        dataType: "json",
        url: window.postsUrl,
        success: function (data) {
            var items = [];
            $.each(data, function(key, val) {
                items.push('<li><a href="' + window.postsUrl + '/' + val.id + '">' + val.title + '</a></li>');
            });
            $("<ul/>", {
                class: 'list-no-indent',
                html: items.join('')
            }).appendTo('#latestPosts div');
        }
    });
});