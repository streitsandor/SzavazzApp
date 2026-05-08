(function (window, $) {
    'use strict';

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

    function initMainPage() {
        console.log('DEBUG: Main oldal betöltve.');
    }

    window.SzavazzAppMain = {
        normalizePercent,
        calculateVotePercent,
        buildProgressWidth,
        applyProgressValue,
        initMainPage
    };

    $(function () {
        initMainPage();
    });
})(window, window.jQuery || window.$);