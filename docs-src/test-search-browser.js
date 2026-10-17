// Optional browser smoke test: build first, then provide an installed Playwright via
// AJ_DOCS_PLAYWRIGHT when it is not available through normal Node resolution.
const assert = require('node:assert/strict');
const fs = require('node:fs/promises');
const http = require('node:http');
const path = require('node:path');
const os = require('node:os');
const { chromium } = require(process.env.AJ_DOCS_PLAYWRIGHT || 'playwright');

async function main() {
    const output = path.resolve(__dirname, 'dist');
    const types = { '.html': 'text/html; charset=utf-8', '.json': 'application/json', '.js': 'text/javascript', '.css': 'text/css' };
    const server = http.createServer(async (request, response) => {
        try {
            const url = new URL(request.url, 'http://localhost');
            const file = path.resolve(output, '.' + decodeURIComponent(url.pathname), url.pathname.endsWith('/') ? 'index.html' : '');
            if (!file.startsWith(output + path.sep)) {
                response.writeHead(403).end();
                return;
            }
            response.setHeader('Content-Type', types[path.extname(file)] || 'application/octet-stream');
            response.end(await fs.readFile(file));
        } catch (_) {
            response.writeHead(404).end();
        }
    });
    await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
    let browser;
    try {
        browser = await chromium.launch(process.env.AJ_DOCS_BROWSER ? { executablePath: process.env.AJ_DOCS_BROWSER } : {});
        const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
        const base = `http://127.0.0.1:${server.address().port}`;
        await page.route('**/*', route => new URL(route.request().url()).origin === base ? route.continue() : route.abort());
        const errors = [];
        page.on('pageerror', error => errors.push(error.message));
        let indexRequests = 0;
        page.on('request', request => { if (request.url().includes('/search/')) indexRequests++; });
        await page.goto(base + '/aj-image/cn/');
        const input = page.locator('#doc-search-input');
        const links = page.locator('#doc-search-results a');
        await input.fill('圆角');
        await links.first().waitFor();
        assert.ok(await page.locator('#doc-search-results mark').count());
        assert.ok((await links.first().getAttribute('href')).startsWith('/aj-image/'));
        await input.fill('setImage()');
        await page.waitForFunction(() => document.querySelector('#doc-search-results mark')?.textContent.toLowerCase() === 'setimage()');
        assert.equal(indexRequests, 1, 'queries should reuse the loaded index');
        await input.press('Enter');
        await page.waitForFunction(() => document.activeElement.matches('#doc-search-results a'));
        await page.keyboard.press('Enter');
        await page.waitForURL(url => !!url.hash);
        assert.ok(await page.evaluate(() => !!document.getElementById(decodeURIComponent(location.hash.slice(1)))));

        await page.goto(base + '/aj-image/cn/');
        await page.locator('#doc-search-scope').selectOption('');
        await input.fill('SimpleFtpClient');
        await links.first().waitFor();
        assert.ok((await links.first().getAttribute('href')).startsWith('/aj-ftp/'));
        await input.press('Escape');
        assert.equal(await input.inputValue(), '');
        assert.ok(await page.locator('#doc-search-results').isHidden());

        await page.setViewportSize({ width: 390, height: 844 });
        await input.fill('圆角');
        await links.first().waitFor();
        assert.ok(await input.isVisible());
        assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
        assert.ok(await page.evaluate(() => {
            const menu = document.querySelector('.docs-navigation');
            return menu.clientHeight >= menu.scrollHeight;
        }), 'active mobile search must not be clipped by the compact navigation height');
        await page.evaluate(() => window.AJUtils.applyTheme('dark', false));
        const screenshots = await fs.mkdtemp(path.join(os.tmpdir(), 'aj-doc-search-'));
        await page.screenshot({ path: path.join(screenshots, 'mobile-dark.png'), fullPage: true });

        await page.goto(base + '/aj-image/');
        await input.fill('roundCorner');
        await links.first().waitFor();
        assert.ok((await page.locator('[role="status"]').textContent()).includes('results'));

        // A failed first load must not poison retries for the lifetime of the page.
        await page.goto(base + '/aj-image/cn/');
        await page.route('**/search/zh.json', route => route.abort());
        await input.fill('圆角');
        await page.waitForFunction(() => document.querySelector('[role="status"]').textContent.includes('失败'));
        await page.unroute('**/search/zh.json');
        await input.press('Enter');
        await links.first().waitFor();

        // Treat index text as data, even when it resembles HTML or script.
        await page.goto(base + '/aj-image/cn/');
        await page.route('**/search/zh.json', route => route.fulfill({ json: { version: 1, entries: [{
            project: 'aj-image', language: 'zh', title: 'Safety', heading: 'needle <script>alert(1)</script>',
            text: 'needle <img src=x onerror=alert(1)>', url: '/aj-image/cn/'
        }] } }));
        await input.fill('needle');
        await links.first().waitFor();
        assert.equal(await page.locator('#doc-search-results img, #doc-search-results script').count(), 0);
        assert.ok((await page.locator('#doc-search-results').textContent()).includes('<img'));
        assert.deepEqual(errors, []);
        console.log('Browser checks passed: Chinese/English search, highlights, scope, keyboard/anchor navigation, mobile/dark layout and failed-load retry.');
        console.log('Screenshot: ' + path.join(screenshots, 'mobile-dark.png'));
    } finally {
        if (browser)
            await browser.close();

        await new Promise(resolve => server.close(resolve));
    }
}

main().catch(error => { console.error(error); process.exitCode = 1; });
