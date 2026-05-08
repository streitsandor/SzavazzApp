$(function () {
    $('#togglePassword').on('click', function () {
        const $password = $('#password');
        const isPassword = $password.attr('type') === 'password';

        $password.attr('type', isPassword ? 'text' : 'password');
        $(this).find('i').toggleClass('fa-eye fa-eye-slash');
    });
});
