const assert = require('node:assert/strict');
const { indexPage, plainText } = require('./lib/search-index');
const { search, snippet, highlightParts } = require('./src/asset/js/search');

const fixture = '<html lang="zh-CN"><nav>noise</nav><article><p>前言</p><h1>图片 &amp; Java</h1>'
    + '<p>创建海报</p><h2>圆角</h2><p>使用 roundCorner() 设置透明圆角。</p>'
    + '<h2>圆角</h2><p>第二节</p><h3 id="custom">已有标题</h3>'
    + '<pre><code>&lt;script&gt;alert(1)&lt;/script&gt;</code></pre></article></html>';
const indexed = indexPage(fixture, '/aj-image/cn/', ['aj-image']);
assert.equal(indexed.entries.length, 5);
assert.ok(!indexed.entries.some(entry => entry.text.includes('noise')));
assert.equal(indexed.entries[0].text, '前言');
assert.equal(indexed.entries[1].title, '图片 & Java');
assert.ok(indexed.html.includes('id="search-圆角-2"'));
assert.equal(new Set(indexed.entries.map(entry => entry.url)).size, 5);
assert.ok(indexed.entries.every(entry => entry.language === 'zh'));
assert.ok(indexed.entries.some(entry => entry.url.endsWith('#custom')));
assert.deepEqual(indexPage('<html>redirect</html>', '/aj-image/old/', ['aj-image']).entries, []);
assert.deepEqual(indexPage(fixture, '/unknown/', ['aj-image']).entries, []);
assert.equal(plainText('a<script>bad</script><style>bad</style><!--bad--> &lt;x&gt;'), 'a <x>');

let found = search(indexed.entries, 'roundCorner()', 'aj-image');
assert.equal(found.length, 1);
assert.equal(found[0].entry.heading, '圆角');
assert.equal(search(indexed.entries, '圆角 不存在', '').length, 0);
assert.equal(search(indexed.entries, '   ', '').length, 0);
assert.equal(search(indexed.entries, '圆角', 'aj-ftp').length, 0);
assert.equal(search(indexed.entries, '[.*+?', '').length, 0);
const entries = [
    { project: 'aj-ftp', title: 'FTP', heading: 'Upload', text: 'Java image', url: '/aj-ftp/upload/' },
    { project: 'aj-image', title: 'Java', heading: 'image', text: 'Java', url: '/aj-image/' }
];
assert.equal(search(entries, 'IMAGE', '')[0].entry.project, 'aj-image');
assert.equal(search(entries, 'java image', '').length, 2);
assert.equal(search(entries, 'java image java image java image java image missing', '').length, 0);
assert.equal(search(entries, 'java', '', 1).length, 1);
const malicious = '<img src=x onerror=alert(1)>';
const parts = highlightParts(malicious, ['img', 'src']);
assert.equal(parts.map(part => part.text).join(''), malicious);
assert.deepEqual(highlightParts('abcde', ['abc', 'bcde']).filter(part => part.match), [{ text: 'abcde', match: true }]);
assert.ok(snippet('a'.repeat(300) + 'needle', ['needle']).includes('needle'));
console.log('Search indexing, anchors, literal Chinese/API matching, AND terms, scope, ranking and safe highlight parts verified.');
