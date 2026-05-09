(function (window, $) {
    'use strict';

    const App = window.SzavazzApp || {};

    function normalizePercent(value) {
        const number = Number(value);

        if (!Number.isFinite(number)) {
            return 0;
        }

        return Math.min(100, Math.max(0, Math.round(number)));
    }

    function calculateVotePercent(voteCount, totalVotes) {
        const votes = Number(voteCount);
        const total = Number(totalVotes);

        if (!Number.isFinite(votes) || !Number.isFinite(total) || total <= 0) {
            return 0;
        }

        return normalizePercent((votes * 100) / total);
    }

    function buildProgressWidth(percent) {
        return `${normalizePercent(percent)}%`;
    }

    function applyProgressValue(progressElement, percent) {
        const normalizedPercent = normalizePercent(percent);

        if (!progressElement) {
            return normalizedPercent;
        }

        progressElement.setAttribute('aria-valuenow', String(normalizedPercent));

        const progressBar = progressElement.querySelector('.progress-bar');

        if (progressBar) {
            progressBar.style.width = buildProgressWidth(normalizedPercent);
        }

        return normalizedPercent;
    }

    function animateProgressBars(root) {
        const container = root || document;
        const bars = container.querySelectorAll('.js-progress-bar');

        const requestFrame = window.requestAnimationFrame || function (callback) {
            return window.setTimeout(callback, 0);
        };

        bars.forEach(function (bar) {
            const percent = normalizePercent(bar.dataset.percent);
            const progressElement = bar.closest('.progress');

            if (progressElement) {
                progressElement.setAttribute('aria-valuenow', String(percent));
            }

            bar.style.width = '0%';

            requestFrame(function () {
                requestFrame(function () {
                    bar.style.width = buildProgressWidth(percent);
                });
            });
        });
    }

    function setupCsrfForAjax() {
        const token = $('meta[name="_csrf"]').attr('content');
        const header = $('meta[name="_csrf_header"]').attr('content');

        if (!token || !header) {
            return;
        }

        $.ajaxSetup({
            beforeSend: function (xhr) {
                xhr.setRequestHeader(header, token);
            }
        });
    }

    function showModalAlert($modal, type, message) {
        const $alert = $modal.find('.js-modal-alert');

        $alert
            .removeClass('d-none alert-success alert-danger alert-warning')
            .addClass(`alert-${type}`)
            .text(message);
    }

    function hideModalAlert($modal) {
        $modal.find('.js-modal-alert')
            .addClass('d-none')
            .removeClass('alert-success alert-danger alert-warning')
            .text('');
    }

    function extractErrorMessage(xhr, fallbackMessage) {
        if (xhr && xhr.responseJSON && xhr.responseJSON.message) {
            return xhr.responseJSON.message;
        }

        return fallbackMessage || 'A művelet nem sikerült.';
    }

    function setButtonLoading($button, loading, loadingText) {
        if (!$button || !$button.length) {
            return;
        }

        if (loading) {
            if (!$button.data('original-html')) {
                $button.data('original-html', $button.html());
            }

            $button
                .prop('disabled', true)
                .html(`<span class="spinner-border spinner-border-sm me-1"></span>${loadingText || 'Feldolgozás...'}`);

            return;
        }

        const originalHtml = $button.data('original-html');

        $button.prop('disabled', false);

        if (originalHtml) {
            $button.html(originalHtml);
        }
    }

    function initPasswordToggle(root) {
        const $root = $(root || document);

        $root.find('#togglePassword')
            .off('click.szavazzappPasswordToggle')
            .on('click.szavazzappPasswordToggle', function () {
                const $password = $root.find('#password');
                const isPassword = $password.attr('type') === 'password';

                $password.attr('type', isPassword ? 'text' : 'password');

                $(this)
                    .find('i')
                    .toggleClass('fa-eye fa-eye-slash');
            });
    }

    App.utils = {
        normalizePercent,
        calculateVotePercent,
        buildProgressWidth,
        applyProgressValue,
        animateProgressBars,
        setupCsrfForAjax,
        showModalAlert,
        hideModalAlert,
        extractErrorMessage,
        setButtonLoading,
        initPasswordToggle
    };

    window.SzavazzApp = App;
})(window, window.jQuery || window.$);