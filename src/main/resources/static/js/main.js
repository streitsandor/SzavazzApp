(function (window, $) {
    'use strict';

    const App = window.SzavazzApp;
    const Utils = App && App.utils;

    // global.js nincs betöltve a main.js előtt
    if (!Utils) {
        throw new Error('Hibás betöltődés.');
    }

    function openVoteModal($card) {
        const pollId = $card.attr('data-poll-id');
        const pollTitle = $card.attr('data-poll-title');
        const $modal = $('#votePollModal');
        const $optionsContainer = $('#votePollOptions');

        Utils.hideModalAlert($modal);
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

        bootstrap.Modal.getOrCreateInstance($modal[0]).show();
    }

    function submitVote(event) {
        event.preventDefault();

        const $modal = $('#votePollModal');
        const $button = $('.js-submit-vote');
        const pollId = $('#votePollId').val();
        const optionId = $('#votePollOptions input[name="optionId"]:checked').val();

        Utils.hideModalAlert($modal);

        if (!optionId) {
            $('.js-vote-error').removeClass('d-none');
            return;
        }

        Utils.setButtonLoading($button, true);

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
                Utils.showModalAlert($modal, 'danger', Utils.extractErrorMessage(xhr, 'A szavazás nem sikerült.'));
            })
            .always(function () {
                Utils.setButtonLoading($button, false);
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

        Utils.hideModalAlert($modal);

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
        const payload = collectCreatePollPayload();

        Utils.hideModalAlert($modal);

        if (!form.checkValidity() || payload.topicIds.length === 0 || payload.options.length < 2) {
            $form.addClass('was-validated');
            Utils.showModalAlert($modal, 'warning', 'Ellenőrizd a kötelező mezőket. Legalább 2 válaszlehetőség szükséges.');
            return;
        }

        Utils.setButtonLoading($button, true);

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
                Utils.showModalAlert($modal, 'danger', Utils.extractErrorMessage(xhr, 'A szavazás létrehozása nem sikerült.'));
            })
            .always(function () {
                Utils.setButtonLoading($button, false);
            });
    }

    function addEditOptionRow(value, readonly) {
        const $container = $('#editPollOptions');
        const index = $container.find('.edit-option-row').length + 1;

        if (index > 10) {
            return;
        }

        const $row = $('<div/>').addClass('input-group mb-2 edit-option-row');

        const $input = $('<input/>', {
            type: 'text',
            class: 'form-control js-edit-option-input',
            placeholder: `${index}. válaszlehetőség`,
            maxlength: 180,
            required: true,
            value: value || ''
        });

        if (readonly) {
            $input.prop('readonly', true);
        }

        const $button = $('<button/>', {
            type: 'button',
            class: 'btn btn-outline-danger js-remove-edit-option',
            title: 'Opció törlése'
        }).html('<i class="fa-solid fa-xmark"></i>');

        if (readonly) {
            $button.prop('disabled', true);
        }

        $row.append($input, $button);
        $container.append($row);

        updateEditOptionButtons(readonly);
    }

    function updateEditOptionButtons(readonlyOptions) {
        const $rows = $('#editPollOptions .edit-option-row');

        $('.js-add-edit-option').prop('disabled', readonlyOptions || $rows.length >= 10);
        $('.js-remove-edit-option').prop('disabled', readonlyOptions || $rows.length <= 2);
    }

    function openEditModal($card) {
        const pollId = $card.attr('data-poll-id');
        const pollTitle = $card.attr('data-poll-title');
        const voteCount = Number($card.attr('data-vote-count') || 0);
        const readonlyOptions = voteCount > 0;

        const $modal = $('#editPollModal');
        const $form = $('#editPollForm');

        $form.removeClass('was-validated');
        Utils.hideModalAlert($modal);

        $('#editPollId').val(pollId);
        $('#editPollTitle').val(pollTitle);
        $('#editPollDescription').val($card.find('.poll-description').first().text().trim());

        const topicIds = $card.find('.js-poll-topic-data')
            .map(function () {
                return String($(this).attr('data-topic-id'));
            })
            .get();

        $('#editPollTopics').val(topicIds);

        $('#editPollOptions').empty();

        $card.find('.js-poll-option-data').each(function () {
            addEditOptionRow($(this).attr('data-option-label'), readonlyOptions);
        });

        $('#editPollOptionsLockedInfo').toggleClass('d-none', !readonlyOptions);

        bootstrap.Modal.getOrCreateInstance($modal[0]).show();
    }

    function collectEditPollPayload() {
        const topicIds = ($('#editPollTopics').val() || [])
            .map(value => Number(value))
            .filter(value => Number.isFinite(value));

        const options = $('.js-edit-option-input')
            .map(function () {
                return $(this).val().trim();
            })
            .get()
            .filter(value => value.length > 0);

        return {
            title: $('#editPollTitle').val().trim(),
            description: $('#editPollDescription').val().trim(),
            topicIds,
            options
        };
    }

    function submitEditPoll(event) {
        event.preventDefault();

        const form = event.currentTarget;
        const $form = $(form);
        const $modal = $('#editPollModal');
        const $button = $('.js-submit-edit');
        const pollId = $('#editPollId').val();
        const payload = collectEditPollPayload();

        Utils.hideModalAlert($modal);

        if (!form.checkValidity() || payload.topicIds.length === 0 || payload.options.length < 2) {
            $form.addClass('was-validated');
            Utils.showModalAlert($modal, 'warning', 'Ellenőrizd a kötelező mezőket.');
            return;
        }

        Utils.setButtonLoading($button, true);

        $.ajax({
            url: `/api/polls/${pollId}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(payload)
        })
            .done(function () {
                window.location.reload();
            })
            .fail(function (xhr) {
                Utils.showModalAlert($modal, 'danger', Utils.extractErrorMessage(xhr, 'A módosítás nem sikerült.'));
            })
            .always(function () {
                Utils.setButtonLoading($button, false);
            });
    }

    function openLockModal($card) {
        const pollId = $card.attr('data-poll-id');
        const pollTitle = $card.attr('data-poll-title');

        const $modal = $('#lockPollModal');

        Utils.hideModalAlert($modal);

        $('#lockPollId').val(pollId);
        $('#lockPollTitle').text(pollTitle);

        bootstrap.Modal.getOrCreateInstance($modal[0]).show();
    }

    function submitLockPoll(event) {
        event.preventDefault();

        const $modal = $('#lockPollModal');
        const $button = $('.js-submit-lock');
        const pollId = $('#lockPollId').val();

        Utils.hideModalAlert($modal);
        Utils.setButtonLoading($button, true);

        $.ajax({
            url: `/api/polls/${pollId}/lock`,
            method: 'POST'
        })
            .done(function () {
                window.location.reload();
            })
            .fail(function (xhr) {
                Utils.showModalAlert($modal, 'danger', Utils.extractErrorMessage(xhr, 'A zárolás nem sikerült.'));
            })
            .always(function () {
                Utils.setButtonLoading($button, false);
            });
    }

    function openDeleteModal($card) {
        const pollId = $card.attr('data-poll-id');
        const pollTitle = $card.attr('data-poll-title');
        const $modal = $('#deletePollModal');

        Utils.hideModalAlert($modal);

        $('#deletePollId').val(pollId);
        $('#deletePollTitle').text(pollTitle);

        bootstrap.Modal.getOrCreateInstance($modal[0]).show();
    }

    function submitDeletePoll(event) {
        event.preventDefault();

        const $modal = $('#deletePollModal');
        const $button = $('.js-submit-delete');
        const pollId = $('#deletePollId').val();

        Utils.hideModalAlert($modal);
        Utils.setButtonLoading($button, true);

        $.ajax({
            url: `/api/polls/${pollId}`,
            method: 'DELETE'
        })
            .done(function () {
                window.location.reload();
            })
            .fail(function (xhr) {
                Utils.showModalAlert($modal, 'danger', Utils.extractErrorMessage(xhr, 'A törlés nem sikerült.'));
            })
            .always(function () {
                Utils.setButtonLoading($button, false);
            });
    }

    function initMainPage() {
        Utils.setupCsrfForAjax();
        Utils.animateProgressBars(document);

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

        $(document).on('click', '.js-open-edit-modal', function () {
            openEditModal($(this).closest('.poll-card'));
        });

        $(document).on('click', '.js-add-edit-option', function () {
            addEditOptionRow('', false);
        });

        $(document).on('click', '.js-remove-edit-option', function () {
            $(this).closest('.edit-option-row').remove();
            updateEditOptionButtons(false);
        });

        $(document).on('submit', '#editPollForm', submitEditPoll);

        $(document).on('click', '.js-open-lock-modal', function () {
            openLockModal($(this).closest('.poll-card'));
        });

        $(document).on('submit', '#lockPollForm', submitLockPoll);
    }

    window.SzavazzAppMain = {
        normalizePercent: Utils.normalizePercent,
        calculateVotePercent: Utils.calculateVotePercent,
        buildProgressWidth: Utils.buildProgressWidth,
        applyProgressValue: Utils.applyProgressValue,
        initMainPage
    };

    $(function () {
        initMainPage();
    });
})(window, window.jQuery || window.$);