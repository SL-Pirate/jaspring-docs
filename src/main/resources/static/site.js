// Application state lives in Jaspring. This module handles browser-only details.
const highlights = new WeakMap();

function highlight(el) {
  const source = el.textContent;
  const language = el.dataset.language;
  if (!language || language === "text") return;
  const cached = highlights.get(el);
  if (
    cached?.source === source &&
    cached.language === language &&
    cached.html === el.innerHTML
  )
    return;

  const pattern =
    language === "html"
      ? /(?<comment><!--[\s\S]*?-->)|(?<tag><[!\w/][^>]*>)|(?<keyword>@(?:if|else|for|empty|switch|case|let|defer)\b)/g
      : /(?<comment>\/\/[^\n]*|\/\*[\s\S]*?\*\/|^\s*#[^\n]*)|(?<string>"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*')|(?<annotation>@\w+)|(?<keyword>\b(?:public|private|protected|class|record|interface|final|var|new|return|void|import|package|static|implements|extends|if|else|true|false|null|const|let|function|implementation|repositories|dependencies|mavenCentral|plugins|id|version)\b)|(?<number>\b\d+(?:\.\d+)*\b)/gm;
  const fragment = document.createDocumentFragment();
  let offset = 0;
  for (const match of source.matchAll(pattern)) {
    fragment.append(document.createTextNode(source.slice(offset, match.index)));
    const token = document.createElement("span");
    token.className = `token-${Object.keys(match.groups).find((key) => match.groups[key] !== undefined)}`;
    token.textContent = match[0];
    fragment.append(token);
    offset = match.index + match[0].length;
  }
  fragment.append(document.createTextNode(source.slice(offset)));
  el.replaceChildren(fragment);
  highlights.set(el, { source, language, html: el.innerHTML });
}

let dialogState;

function mountDialog(el) {
  const previousFocus = document.activeElement;
  const background = [
    ...document.querySelectorAll(
      ".site-header, site-landing, site-docs, .site-footer, .skip-link",
    ),
  ];
  const previousInert = background.map((node) => node.inert);
  const previousOverflow = document.body.style.overflow;
  const trapFocus = (event) => {
    if (event.key !== "Tab") return;
    const controls = [...el.querySelectorAll("a[href], button, input")].filter(
      (node) => !node.disabled,
    );
    const first = controls[0];
    const last = controls.at(-1);
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last?.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first?.focus();
    }
  };
  el.addEventListener("keydown", trapFocus);
  dialogState = {
    el,
    previousFocus,
    background,
    previousInert,
    previousOverflow,
    trapFocus,
  };
  document.body.style.overflow = "hidden";
  requestAnimationFrame(() => el.querySelector("input")?.focus());
}

function syncBrowserDetails() {
  document.querySelectorAll("code[data-language]").forEach(highlight);
  const shortcut = document.querySelector(".search-trigger kbd");
  const shortcutText = /Mac|iPhone|iPad/.test(navigator.platform)
    ? "⌘ K"
    : "Ctrl K";
  if (shortcut && shortcut.textContent !== shortcutText)
    shortcut.textContent = shortcutText;
  const dialog = document.querySelector(".search-dialog");
  if (dialogState && dialogState.el !== dialog) {
    const state = dialogState;
    state.background.forEach((node, index) => {
      node.inert = state.previousInert[index];
    });
    document.body.style.overflow = state.previousOverflow;
    state.el.removeEventListener("keydown", state.trapFocus);
    if (state.previousFocus?.isConnected) state.previousFocus.focus();
    dialogState = undefined;
  }
  if (dialog && !dialogState) mountDialog(dialog);
  dialogState?.background.forEach((node) => {
    node.inert = true;
  });
}

// Observe server-rendered DOM patches; no UI data is duplicated in the browser.
const browserDetails = new MutationObserver(syncBrowserDetails);
browserDetails.observe(document.body, {
  childList: true,
  subtree: true,
  attributes: true,
  attributeFilter: ["data-language"],
});
syncBrowserDetails();

document.addEventListener("keydown", (event) => {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
    event.preventDefault();
    if (document.querySelector(".search-dialog"))
      document.querySelector("#docs-query")?.focus();
    else
      document
        .querySelector(".site-header")
        ?.dispatchEvent(new CustomEvent("open-search", { bubbles: true }));
  }
});

document.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-copy]");
  if (!button || button.dataset.copying) return;
  const source = button
    .closest(".doc-code-block, .install-card")
    ?.querySelector("code")?.textContent;
  if (!source) return;
  const originalLabel = button.textContent;
  button.dataset.copying = "true";
  try {
    await navigator.clipboard.writeText(source);
    button.textContent = "Copied ✓";
  } catch {
    button.textContent = "Select to copy";
    const range = document.createRange();
    range.selectNodeContents(
      button.closest(".doc-code-block, .install-card").querySelector("code"),
    );
    const selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
  }
  setTimeout(() => {
    button.textContent = originalLabel;
    delete button.dataset.copying;
  }, 2000);
});

document.addEventListener("keydown", (event) => {
  const tab = event.target.closest('[role="tab"]');
  if (!tab || !["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key))
    return;
  const tabs = [
    ...tab.closest('[role="tablist"]').querySelectorAll('[role="tab"]'),
  ];
  const direction = event.key === "ArrowLeft" ? -1 : 1;
  const index =
    event.key === "Home"
      ? 0
      : event.key === "End"
        ? tabs.length - 1
        : (tabs.indexOf(tab) + direction + tabs.length) % tabs.length;
  event.preventDefault();
  tabs[index].focus();
  tabs[index].click();
});

const tocLinks = [...document.querySelectorAll(".doc-toc nav a")];
if (tocLinks.length && "IntersectionObserver" in window) {
  const observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (!entry.isIntersecting) continue;
        tocLinks.forEach((link) =>
          link.classList.toggle(
            "toc-active",
            link.hash === `#${entry.target.id}`,
          ),
        );
      }
    },
    { rootMargin: "-100px 0px -55% 0px" },
  );
  document
    .querySelectorAll(".doc-section")
    .forEach((section) => observer.observe(section));
}
