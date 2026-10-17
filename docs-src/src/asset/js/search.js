(function (root) {
    'use strict';

    /** Literal, case-insensitive matching: every keyword must occur in a section. */
    function search(entries, query, project, limit) {
        query = query.trim().slice(0, 200).toLowerCase();
        if (!query)
            return [];

        var terms = Array.from(new Set(query.split(/[\s，,]+/).filter(Boolean)));
        return entries.filter(function (entry) { return !project || entry.project === project; })
            .map(function (entry) {
                var heading = entry.heading.toLowerCase();
                var title = entry.title.toLowerCase();
                var text = entry.text.toLowerCase();
                var all = title + '\n' + heading + '\n' + text;
                if (!terms.every(function (term) { return all.indexOf(term) !== -1; }))
                    return null;

                var score = (heading === query ? 100 : 0) + (heading.indexOf(query) !== -1 ? 30 : 0);
                terms.forEach(function (term) {
                    score += (heading.indexOf(term) !== -1 ? 12 : 0) + (title.indexOf(term) !== -1 ? 5 : 0)
                        + (text.indexOf(term) !== -1 ? 1 : 0);
                });
                return { entry: entry, score: score, terms: terms };
            }).filter(Boolean).sort(function (a, b) { return b.score - a.score || a.entry.url.localeCompare(b.entry.url); })
            .slice(0, limit || 20);
    }

    function snippet(text, terms) {
        var lower = text.toLowerCase();
        var positions = terms.map(function (term) { return lower.indexOf(term); }).filter(function (position) { return position >= 0; });
        var start = Math.max(0, (positions.length ? Math.min.apply(null, positions) : 0) - 35);
        return (start ? '…' : '') + text.slice(start, start + 150) + (start + 150 < text.length ? '…' : '');
    }

    /** Return text parts instead of inserting content or user input through innerHTML. */
    function highlightParts(text, terms) {
        var lower = text.toLowerCase();
        var ranges = [];
        terms.forEach(function (term) {
            if (!term)
                return;

            var from = 0;
            var at;
            while ((at = lower.indexOf(term, from)) !== -1) {
                ranges.push([at, at + term.length]);
                from = at + term.length;
            }
        });
        ranges.sort(function (a, b) { return a[0] - b[0]; });
        var merged = [];
        ranges.forEach(function (range) {
            var previous = merged[merged.length - 1];
            if (previous && range[0] <= previous[1])
                previous[1] = Math.max(previous[1], range[1]);
            else
                merged.push(range.slice());
        });
        var parts = [];
        var cursor = 0;
        merged.forEach(function (range) {
            parts.push({ text: text.slice(cursor, range[0]), match: false });
            parts.push({ text: text.slice(range[0], range[1]), match: true });
            cursor = range[1];
        });
        parts.push({ text: text.slice(cursor), match: false });
        return parts;
    }

    function mount(container) {
        var document = container.ownerDocument;
        var input = container.querySelector('input');
        var scope = container.querySelector('select');
        var status = container.querySelector('[role="status"]');
        var results = container.querySelector('ol');
        var chinese = container.dataset.language === 'zh';
        var loading;
        var timer;
        var revision = 0;
        var composing = false;

        function load() {
            if (!loading) {
                // HTTP revalidation keeps the static index aligned with a new deployment.
                loading = root.fetch('/search/' + (chinese ? 'zh' : 'en') + '.json', { cache: 'no-cache' })
                    .then(function (response) {
                        if (!response.ok)
                            throw new Error('Search index unavailable');

                        return response.json();
                    }).then(function (data) {
                        if (data.version !== 1 || !Array.isArray(data.entries))
                            throw new Error('Invalid search index');

                        return data.entries;
                    }).catch(function (error) {
                        loading = null;
                        throw error;
                    });
            }
            return loading;
        }

        function appendText(node, text, terms) {
            highlightParts(text, terms).forEach(function (part) {
                var child = part.match ? document.createElement('mark') : document.createTextNode(part.text);
                if (part.match)
                    child.textContent = part.text;

                node.appendChild(child);
            });
        }

        function run(focus) {
            var current = ++revision;
            var query = input.value.trim();
            var navigation = container.closest('.docs-navigation');
            if (navigation)
                navigation.classList.toggle('is-searching', Boolean(query));

            results.replaceChildren();
            results.hidden = true;
            if (!query) {
                status.textContent = '';
                return;
            }
            status.textContent = chinese ? '正在搜索…' : 'Searching…';
            load().then(function (entries) {
                if (current !== revision)
                    return;

                var found = search(entries, query, scope.value);
                found.forEach(function (result) {
                    // Index URLs are generated locally; reject unexpected link schemes defensively.
                    if (!/^\/(?!\/)/.test(result.entry.url))
                        return;

                    var item = document.createElement('li');
                    var link = document.createElement('a');
                    link.href = result.entry.url;
                    appendText(link, result.entry.heading, result.terms);
                    var context = document.createElement('small');
                    context.textContent = result.entry.project + ' · ' + result.entry.title;
                    var paragraph = document.createElement('p');
                    appendText(paragraph, snippet(result.entry.text, result.terms), result.terms);
                    item.append(link, context, paragraph);
                    results.appendChild(item);
                });
                results.hidden = !results.children.length;
                status.textContent = found.length ? (chinese ? '找到 ' + found.length + ' 条结果（最多显示 20 条）' : found.length + ' results (up to 20 shown)')
                    : (chinese ? '没有找到结果，试试其他关键词或全部项目。' : 'No results. Try other keywords or all projects.');
                if (focus && results.querySelector('a'))
                    results.querySelector('a').focus();
            }).catch(function () {
                if (current === revision)
                    status.textContent = chinese ? '搜索加载失败，请检查网络后重试。' : 'Search could not load. Check your connection and try again.';
            });
        }

        function schedule() {
            clearTimeout(timer);
            // Invalidate pending responses immediately, not only after the debounce expires.
            revision++;
            if (!composing)
                timer = root.setTimeout(function () { run(false); }, 150);
        }
        input.addEventListener('input', schedule);
        input.addEventListener('compositionstart', function () { composing = true; clearTimeout(timer); revision++; });
        input.addEventListener('compositionend', function () { composing = false; schedule(); });
        scope.addEventListener('change', function () { clearTimeout(timer); run(false); });
        container.querySelector('form').addEventListener('submit', function (event) {
            event.preventDefault();
            if (!composing) {
                clearTimeout(timer);
                run(true);
            }
        });
        container.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                clearTimeout(timer);
                input.value = '';
                run(false);
                input.focus();
            }
        });
    }

    if (typeof module !== 'undefined' && module.exports)
        module.exports = { search: search, snippet: snippet, highlightParts: highlightParts };

    if (root.document)
        root.document.querySelectorAll('[data-doc-search]').forEach(mount);
})(typeof window !== 'undefined' ? window : globalThis);
