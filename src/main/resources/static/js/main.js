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

        $progressElement.setAttribute('aria-valuenow', String(normalizedPercent));

        const progressBar = progressElement.querySelector('.progress-bar');

        if (progressBar) {
            progressBar.style.width = buildProgressWidth(normalizedPercent);
        }

        return normalizedPercent;
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

    function setButtonLoading($button, loading) {
        if (!$button.length) {
            return;
        }

        if (loading) {
            if (!$button.data('original-html')) {
                $button.data('original-html', $button.html());
            }

            $button
                .prop('disabled', true)
                .html('<span class="spinner-border spinner-border-sm me-1"></span>Feldolgozás...');
        } else {
            $button
                .prop('disabled', false)
                .html($button.data('original-html'));
        }
    }

    function openVoteModal($card) {
        const pollId = $card.attr('data-poll-id');
        const pollTitle = $card.attr('data-poll-title');

        const $modal = $('#votePollModal');
        const $optionsContainer = $('#votePollOptions');

        hideModalAlert($modal);
        $('.js-vote-error').addClass('d-none');

        $('#votePollId').val(pollId);
        $('#votePollTitle').text(pollTitle);

        $optionsContainer.empty();

        $card.find('.js-poll-option-data').each(function () {
            const optionId = $(this).attr('data-option-id');
            const optionLabel = $(this).attr('data-option-label');

            const $label = $('<label/>').addClass('vote-option-card');
            const $input = $('<input/>', {
                type: 'radio',
                name: 'optionId',
                value: optionId,
                class: 'form-check-input me-2'
            });

            const $text = $('<span/>')
                .addClass('vote-option-label')
                .text(optionLabel);

            $label.append($input, $text);
            $optionsContainer.append($label);
        });

        const modal = bootstrap.Modal.getOrCreateInstance($modal[0]);
        modal.show();
    }

    function submitVote(event) {
        event.preventDefault();

        const $modal = $('#votePollModal');
        const $button = $('.js-submit-vote');
        const pollId = $('#votePollId').val();
        const optionId = $('#votePollOptions input[name="optionId"]:checked').val();

        hideModalAlert($modal);

        if (!optionId) {
            $('.js-vote-error').removeClass('d-none');
            return;
        }

        setButtonLoading($button, true);

        $.ajax({
            url: `/api/polls/${pollId}/vote`,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                optionId: Number(optionId)
            })
        })
            .done(function () {
                window.location.reload();
            })
            .fail(function (xhr) {
                showModalAlert($modal, 'danger', extractErrorMessage(xhr, 'A szavazás nem sikerült.'));
            })
            .always(function () {
                setButtonLoading($button, false);
            });
    }

    function addCreateOptionRow(value) {
        const $container = $('#createPollOptions');
        const index = $container.find('.create-option-row').length + 1;

        if (index > 10) {
            return;
        }

        const $row = $('<div/>').addClass('input-group mb-2 create-option-row');

        const $input = $('<input/>', {
            type: 'text',
            class: 'form-control js-create-option-input',
            placeholder: `${index}. válaszlehetőség`,
            maxlength: 180,
            required: true,
            value: value || ''
        });

        const $button = $('<button/>', {
            type: 'button',
            class: 'btn btn-outline-danger js-remove-option',
            title: 'Opció törlése'
        }).html('<i class="fa-solid fa-xmark"></i>');

        $row.append($input, $button);
        $container.append($row);

        updateCreateOptionButtons();
    }

    function updateCreateOptionButtons() {
        const $rows = $('#createPollOptions .create-option-row');

        $('.js-add-option').prop('disabled', $rows.length >= 10);
        $('.js-remove-option').prop('disabled', $rows.length <= 2);
    }

    function resetCreatePollForm() {
        const $form = $('#createPollForm');
        const $modal = $('#createPollModal');

        $form.removeClass('was-validated');
        $form[0].reset();

        hideModalAlert($modal);

        $('#createPollOptions').empty();
        addCreateOptionRow();
        addCreateOptionRow();
    }

    function collectCreatePollPayload() {
        const topicIds = ($('#createPollTopics').val() || [])
            .map(value => Number(value))
            .filter(value => Number.isFinite(value));

        const options = $('.js-create-option-input')
            .map(function () {
                return $(this).val().trim();
            })
            .get()
            .filter(value => value.length > 0);

        return {
            title: $('#createPollTitle').val().trim(),
            description: $('#createPollDescription').val().trim(),
            topicIds,
            options
        };
    }

    function submitCreatePoll(event) {
        event.preventDefault();

        const form = event.currentTarget;
        const $form = $(form);
        const $modal = $('#createPollModal');
        const $button = $('.js-submit-create');

        hideModalAlert($modal);

        const payload = collectCreatePollPayload();

        if (!form.checkValidity() || payload.topicIds.length === 0 || payload.options.length < 2) {
            $form.addClass('was-validated');
            showModalAlert($modal, 'warning', 'Ellenőrizd a kötelező mezőket. Legalább 2 válaszlehetőség szükséges.');
            return;
        }

        setButtonLoading($button, true);

        $.ajax({
            url: '/api/polls',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload)
        })
            .done(function () {
                window.location.reload();
            })
            .fail(function (xhr) {
                showModalAlert($modal, 'danger', extractErrorMessage(xhr, 'A szavazás létrehozása nem sikerült.'));
            })
            .always(function () {
                setButtonLoading($button, false);
            });
    }

    function openDeleteModal($card) {
        const pollId = $card.attr('data-poll-id');
        const pollTitle = $card.attr('data-poll-title');

        const $modal = $('#deletePollModal');

        hideModalAlert($modal);

        $('#deletePollId').val(pollId);
        $('#deletePollTitle').text(pollTitle);

        const modal = bootstrap.Modal.getOrCreateInstance($modal[0]);
        modal.show();
    }

    function submitDeletePoll(event) {
        event.preventDefault();

        const $modal = $('#deletePollModal');
        const $button = $('.js-submit-delete');
        const pollId = $('#deletePollId').val();

        hideModalAlert($modal);
        setButtonLoading($button, true);

        $.ajax({
            url: `/api/polls/${pollId}`,
            method: 'DELETE'
        })
            .done(function () {
                window.location.reload();
            })
            .fail(function (xhr) {
                showModalAlert($modal, 'danger', extractErrorMessage(xhr, 'A törlés nem sikerült.'));
            })
            .always(function () {
                setButtonLoading($button, false);
            });
    }

    function initMainPage() {
        setupCsrfForAjax();

        $(document).on('click', '.js-open-vote-modal', function () {
            openVoteModal($(this).closest('.poll-card'));
        });

        $(document).on('change', '#votePollOptions input[name="optionId"]', function () {
            $('.vote-option-card').removeClass('is-selected');
            $(this).closest('.vote-option-card').addClass('is-selected');
            $('.js-vote-error').addClass('d-none');
        });

        $(document).on('submit', '#votePollForm', submitVote);

        $('#createPollModal').on('show.bs.modal', resetCreatePollForm);

        $(document).on('click', '.js-add-option', function () {
            addCreateOptionRow();
        });

        $(document).on('click', '.js-remove-option', function () {
            $(this).closest('.create-option-row').remove();
            updateCreateOptionButtons();
        });

        $(document).on('submit', '#createPollForm', submitCreatePoll);

        $(document).on('click', '.js-open-delete-modal', function () {
            openDeleteModal($(this).closest('.poll-card'));
        });

        $(document).on('submit', '#deletePollForm', submitDeletePoll);
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