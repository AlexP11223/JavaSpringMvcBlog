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

function scrollToViewIfNotVisible(element){
    var offset = element.offset().top;
    if(!element.is(":visible")) {
        element.css({"visiblity":"hidden"}).show();
        offset = element.offset().top;
        element.css({"visiblity":"", "display":""});
    }

    var visible_area_start = $(window).scrollTop();
    var visible_area_end = visible_area_start + window.innerHeight;

    if(offset < visible_area_start || offset > visible_area_end){
        $('html,body').animate({scrollTop: offset - window.innerHeight/3}, 300);
        return false;
    }
    return true;
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