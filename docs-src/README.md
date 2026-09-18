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

# Less.js supports

Install https://www.npmjs.com/package/eleventy-plugin-less
