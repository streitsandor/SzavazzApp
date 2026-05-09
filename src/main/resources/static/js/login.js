(function (window) {
    'use strict';

    const App = window.SzavazzApp;

    window.SzavazzAppLogin = {
        initPasswordToggle: App.utils.initPasswordToggle
    };

    $(function () {
        App.utils.initPasswordToggle(document);
    });
})(window);