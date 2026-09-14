/* Shared helpers for AJ-Utils documentation pages. */
(function (window, document) {
  "use strict";

  var root = document.documentElement;
  var prefix = "aj-utils:";

  function read(key) {
    try { return window.localStorage.getItem(prefix + key); }
    catch (_) { return null; }
  }

  function write(key, value) {
    try { window.localStorage.setItem(prefix + key, value); }
    catch (_) { /* Some file:// previews disable storage. */ }
  }

  /** Match the browser preference to a supported language code. */
  function getBrowserLanguage(supported, fallback) {
    supported = supported || ["zh", "en"];
    fallback = fallback || "en";
    var languages = window.navigator.languages || [window.navigator.language || fallback];

    for (var i = 0; i < languages.length; i += 1) {
      var language = String(languages[i]).toLowerCase();
      for (var j = 0; j < supported.length; j += 1) {
        var candidate = String(supported[j]).toLowerCase();
        if (language === candidate || language.indexOf(candidate + "-") === 0) return supported[j];
      }
    }
    return fallback;
  }

  function prefersDarkMode() {
    return Boolean(window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches);
  }

  /** Apply light/dark theme and optionally persist the selection. */
  function applyTheme(theme, remember) {
    theme = theme === "light" || theme === "dark" ? theme : (prefersDarkMode() ? "dark" : "light");
    root.dataset.theme = theme;
    root.style.colorScheme = theme;
    if (remember) write("theme", theme);
    return theme;
  }

  function initTheme() {
    return applyTheme(read("theme"), false);
  }

  /** Set document language and expose it through html[data-lang]. */
  function applyLanguage(language, remember) {
    root.lang = language === "zh" ? "zh-CN" : language;
    root.dataset.lang = language;
    if (remember) write("language", language);
    return language;
  }

  function initLanguage(options) {
    options = options || {};
    return applyLanguage(read("language") || getBrowserLanguage(options.supported, options.fallback || "en"), false);
  }

  /** Bind buttons that switch between light and dark themes. */
  function bindThemeToggle(options) {
    options = options || {};
    var buttons = document.querySelectorAll(options.selector || "[data-theme-toggle]");

    function refreshLabel() {
      var current = root.dataset.theme || initTheme();
      var next = current === "dark" ? "light" : "dark";
      buttons.forEach(function (button) {
        button.textContent = current === "dark" ? "☀" : "☾";
        button.setAttribute("aria-label", "Switch theme to " + next);
        button.setAttribute("title", "Switch theme to " + next);
      });
    }

    buttons.forEach(function (button) {
      button.addEventListener("click", function () {
        var current = root.dataset.theme || initTheme();
        applyTheme(current === "dark" ? "light" : "dark", true);
        refreshLabel();
      });
    });
    refreshLabel();
  }

  /** Bind buttons that switch between the configured site languages. */
  function bindLanguageToggle(options) {
    options = options || {};
    var languages = options.languages || ["zh", "en"];
    var buttons = document.querySelectorAll(options.selector || "[data-language-toggle]");

    function refreshLabel() {
      var next = root.dataset.lang === "zh" ? "EN" : "中";
      buttons.forEach(function (button) {
        button.textContent = next;
        button.setAttribute("aria-label", "Switch language to " + next);
      });
    }

    buttons.forEach(function (button) {
      button.addEventListener("click", function () {
        var current = root.dataset.lang || languages[0];
        var next = languages[(languages.indexOf(current) + 1) % languages.length];
        applyLanguage(next, true);
        refreshLabel();
      });
    });
    refreshLabel();
  }

  function onReady(callback) {
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", callback, { once: true });
    else callback();
  }

  /** Copy text with a fallback that also works in many file:// previews. */
  function copyText(text) {
    if (window.navigator.clipboard && window.isSecureContext) return window.navigator.clipboard.writeText(text);
    return new Promise(function (resolve, reject) {
      var textarea = document.createElement("textarea");
      textarea.value = text;
      textarea.style.cssText = "position:fixed;opacity:0;pointer-events:none";
      document.body.appendChild(textarea);
      textarea.select();
      var copied = false;
      try { copied = document.execCommand("copy"); } catch (_) {}
      textarea.remove();
      if (copied) resolve(); else reject(new Error("Unable to copy text"));
    });
  }

  window.AJUtils = {
    getBrowserLanguage: getBrowserLanguage,
    prefersDarkMode: prefersDarkMode,
    applyTheme: applyTheme,
    initTheme: initTheme,
    bindThemeToggle: bindThemeToggle,
    applyLanguage: applyLanguage,
    initLanguage: initLanguage,
    bindLanguageToggle: bindLanguageToggle,
    onReady: onReady,
    copyText: copyText
  };
})(window, document);
