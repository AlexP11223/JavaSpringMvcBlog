$(document).ready(function() {
    $("#emailForm").validate({
        rules: {
            email: {
                required: true,
                email: true
            },
            currentPassword: {
                required: true
            }
        },
        messages: {
            email: {
                required: "Enter e-mail"
            },
            currentPassword: {
                required: "Enter password"
            }
        }
    });

    $("#passwordForm").validate({
        rules: {
            currentPassword: {
                required: true
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
            password: {
                required: "Enter password",
                minlength: "Password too short"
            },
            password2: {
                required: "Enter password",
                equalTo: "Passwords do not match"
            },
            currentPassword: {
                required: "Enter password"
            }
        }
    });
});
