# Basketball Shot Analyzer

פרויקט גמר 5 יח"ל מדעי המחשב - אפליקציית אנדרואיד (Java) לניתוח זריקת קפיצה בכדורסל
בעזרת Google ML Kit Pose Detection.

> קוד שנוצר בעזרת AI מסומן בראש כל מחלקה: `// AI-GENERATED (Claude Code) - reviewed and understood by the student`

## מחלקות ותפקידן

| שכבה | מחלקה | אחריות |
|------|-------|--------|
| model | `User` | משתמש: שם, hash של סיסמה ו-salt |
| model | `ShotData` | זריקה: זוויות, ציון (מוגבל 0-100), משוב, הערה, זמן |
| model | `MonthlyStat` | שורת סטטיסטיקה חודשית (תוצאת GROUP BY) |
| model | `UserSummary` | סיכום משתמש (תוצאת JOIN) |
| db | `DatabaseHelper` | SQLite: טבלאות users/shots, CRUD מלא, GROUP BY ו-JOIN |
| util | `ShotMathUtils` | חישוב זוויות עם `Math.atan2`, נוסחת ציון, משוב בעברית |
| util | `AuthUtils` | בדיקת קלט, SHA-256 + salt לסיסמאות |
| util | `SessionManager` | SharedPreferences: משתמש מחובר ו"הישאר מחובר" |
| util | `ShotAnalyzer` | חילוץ פריימים (`MediaMetadataRetriever`) + ML Kit + בחירת רגע השחרור |
| ui | `PoseOverlayView` | View מותאם (`onDraw`): פריים, שלד ותוויות זווית |
| ui | `ShotAdapter` | Adapter ל-RecyclerView של היסטוריית הזריקות |
| ui | `ScoreColors` | צבע אחיד לציון בכל המסכים (ירוק / כתום / אדום) |
| - | `AuthActivity` | מסך התחברות/הרשמה |
| - | `MainActivity` | הרשאות, גלריה/מצלמה, ניתוח ב-ExecutorService + Handler |
| - | `HistoryActivity` | רשימת זריקות, עריכת הערה, מחיקה, סטטיסטיקה |
