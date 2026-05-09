const $ = require('jquery');

describe('SzavazzApp frontend unit tesztek', () => {
    beforeEach(() => {
        jest.resetModules();

        document.body.innerHTML = '';

        window.$ = $;
        window.jQuery = $;

        global.$ = $;
        global.jQuery = $;
    });

    test('main: százalékszámítás helyesen kerekít és nullás összes szavazatnál 0-t ad', () => {
        require('../../main/resources/static/js/global.js');
        require('../../main/resources/static/js/main.js');

        expect(window.SzavazzAppMain.calculateVotePercent(1, 3)).toBe(33);
        expect(window.SzavazzAppMain.calculateVotePercent(2, 3)).toBe(67);
        expect(window.SzavazzAppMain.calculateVotePercent(0, 0)).toBe(0);
    });

    test('main: progress bar érték beállítása megjeleníti a százalékot és beállítja a szélességet', () => {
        document.body.innerHTML = `
            <div class="progress poll-progress" role="progressbar" aria-valuenow="0">
                <div class="progress-bar progress-bar-striped progress-bar-animated" style="width: 0%"></div>
            </div>
        `;

        require('../../main/resources/static/js/global.js');
        require('../../main/resources/static/js/main.js');

        const progressElement = document.querySelector('.poll-progress');
        const normalizedPercent = window.SzavazzAppMain.applyProgressValue(progressElement, 145);

        expect(normalizedPercent).toBe(100);
        expect(progressElement.getAttribute('aria-valuenow')).toBe('100');
        expect(progressElement.querySelector('.progress-bar').style.width).toBe('100%');
    });
});