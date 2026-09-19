const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const projects = require('./src/_data/projects.json');
const legacyRoutes = require('./src/_data/legacyRoutes')();
const output = path.join(__dirname, '.test-dist');
const read = url => fs.readFileSync(path.join(output, url, 'index.html'), 'utf8');
const source = file => fs.readFileSync(path.join(__dirname, '..', file), 'utf8');
const httpSource = 'aj-http/src/main/java/com/ajaxjs/util/httpremote/';
const cryptoSource = 'aj-util/src/main/java/com/ajaxjs/util/cryptography/';
const httpPages = ['Base', 'Get', 'Transfer', 'ProxySecurity', 'advanced-usage'];
const httpMenuPages = ['Base', 'Get', 'Transfer', 'ProxySecurity'];
const cryptoPages = ['intro', 'flow', 'Cryptography', 'Rsa'];

function checkLinks(html, page) {
    for (const match of html.matchAll(/href="(\/(?!\/)[^"?#]*)(?:[?#][^"]*)?"/g)) {
        const url = match[1];
        if (url.startsWith('/asset/')) continue;
        const target = path.join(output, url, url.endsWith('/') ? 'index.html' : '');
        assert.ok(fs.existsSync(target), `${page}: broken link ${url}`);
    }
}

function checkMenu(html, project, suffix) {
    const menu = html.match(/<menu\b[^>]*>([\s\S]*?)<\/menu>/)[1];
    assert.ok(!menu.includes('/aj-util/http_request/'), 'old HTTP menu URL');
    for (const page of httpMenuPages) {
        assert.equal(menu.includes(`href="/aj-http/http_request/${page}${suffix}/"`), project === 'aj-http');
    }
    assert.equal(menu.includes('/aj-util/common/'), project === 'aj-util');
}

function checkGuide(file, terms, forbidden) {
    const text = source(`docs-src/src/${file}.md`);
    for (const term of terms) assert.ok(text.includes(term), `${file}: missing contract ${term}`);
    assert.ok(!forbidden.test(text), `${file}: obsolete API`);
    // Static import existence check, not a Java compilation/runtime test.
    for (const match of text.matchAll(/^import (com\.ajaxjs\.[\w.]+);/gm)) {
        const relative = `src/main/java/${match[1].replaceAll('.', '/')}.java`;
        assert.ok(['aj-util', 'aj-http'].some(module => fs.existsSync(path.join(__dirname, '..', module, relative))),
            `${file}: missing source for ${match[1]}`);
    }
}

try {
    assert.equal(new Set(legacyRoutes.map(route => route.from)).size, legacyRoutes.length, 'duplicate redirects');
    const redirectSources = new Set(legacyRoutes.map(route => route.from));
    for (const project of projects) {
        for (const language of ['', 'cn/']) {
            const html = read(`${project}/${language}`);
            assert.ok(html.includes(`<title>${project} - `));
            assert.ok(html.includes(`value="/${project}/${language}" selected`));
            assert.equal((html.match(/<option /g) || []).length, projects.length);
            for (const other of projects) {
                assert.ok(html.includes(`value="/${other}/${language}"`));
            }
            checkMenu(html, project, language ? '-cn' : '');
            checkLinks(html, `${project}/${language}`);
            if (project === 'aj-http') {
                assert.ok(!html.includes('coming soon') && !html.includes('详细文档待补充'));
                for (const term of ['aj-net', '2.1', 'Request', 'HttpMethod', 'Response', 'disconnect']) {
                    assert.ok(html.includes(term), `HTTP home missing ${term}`);
                }
            }
        }
    }
    for (const route of legacyRoutes) {
        assert.ok(read(route.from).includes(`url=${route.to}`));
        assert.ok(!redirectSources.has(route.to), `redirect chain: ${route.from}`);
        const html = read(route.to);
        assert.ok(html.includes('id="project-select"'));
        const project = route.to.split('/')[1];
        checkMenu(html, project, /-cn\/$|\/cn\/$/.test(route.to) ? '-cn' : '');
        checkLinks(html, route.to);
    }

    const obsoleteHttp = /\b(?:ResponseEntity|ResponseHandler|SetConnection|BatchDownload|FileUpload)\b|Get\.(?:simpleGET|download)\(|Post\.(?:post|multiPOST)\(|Delete\.del\(/;
    const httpTerms = {
        Base: ['httpremote.model', 'initData()', '200–299', 'UncheckedIOException', 'responseAsJsonList', 'DataReader'],
        Get: ['Get.text', 'Post.form', 'Put.api', 'Head', 'getContentLength()', 'disconnect'],
        Transfer: ['MultipartPost', 'MultipartWriter', 'HttpFileDownload', 'downloadAllAsync'],
        ProxySecurity: ['CallHandler.create', '@HEAD', '@Url.config', 'create2()', 'SkipSSL', '460'],
        'advanced-usage': []
    };
    const cryptoTerms = {
        intro: ['DoCipher', 'CipherResult', 'AesGcm', 'AesCbc', 'AesPbe', 'AesLegacy', 'AesCipherResult', 'RestoreKey', 'PemUtils'],
        flow: ['doCipherFromBase64', 'GCMParameterSpec', 'SecretKeyMgr', 'IllegalStateException', 'IllegalArgumentException', 'SHA1PRNG'],
        Cryptography: ['AesGcm', 'AesCbc', 'AesPbe', 'AesLegacy', 'AesCipherResult', 'getNonce()', 'PBKDF2WithHmacSHA256', '100,000', 'AES/ECB/PKCS5Padding'],
        Rsa: ['Rsa.generateKeyPair', 'MGF1', '190', 'DoSignature', 'DoVerify', 'RestoreKey', 'PemUtils', 'PKCS#8', 'PKCS#1', 'CertificateUtils', 'deserializeToCerts']
    };
    for (const suffix of ['', '-cn']) {
        for (const page of httpPages) {
            const slug = `http_request/${page}${suffix}`;
            const to = `/aj-http/${slug}/`;
            for (const prefix of ['', '/aj-util']) {
                const from = `${prefix}/${slug}/`;
                assert.ok(legacyRoutes.some(route => route.from === from && route.to === to), `missing ${from} -> ${to}`);
                assert.ok(read(from).includes(`rel="canonical" href="https://aj-util.ajaxjs.com${to}"`));
            }
            assert.ok(!fs.existsSync(path.join(__dirname, 'src', 'aj-util', `${slug}.md`)), 'HTTP source still under aj-util');
            checkGuide(`aj-http/${slug}`, httpTerms[page], obsoleteHttp);
        }
        for (const page of cryptoPages) {
            checkGuide(`aj-util/cryptography/${page}${suffix}`, cryptoTerms[page],
                /import com\.ajaxjs\.util\.cryptography\.(?:Cryptography|rsa\.KeyMgr);|\bCryptography\.(?:PBE_|AES_|DES_|initSalt)|\.setStrData\(|\.signToString\(/);
        }
    }

    // Lock documentation to selected source contracts; changing implementation requires reviewing both languages.
    const contracts = [
        [httpSource + 'Request.java', ['package com.ajaxjs.util.httpremote;', 'responseCode >= 200 && responseCode < 300', 'resp.setResponseText(result);', 'connectTimeout = 10000', 'readTimeout = 15000']],
        [httpSource + 'MultipartPost.java', ['setChunkedStreamingMode(8192)', 'setInstanceFollowRedirects(false)', 'connection.disconnect()']],
        [httpSource + 'HttpFileDownload.java', ['newFixedThreadPool(4', 'this.urls = Objects.requireNonNull(urls', 'StandardCopyOption.REPLACE_EXISTING']],
        [cryptoSource + 'aes/AesGcm.java', ['GCM_NONCE_LENGTH = 12', 'GCM_TAG_LENGTH = 128']],
        [cryptoSource + 'aes/AesCbc.java', ['AES/CBC/PKCS5Padding', 'CBC_IV_LENGTH = 16']],
        [cryptoSource + 'aes/AesPbe.java', ['PBE_SALT_LENGTH = 16', 'MIN_PBE_ITERATIONS = 100_000', 'PBE_KEY_LENGTH = 128', 'ObjectHelper.concat(nonce, encrypted.getResult())']],
        [cryptoSource + 'aes/AesLegacy.java', ['AES/ECB/PKCS5Padding']],
        [cryptoSource + 'SecretKeyMgr.java', ['PBKDF2WithHmacSHA256']],
        [cryptoSource + 'rsa/Rsa.java', ['OAEPWithSHA-256AndMGF1Padding', 'MGF1ParameterSpec.SHA256', 'OAEPWithSHA-1AndMGF1Padding']],
        [cryptoSource + 'rsa/RestoreKey.java', ['X509EncodedKeySpec', 'PKCS8EncodedKeySpec', 'PKCS#1 RSA keys are not supported.']]
    ];
    for (const [file, terms] of contracts) {
        const text = source(file);
        for (const term of terms) assert.ok(text.includes(term), `${file}: changed source contract ${term}`);
    }
    const pom = source('aj-http/pom.xml');
    assert.ok(pom.includes('<artifactId>aj-net</artifactId>') && pom.includes('<version>2.1</version>'));
    const sitemap = fs.readFileSync(path.join(output, 'sitemap.xml'), 'utf8');
    for (const project of projects) assert.ok(sitemap.includes(`https://aj-util.ajaxjs.com/${project}/`));
    for (const route of legacyRoutes) assert.ok(!sitemap.includes(`https://aj-util.ajaxjs.com${route.from}</loc>`));
    for (const page of httpPages) {
        for (const suffix of ['', '-cn']) assert.ok(sitemap.includes(`https://aj-util.ajaxjs.com/aj-http/http_request/${page}${suffix}/`));
    }
    console.log(`Verified ${projects.length} bilingual project homes, ${legacyRoutes.length} direct redirects, project-specific menus, links, sitemap, HTTP/crypto guides and static source contracts.`);
} finally {
    fs.rmSync(output, { recursive: true, force: true });
}
