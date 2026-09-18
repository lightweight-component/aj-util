const fs = require('node:fs');
const path = require('node:path');

// Preserve root AJ Util bookmarks and both historical HTTP URL prefixes.
module.exports = function () {
    const root = path.join(__dirname, '..', 'aj-util');
    const routes = [];
    function visit(directory) {
        for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
            const file = path.join(directory, entry.name);
            if (entry.isDirectory()) {
                visit(file);
            } else if (entry.name.endsWith('.md')) {
                const slug = path.relative(root, file).split(path.sep).join('/').slice(0, -3);
                routes.push({
                    from: `/${slug === 'index' ? 'docs-index' : slug}/`,
                    to: `/aj-util/${slug === 'index' ? '' : `${slug}/`}`
                });
            }
        }
    }
    visit(root);

    const httpRoot = path.join(__dirname, '..', 'aj-http', 'http_request');
    for (const entry of fs.readdirSync(httpRoot, { withFileTypes: true })) {
        if (!entry.isFile() || !entry.name.endsWith('.md')) continue;
        const slug = `http_request/${entry.name.slice(0, -3)}`;
        for (const prefix of ['', '/aj-util']) {
            routes.push({ from: `${prefix}/${slug}/`, to: `/aj-http/${slug}/` });
        }
    }
    return routes;
};
