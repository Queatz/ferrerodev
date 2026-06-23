// Webpack optimization for the Kotlin Multiplatform + Compose web app.
//
// Goal: speed up loading and (more importantly) improve repeat-visit caching.
// Compose Multiplatform ships a large amount of library code (Kotlin stdlib,
// the Compose runtime, Compose UI, skiko bindings, ...) that changes only when
// dependencies are bumped, whereas the application code in this repo changes
// frequently. By extracting the library code into a separate, long-term
// cacheable "vendor" chunk, returning visitors only re-download the small
// application bundle after a deploy, and the browser can fetch both files in
// parallel (HTTP/2) on the first visit.
//
// Important constraints for this project:
//   * The Kotlin/JS Gradle plugin does NOT use HtmlWebpackPlugin, so it does
//     not inject <script> tags. The app is served as static files (nginx) with
//     a hand-written index.html. Therefore every *initial* chunk must be
//     referenced explicitly from index.html, and chunk file names must be
//     deterministic (no content hash) so the references stay valid.
//   * For that reason we deliberately produce exactly ONE extra chunk
//     ("webApp-vendor.js") and avoid `maxSize`, which would split the output
//     into many hash-named files that the static index.html could not
//     reference. This chunk must be listed in index.html before webApp.js.
//
// Files in this directory are merged into the generated webpack config; the
// `config` object is the webpack configuration.

config.optimization = config.optimization || {};
config.optimization.splitChunks = {
    cacheGroups: {
        // Everything that is library code (lives under the generated `kotlin/`
        // folder or in node_modules) but is NOT this project's own modules
        // goes into a single cacheable vendor chunk.
        vendor: {
            chunks: "all",
            // Produces a single, deterministically named chunk "webApp-vendor.js".
            name: "vendor",
            // Keep it as a single file; do not let `maxSize` fragment it.
            enforce: true,
            reuseExistingChunk: true,
            test(module) {
                const resource =
                    (module.nameForCondition && module.nameForCondition()) || "";
                if (!resource) return false;
                // Compiled Kotlin modules live in a `kotlin/` folder; the app's own
                // modules are the `ferrerodev-app-*` files there. Everything else
                // in `kotlin/` (stdlib, Compose, etc.) plus npm `node_modules`
                // dependencies is library code.
                const isAppCode = /[\\/]kotlin[\\/]ferrerodev-app-/.test(resource);
                const isLibrary =
                    (/[\\/]kotlin[\\/]/.test(resource) && !isAppCode) ||
                    /[\\/]node_modules[\\/]/.test(resource);
                return isLibrary;
            },
        },
    },
};
