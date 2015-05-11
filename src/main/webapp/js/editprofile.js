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

    var uploadBtn = $('#uploadAvatar');
    var removeBtn = $('#removeAvatar');
    var pb = $('#avatarUploadProgress').find('.progress-bar');
    var avatarImg = $('#avatarImg');
    var avatarErrorLabel = $('#avatarError');
    var avatarSuccessLabel = $('#avatarSuccess');

    $('#avatarFileUploadInput').fileupload({
        url: window.avatarUploadUrl,
        dataType: "json",
        send: function (e, data) {
            pb.css('width', '0');
            pb.switchClass('progress-bar-danger', 'progress-bar-success', 0);
            pb.parent().show();

            avatarErrorLabel.hide();
            avatarSuccessLabel.hide();

            uploadBtn.addClass('disabled');
            removeBtn.addClass('disabled');
        },
        done: function (e, data) {
            if (data.result.status == 'ok') {
                avatarImg.attr('src', window.imgBaseUrl + data.result.link);

                removeBtn.show();

                avatarSuccessLabel.show();
            }
            else {
                pb.switchClass('progress-bar-success', 'progress-bar-danger');

                var errMsg = 'Failed to upload, ' + data.result.status;

                if (data.result.status == 'invalid_format') {
                    errMsg = 'Only JPG and PNG allowed.';
                }

                avatarErrorLabel.text(errMsg);
                avatarErrorLabel.show();
            }
        },
        fail: function (e, data) {
            pb.switchClass('progress-bar-success', 'progress-bar-danger');

            avatarErrorLabel.text('Failed to upload picture. Check that it is PNG or JPG and not exceeds 1 MB');
            avatarErrorLabel.show();
        },
        always: function (e, data) {
            uploadBtn.removeClass('disabled');
            removeBtn.removeClass('disabled');
        },
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            pb.css('width', progress + '%');
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');

    removeBtn.click(function() {
        pb.parent().hide();

        avatarErrorLabel.hide();
        avatarSuccessLabel.hide();

        uploadBtn.addClass('disabled');
        removeBtn.addClass('disabled');

        var loadingIndicator = $('.loading-indicator');
        loadingIndicator.show();

        $.ajax({
            type: 'post',
            url: removeBtn.attr('data-href'),
            success: function (data) {
                if (data == 'ok') {
                    avatarImg.attr('src', window.noAvatarImgUrl);

                    removeBtn.hide();
                }
                else {
                    avatarErrorLabel.text('Error: ' + data + '. Try reloading page.');
                }
            },
            error: function () {
                avatarErrorLabel.text('Failed to send request. Try reloading page.');
            },
            complete: function() {
                loadingIndicator.hide();

                uploadBtn.removeClass('disabled');
                removeBtn.removeClass('disabled');
            }
        });
    });
});
