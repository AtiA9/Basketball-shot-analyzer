// effects.js - visual polish layer: scroll-reveal entrances + pointer spotlight/tilt.
// Pure progressive enhancement: if this never runs, every element is already in its
// normal fully-visible state (nothing in the base CSS depends on JS to become visible).
(function () {
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const canHover = window.matchMedia("(pointer: fine)").matches;

  /* ---------- scroll-triggered entrance ----------
     Every target gets hidden then observed - IntersectionObserver always fires
     immediately for elements already in view at observe() time, so above-the-fold
     content plays its entrance on load too (a fuller, more deliberate first paint),
     and below-the-fold content animates in as the user scrolls to it. */
  function setupReveal() {
    const targets = document.querySelectorAll(
      ".stage-card, .p-quiz, .p-scenario, .p-task, .callout, .reference-note, .progress-shell, .prevnext a"
    );
    if (!targets.length || reduceMotion) return;

    const toObserve = [];
    targets.forEach((el, i) => {
      if (el.classList.contains("reveal-in") || el.classList.contains("reveal-hidden")) return;
      el.classList.add("reveal-hidden");
      el.style.animationDelay = (i % 8) * 65 + "ms";
      toObserve.push(el);
    });
    if (!toObserve.length) return;

    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          entry.target.classList.remove("reveal-hidden");
          entry.target.classList.add("reveal-in");
          io.unobserve(entry.target);
        });
      },
      { threshold: 0.08, rootMargin: "0px 0px -4% 0px" }
    );
    toObserve.forEach((el) => io.observe(el));
  }

  /* ---------- pointer spotlight + subtle tilt ---------- */
  function attachSpotlight(el, withTilt) {
    el.addEventListener("pointermove", (e) => {
      const r = el.getBoundingClientRect();
      const px = (e.clientX - r.left) / r.width;
      const py = (e.clientY - r.top) / r.height;
      el.style.setProperty("--mx", (px * 100).toFixed(1) + "%");
      el.style.setProperty("--my", (py * 100).toFixed(1) + "%");
      if (withTilt) {
        const rx = (0.5 - py) * 7;
        const ry = (px - 0.5) * 9;
        el.style.setProperty("--rx", rx.toFixed(2) + "deg");
        el.style.setProperty("--ry", ry.toFixed(2) + "deg");
      }
    });
    el.addEventListener("pointerleave", () => {
      if (withTilt) {
        el.style.setProperty("--rx", "0deg");
        el.style.setProperty("--ry", "0deg");
      }
    });
  }

  function setupPointerFX() {
    if (!canHover || reduceMotion) return;
    document.querySelectorAll(".stage-card").forEach((el) => attachSpotlight(el, true));
    document.querySelectorAll(".p-quiz, .p-scenario, .p-task").forEach((el) => attachSpotlight(el, false));
  }

  function init() {
    setupReveal();
    setupPointerFX();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }

  // Practice components (quiz/scenario/task) render asynchronously after this script
  // runs once, so re-scan shortly after to catch anything mounted late.
  setTimeout(init, 250);
})();
