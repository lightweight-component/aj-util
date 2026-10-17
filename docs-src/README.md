Powered by [Eleventy](https://www.11ty.dev/).

This is a Node.js project. Run `npm install` to install dependencies, then run `npm run dev` to start the development
server.

Easy to write HTML content in this environment, just pure HTML/CSS/JS is enough, plus Markdown to HTML is supported.

## Documentation structure

- `src/index.html`: shared landing page.
- `src/aj-util/`: existing AJ Util documentation; English home at `/aj-util/`, Chinese home at `/aj-util/cn/`.
- `src/aj-http/`: source-backed AJ HTTP homes and `http_request/` guides, extracted from AJ Util.
- `src/<project>/`: each other project's English `index.md` and Chinese `cn.md` (other projects currently have placeholders).
- `src/_data/projects.json`: project list used by the shared selector.
- `src/<project>/<project>.json`: directory data identifying the current project.
- `src/_includes/layouts/aj-util.njk` and `aj-util-cn.njk`: shared English/Chinese layouts. The detailed AJ Util menu is only shown for `aj-util`; HTTP navigation is only shown for `aj-http`.
- `src/asset/`: shared assets, unchanged by the migration.
- `prompt.md`: authoring notes, outside the published source tree.

To add a project, add its ID to `projects.json`, create its directory and directory data (`{"project": "project-id"}`), and add both home pages using the corresponding layouts.
The project selector preserves the current language and opens the selected project's home page.
Existing AJ Util URLs (including `/docs-index/` and `/cn/`) redirect to their new locations via `legacy-redirects.njk` and `_data/legacyRoutes.js`.
Both `/http_request/*` and `/aj-util/http_request/*` redirect directly to `/aj-http/http_request/*` (including `-cn` pages); the HTTP redirect list is derived from the new HTTP tree.
The sitemap lists the new pages, not the legacy redirects.

Run `npm run build` to generate `dist/`, or `npm test` to build into an isolated temporary directory and verify project homes, selectors, migrated links, redirects and sitemap.
The tracked repository-level `docs/` deployment output is not updated automatically; publish a clean build separately.

## Static documentation search

Every documentation page has a search box, with the current project selected by default and an option to search all projects in the same language. Chinese substrings and literal API names work without a backend. Multiple whitespace/comma-separated keywords must all match; heading/title matches rank above body matches. Results show up to 20 sections with highlighted excerpts and links to heading anchors. Press Enter to focus the first result or Escape to clear.

During each build, `lib/search-index.js` reads rendered article HTML and adds IDs only to headings that do not already have one. `eleventy.config.js` writes `/search/en.json` and `/search/zh.json` into the actual output directory. Redirects, the landing page, navigation, and footer text are not indexed. Heading-only sections remain searchable.

`src/asset/js/search.js` loads the relevant language index on the first non-empty query, reuses it in memory, and requests HTTP revalidation on a new page load. Queries stay in the browser and are not sent to a search service. No localStorage index or remote search dependency is used. Deploy both the generated pages and the `search/` directory together; use an HTTP server rather than opening HTML through `file://`. Existing hosting/CDN cache rules still apply.

`npm test` includes search matching/indexing tests and checks all generated result URLs/anchors. For optional real-browser checks, run `npm run build` followed by `node test-search-browser.js`. Supply an installed Playwright module through `AJ_DOCS_PLAYWRIGHT` if necessary, and an optional browser executable through `AJ_DOCS_BROWSER`. Playwright is deliberately not required for the normal documentation build. The browser test uses only a temporary local HTTP server, blocks external requests, and prints its screenshot location.

## Less.js support

Install https://www.npmjs.com/package/eleventy-plugin-less
