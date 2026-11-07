import type {
  HighlighterCore,
  LanguageRegistration,
  ThemeRegistration,
} from "shiki/core";

interface Highlight {
  source: string;
  language: string;
  html: string;
}

// Tokens left at this color keep inheriting the block's own text color.
const plain = "inherit";

// Colors live in site.css; the theme only maps grammar scopes onto them.
const theme: ThemeRegistration = {
  name: "jaspring",
  type: "dark",
  fg: plain,
  bg: "transparent",
  settings: [
    { scope: ["comment"], settings: { foreground: "var(--code-comment)" } },
    { scope: ["string"], settings: { foreground: "var(--code-string)" } },
    { scope: ["string.unquoted"], settings: { foreground: plain } },
    {
      scope: [
        "keyword",
        "storage.modifier",
        "storage.type.primitive",
        "storage.type.ts",
        "storage.type.js",
        "storage.type.interface",
        "storage.type.type",
        "storage.type.class",
        "storage.type.function",
        "constant.language",
        "variable.language",
      ],
      settings: { foreground: "var(--code-keyword)" },
    },
    {
      scope: ["keyword.operator", "storage.modifier.package", "storage.modifier.import"],
      settings: { foreground: plain },
    },
    { scope: ["keyword.operator.new"], settings: { foreground: "var(--code-keyword)" } },
    { scope: ["constant.numeric"], settings: { foreground: "var(--code-number)" } },
    {
      scope: [
        "entity.name.tag",
        "punctuation.definition.tag",
        "storage.type.annotation",
        "punctuation.definition.annotation",
        "support.type.property-name.json",
        "entity.name.command",
      ],
      settings: { foreground: "var(--code-tag)" },
    },
    {
      scope: [
        "entity.other.attribute-name",
        "support.type.property-name.css",
        "constant.other.option",
      ],
      settings: { foreground: "var(--code-attribute)" },
    },
  ],
};

// The Groovy grammar knows no Gradle DSL, so its block and configuration names are injected as keywords.
const gradle: LanguageRegistration = {
  name: "gradle",
  scopeName: "source.gradle",
  embeddedLangs: ["groovy"],
  patterns: [{ include: "source.groovy" }],
  repository: {},
  injections: {
    "L:source.gradle -string -comment": {
      patterns: [
        {
          match:
            "\\b(?:plugins|pluginManagement|repositories|mavenCentral|gradlePluginPortal|dependencies|implementation|api|developmentOnly|testImplementation|testRuntimeOnly|devImplementation|devApi|id|version|jnpm)\\b",
          name: "keyword.other.gradle",
        },
      ],
    },
  },
};

type Grammar = () => Promise<LanguageRegistration[]>;

// Keyed by the docs' language names. Each grammar is its own chunk, so a page downloads only what it shows.
const languages: Record<string, { id: string; load: Grammar }> = {
  java: { id: "java", load: () => import("shiki/langs/java.mjs").then((m) => m.default) },
  html: {
    id: "angular-html",
    load: () => import("shiki/langs/angular-html.mjs").then((m) => m.default),
  },
  gradle: {
    id: "gradle",
    load: () => import("shiki/langs/groovy.mjs").then((m) => [...m.default, gradle]),
  },
  typescript: {
    id: "typescript",
    load: () => import("shiki/langs/typescript.mjs").then((m) => m.default),
  },
  javascript: {
    id: "javascript",
    load: () => import("shiki/langs/javascript.mjs").then((m) => m.default),
  },
  json: { id: "json", load: () => import("shiki/langs/json.mjs").then((m) => m.default) },
  yaml: { id: "yaml", load: () => import("shiki/langs/yaml.mjs").then((m) => m.default) },
  css: { id: "css", load: () => import("shiki/langs/css.mjs").then((m) => m.default) },
  shell: {
    id: "shellscript",
    load: () => import("shiki/langs/shellscript.mjs").then((m) => m.default),
  },
};

const highlights = new WeakMap<HTMLElement, Highlight>();
const loaded = new Set<string>();
const loading = new Map<string, Promise<void>>();
let highlighter: HighlighterCore | undefined;
let starting: Promise<HighlighterCore> | undefined;

function start(): Promise<HighlighterCore> {
  starting ??= Promise.all([import("shiki/core"), import("shiki/engine/javascript")]).then(
    async ([core, engine]) => {
      highlighter = await core.createHighlighterCore({
        themes: [theme],
        langs: [],
        engine: engine.createJavaScriptRegexEngine({ forgiving: true }),
      });
      return highlighter;
    },
  );
  return starting;
}

function load(language: { id: string; load: Grammar }): Promise<void> {
  let pending = loading.get(language.id);
  if (!pending) {
    pending = Promise.all([start(), language.load()]).then(async ([shiki, grammar]) => {
      await shiki.loadLanguage(...grammar);
      loaded.add(language.id);
    });
    loading.set(language.id, pending);
  }
  return pending;
}

function render(el: HTMLElement, source: string, id: string, shiki: HighlighterCore): void {
  const { tokens } = shiki.codeToTokens(source, { lang: id, theme });
  const fragment = document.createDocumentFragment();
  tokens.forEach((line, index) => {
    if (index > 0) fragment.append("\n");
    for (const token of line) {
      if (!token.color || token.color === plain) {
        fragment.append(token.content);
        continue;
      }
      const span = document.createElement("span");
      span.style.color = token.color;
      span.textContent = token.content;
      fragment.append(span);
    }
  });
  el.replaceChildren(fragment);
  highlights.set(el, { source, language: id, html: el.innerHTML });
}

// Safe after every patch: an unchanged block is left alone, and one the morph reset to plain text is highlighted again.
export function highlight(el: HTMLElement): void {
  const language = languages[el.dataset.language ?? ""];
  if (!language) return;
  const source = el.textContent ?? "";
  const cached = highlights.get(el);
  if (cached?.source === source && cached.language === language.id && cached.html === el.innerHTML)
    return;

  // Synchronous once loaded, so a patched block is restored before the browser paints it.
  if (highlighter && loaded.has(language.id)) {
    render(el, source, language.id, highlighter);
    return;
  }
  load(language)
    .then(() => {
      if (highlighter && el.isConnected && el.textContent === source)
        render(el, source, language.id, highlighter);
    })
    .catch((error: unknown) => console.warn("Code highlighting is unavailable", error));
}
