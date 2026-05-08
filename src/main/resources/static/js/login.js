(function (window, $) {
    'use strict';

    function initPasswordToggle(root) {
        const $root = $(root || document);

        $root.find('#togglePassword')
            .off('click.szavazzappLogin')
            .on('click.szavazzappLogin', function () {
                const $password = $root.find('#password');
                const isPassword = $password.attr('type') === 'password';

                $password.attr('type', isPassword ? 'text' : 'password');

                $(this)
                    .find('i')
                    .toggleClass('fa-eye fa-eye-slash');
            });
    }

    window.SzavazzAppLogin = {
        initPasswordToggle
    };

    $(function () {
        initPasswordToggle(document);
    });
})(window, window.jQuery || window.$);