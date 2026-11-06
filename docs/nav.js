// nav.js - the stages of the bagrut guide, top navigation and previous/next links
const PAGES = [
  {
    "id": "s1",
    "href": "01-overview.html",
    "label": "1",
    "title": "האפליקציה והארכיטקטורה",
    "weeks": "מה האפליקציה עושה, השכבות, וכל המסלול של זריקה אחת"
  },
  {
    "id": "s2",
    "href": "02-oop-activities.html",
    "label": "2",
    "title": "OOP ו-Activities",
    "weeks": "הכמסה, ירושה, ממשקים, Intent ומחזור חיים"
  },
  {
    "id": "s3",
    "href": "03-auth-prefs.html",
    "label": "3",
    "title": "הרשמה, הצפנה ו-SharedPreferences",
    "weeks": "בדיקת קלט, hash ו-salt, ו\"הישאר מחובר\""
  },
  {
    "id": "s4",
    "href": "04-sqlite.html",
    "label": "4",
    "title": "SQLite",
    "weeks": "שתי טבלאות, CRUD, GROUP BY ו-JOIN"
  },
  {
    "id": "s5",
    "href": "05-media-threads.html",
    "label": "5",
    "title": "מדיה, הרשאות ו-Threads",
    "weeks": "גלריה, מצלמה, חילוץ פריימים, ExecutorService ו-Handler"
  },
  {
    "id": "s6",
    "href": "06-mlkit-math.html",
    "label": "6",
    "title": "ML Kit והמתמטיקה",
    "weeks": "זיהוי תנוחה, atan2, זוויות והציון"
  },
  {
    "id": "s7",
    "href": "07-release.html",
    "label": "7",
    "title": "מציאת רגע השחרור",
    "weeks": "חיפוש גס ומדויק, יישור המרפק, ובדיקה על סרטון אמיתי"
  },
  {
    "id": "s8",
    "href": "08-ui.html",
    "label": "8",
    "title": "View מותאם, RecyclerView ועיצוב",
    "weeks": "onDraw, Adapter, ViewHolder, strings.xml ו-Material"
  },
  {
    "id": "s9",
    "href": "09-exam-day.html",
    "label": "9",
    "title": "יום הבחינה",
    "weeks": "תסריט להדגמה, רשימת בדיקה ומגבלות"
  },
  {
    "id": "s10",
    "href": "10-practice.html",
    "label": "10",
    "title": "תרגול ומילון",
    "weeks": "כל שאלות הבוחן במקום אחד, ומילון מונחים"
  },
  {
    "id": "s11",
    "href": "11-android-concepts.html",
    "label": "11",
    "title": "מושגי Android Studio",
    "weeks": "59 מושגים לבגרות, עם דוגמאות קוד ושאלות"
  }
];

function renderTopNav(currentId) {
  const mount = document.getElementById("topnav-mount");
  if (!mount) return;
  const pills = PAGES.map(p => {
    const cls = "stage-pill" + (p.id === currentId ? " current" : "");
    return `<a class="${cls}" href="${p.href}" title="${p.title}">${p.label}</a>`;
  }).join("");
  mount.innerHTML = `
    <a class="stage-pill" href="index.html">🏠 בית</a>
    ${pills}
    <a class="brand" href="index.html">מדריך הבגרות 🏀</a>
  `;
}

function renderPrevNext(currentId) {
  const mount = document.getElementById("prevnext-mount");
  if (!mount) return;
  const idx = PAGES.findIndex(p => p.id === currentId);
  if (idx === -1) return;
  const prev = idx > 0 ? PAGES[idx - 1] : null;
  const next = idx < PAGES.length - 1 ? PAGES[idx + 1] : null;
  let out = prev ? `<a href="${prev.href}"><span class="dir">קודם</span>${prev.title}</a>` : `<span></span>`;
  out += next ? `<a href="${next.href}"><span class="dir">הבא</span>${next.title}</a>`
              : `<a href="index.html"><span class="dir">סיימת את כל השלבים</span>חזרה לדף הבית</a>`;
  mount.innerHTML = out;
}
