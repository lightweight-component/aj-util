/* Documentation-only extensions. Shared site behavior is loaded from aj-util.ajaxjs.com. */
(function (window, document) {
  "use strict";

  function onReady(callback) {
    if (window.AJUtils && typeof window.AJUtils.onReady === "function") window.AJUtils.onReady(callback);
    else if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", callback, { once: true });
    else callback();
  }

  /** Highlight Markdown code blocks when Highlight.js has been loaded by a documentation layout. */
  function initSyntaxHighlighting() {
    if (!window.hljs || typeof window.hljs.highlightElement !== "function") return false;

    document.querySelectorAll("pre code:not(.nohighlight):not(.no-highlight)").forEach(function (block) {
      if (!block.dataset.highlighted) window.hljs.highlightElement(block);
    });
    return true;
  }

  window.AJUtils = window.AJUtils || {};
  window.AJUtils.initSyntaxHighlighting = initSyntaxHighlighting;
  onReady(initSyntaxHighlighting);
})(window, document);
