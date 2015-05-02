

$(document).ready(function() {
    $.validator.methods._remote = $.validator.methods.remote;
    var timer = 0;
    $.validator.methods.remote = function () {
        clearTimeout(timer);

        var args = arguments;

        timer = setTimeout(function() {
            $.validator.methods._remote.apply(this, args);
        }.bind(this), 500);

        return "pending";
    };

    $("#regForm").validate({
        rules: {
            username: {
                required: true,
                minlength: 3,
                remote: {
                    url: window.usernameCheckUrl,
                    type: "get",
                    data: {
                        username: function () {
                            return $("#username").val();
                        }
                    }
                }
            },
            email: {
                required: true,
                remote: {
                    url: window.emailCheckUrl,
                    type: "get",
                    data: {
                        email: function () {
                            return $("#email").val();
                        }
                    }
                }
            },
            password: {
                required: true,
                minlength: 6
            },
            password2: {
                required: true,
                equalTo: "#password"
            }
        },
        messages: {
            username: {
                required: "Enter username",
                minlength: "Too short username",
                remote: function (params) {
                    return "Username " + params + " already registered. Have you already registered? Contact admin if you forgot your password";
                }
            },
            email: {
                required: "Enter e-mail",
                remote: function (params) {
                    return "Email " + params + " already registered. Have you already registered? Contact admin if you forgot your password";
                }
            },
            password: {
                required: "Enter password",
                minlength: "Password too short"
            },
            password2: {
                required: "Enter password",
                equalTo: "Passwords do not match"
            }
        }
    });
});
