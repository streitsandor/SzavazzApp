$(function () {
    console.log('DEBUG: Main oldal betöltve.');

    $('.poll-options button').on('click', function () {
        const $button = $(this);

        if ($button.prop('disabled')) {
            return;
        }

        $button
            .closest('.poll-options')
            .find('button')
            .removeClass('active');

        $button.addClass('active');
    });
});