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
});