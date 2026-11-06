// guide.js - Q&A bank, glossary, interactive labs and the practice quiz for the bagrut guide.
// Every block checks that its elements exist, so the same file is loaded on every page.
(function () {
  "use strict";

  var TOPICS = {
    app: "האפליקציה", arch: "ארכיטקטורה", oop: "OOP", activity: "Activities ו-Intent",
    security: "הצפנה וקלט", prefs: "SharedPreferences", sqlite: "SQLite", media: "הרשאות ומדיה",
    thread: "Thread ו-Handler", mlkit: "ML Kit", math: "מתמטיקה וציון", release: "רגע השחרור",
    view: "View מותאם", recycler: "RecyclerView", ui: "עיצוב ומשאבים", ai: "שימוש ב-AI"
  };

  // [topic, question, answer-html]
  var Q = [
    ["app", "תאר את האפליקציה במשפט אחד.", "אפליקציית אנדרואיד שמקבלת סרטון של זריקת קפיצה, מזהה בו נקודות גוף עם ML Kit, מוצאת את רגע השחרור, מחשבת זווית שחרור, נטיית גוף ויישור היד בסוף התנועה, נותנת ציון 0–100 ומשוב בעברית, ושומרת היסטוריה לכל משתמש ב-SQLite."],
    ["app", "למה דווקא 48.5 מעלות?", "זה ערך המטרה לזווית השחרור. זריקות מטווח בינוני יוצאות בדרך כלל בזווית של בערך 45–55 מעלות, ו-48.5 נמצא בתוך הטווח. הערך מוגדר כקבוע <code>IDEAL_RELEASE_ANGLE</code> ב-<code>ShotMathUtils</code>, כך שאפשר לשנות אותו במקום אחד."],
    ["app", "איך הציון מורכב?", "60% מזווית השחרור, 20% מיציבות הגוף (נטייה עד 10° בלי קנס), ו-20% מהמשך התנועה (מרפק של לפחות 160° בסוף). בכל רכיב יורדות 2 נקודות לכל מעלה של חריגה, והתוצאה מוגבלת ל-0–100."],
    ["app", "מה צריך כדי שהניתוח יצליח?", "סרטון שמצולם מהצד, שבו כל הגוף נראה, בתאורה טובה ועם שחקן אחד. אם ML Kit לא מזהה כתף, מרפק ושורש כף יד באף פריים, מוצגת הודעה שלא זוהה שחקן."],

    ["arch", "למה חילקת את הקוד לשכבות model, db, util ו-ui?", "הפרדת אחריות. לכל שכבה תפקיד אחד: model מחזיק נתונים, db ניגש למסד, util מכיל לוגיקה, ו-ui וה-Activities מציגים. ככה אפשר לשנות חלק אחד בלי לשבור אחר. למשל, אפשר להחליף את המסד בלי לגעת במסכים."],
    ["arch", "איפה נמצאים החישובים ולמה לא ב-Activity?", "ב-<code>ShotMathUtils</code> וב-<code>ShotAnalyzer</code> בתיקיית util. Activity אחראית על המסך. אם החישובים היו בה, אי אפשר היה לבדוק אותם בנפרד או להשתמש בהם ממקום אחר."],
    ["arch", "מה ההבדל בין ShotAnalyzer ל-ShotMathUtils?", "<code>ShotAnalyzer</code> עובד עם אנדרואיד וגוגל: סרטון, Bitmap ו-ML Kit, והוא איטי ורץ ברקע. <code>ShotMathUtils</code> הוא Java טהור: מקבל מספרים ומחזיר מספרים וטקסט, בלי תלות באנדרואיד."],
    ["arch", "למה כל ה-SQL נמצא רק ב-DatabaseHelper?", "כדי שיהיה מקום אחד שאחראי על המסד. מסכים קוראים למתודות כמו <code>getShotsForUser</code> ולא יודעים איך הטבלה בנויה."],

    ["oop", "איפה יש ירושה בפרויקט?", "<code>DatabaseHelper extends SQLiteOpenHelper</code>, <code>PoseOverlayView extends View</code>, כל ה-Activities <code>extends AppCompatActivity</code>, <code>ShotAdapter extends RecyclerView.Adapter</code>, ו-<code>ShotViewHolder extends RecyclerView.ViewHolder</code>."],
    ["oop", "איפה יש ממשק (interface) ולמה?", "<code>ShotAdapter.OnShotActionListener</code>, עם <code>onEditNote</code> ו-<code>onDelete</code>. <code>HistoryActivity</code> מממשת אותו. כך ה-Adapter מודיע על לחיצה בלי להכיר את המסד או את ה-Activity."],
    ["oop", "איפה יש העמסת בנאים?", "ב-<code>User</code> וב-<code>ShotData</code>. בנאי קצר לאובייקט חדש (בלי id), ובנאי מלא לאובייקט שנקרא מהמסד. הקצר קורא למלא עם <code>this(...)</code>."],
    ["oop", "מה זה הכמסה ואיפה היא בפרויקט?", "הסתרת השדות (<code>private</code>) וגישה דרך מתודות. ב-<code>ShotData.setScore</code> המחלקה מגבילה את הציון ל-0–100, כך שאי אפשר להכניס ערך לא חוקי."],
    ["oop", "למה ל-ShotMathUtils יש בנאי private?", "זו מחלקת עזר עם מתודות סטטיסטיות בלבד. אין סיבה ליצור ממנה אובייקט, והבנאי הפרטי מונע את זה. היא גם <code>final</code>, כדי שאי אפשר יהיה לרשת ממנה."],
    ["oop", "מה זה ShotAnalyzer.Result ולמה הוא static?", "מחלקה פנימית שמקבצת את כל תוצאות הניתוח: פריים, נקודות, זוויות, ציון ומשוב. היא static כי היא לא צריכה גישה לאובייקט של <code>ShotAnalyzer</code> שיצר אותה."],
    ["oop", "מה עושה @Override?", "מסמן שהמתודה דורסת מתודה ממחלקת האב או מממשת מתודה מממשק. אם השם או הפרמטרים לא תואמים, הקומפיילר מתריע."],

    ["activity", "מה קורה כשפותחים את האפליקציה?", "<code>AuthActivity</code> מוגדרת כ-LAUNCHER ב-Manifest. ב-<code>onCreate</code> היא בודקת <code>session.shouldAutoLogin()</code>. אם המשתמש ביקש להישאר מחובר, עוברים ישר ל-<code>MainActivity</code> וסוגרים את מסך ההתחברות. אחרת מוצג טופס ההתחברות."],
    ["activity", "מה זה Intent? תן דוגמה למפורש ולמרומז.", "הודעה שמבקשת מהמערכת להפעיל רכיב. מפורש: <code>new Intent(this, MainActivity.class)</code>, מסך מסוים שלי. מרומז: <code>MediaStore.ACTION_VIDEO_CAPTURE</code>, \"מישהו שיודע לצלם וידאו\", והמערכת מוצאת את אפליקציית המצלמה."],
    ["activity", "למה קוראים ל-finish() אחרי מעבר למסך הראשי?", "כדי להוציא את מסך ההתחברות מערימת המסכים. בלי זה, לחיצה על \"חזרה\" במסך הראשי הייתה מחזירה להתחברות."],
    ["activity", "מה עושים הדגלים NEW_TASK ו-CLEAR_TASK ב-RegisterActivity?", "מוחקים את כל המסכים הקודמים, כלומר גם את ההתחברות וגם את ההרשמה. המסך הראשי נפתח כמסך היחיד, ו\"חזרה\" יוצאת מהאפליקציה."],
    ["activity", "למה ב-onDestroy יש קוד?", "כדי לשחרר משאבים: <code>executor.shutdownNow()</code> עוצר את ה-thread ברקע, ו-<code>analyzer.close()</code> משחרר את ML Kit."],
    ["activity", "מה זה AndroidManifest.xml?", "קובץ שמתאר את האפליקציה למערכת: אילו מסכים יש, מי מסך הפתיחה, אילו הרשאות נדרשות, האייקון וערכת הנושא. Activity שלא רשומה בו לא תיפתח."],

    ["security", "למה לא שומרים את הסיסמה כמו שהיא?", "אם מישהו משיג את קובץ המסד, הוא יראה את כל הסיסמאות, ואנשים משתמשים באותה סיסמה בכמה אתרים. hash הוא חד-כיווני: אי אפשר לשחזר ממנו את הסיסמה."],
    ["security", "מה זה salt ולמה צריך אותו?", "מחרוזת אקראית (16 בתים מ-<code>SecureRandom</code>) שמצרפים לסיסמה לפני ה-hash. בזכותה לשני משתמשים עם אותה סיסמה יש hash שונה, ואי אפשר להשתמש בטבלאות מוכנות של hash-ים לסיסמאות נפוצות. ה-salt נשמר במסד ליד ה-hash."],
    ["security", "איך בודקים סיסמה בהתחברות?", "שולפים את המשתמש לפי שם, מחשבים <code>hashPassword(הסיסמה שהוקלדה, user.getSalt())</code>, ומשווים ל-hash השמור. אם שווים, הסיסמה נכונה."],
    ["security", "איזה אלגוריתם בחרת ומה החולשה שלו?", "SHA-256 עם salt. החולשה: הוא מהיר, ולכן תוקף יכול לנסות הרבה ניחושים בשנייה. במערכת אמיתית הייתי משתמש ב-PBKDF2 או bcrypt, שהם איטיים בכוונה. לפרויקט, SHA-256 עם salt ברור ומדגים את הרעיון."],
    ["security", "אילו בדיקות קלט יש?", "שם משתמש: לפחות 3 תווים, רק אותיות באנגלית, ספרות וקו תחתון. סיסמה: לפחות 6 תווים ולפחות ספרה אחת. בהרשמה: שתי הסיסמאות זהות, ושם המשתמש לא תפוס. כל שגיאה מוצגת מתחת לשדה שלה."],
    ["security", "למה SecureRandom ולא Random?", "<code>Random</code> צפוי: מי שיודע את נקודת ההתחלה יכול לחשב את המספרים. <code>SecureRandom</code> מייצר מספרים שאי אפשר לנחש, כמו שצריך בהצפנה."],
    ["security", "איך יודעים שהשם תפוס?", "לעמודה <code>username</code> יש אילוץ <code>UNIQUE</code>. אם מנסים להכניס שם קיים, <code>insert</code> מחזיר ‎-1, ואז מוצגת השגיאה \"שם המשתמש כבר תפוס\"."],

    ["prefs", "מה נשמר ב-SharedPreferences?", "בקובץ <code>session</code>, במצב <code>MODE_PRIVATE</code>: מזהה המשתמש, שם המשתמש, והאם הוא ביקש להישאר מחובר. הסיסמה לא נשמרת שם."],
    ["prefs", "מה ההבדל בין apply() ל-commit()?", "<code>apply()</code> כותב לקובץ ברקע ולא מחזיר ערך. <code>commit()</code> כותב מיד, חוסם עד שמסתיים, ומחזיר הצלחה או כישלון. השתמשתי ב-apply כדי לא לחסום את המסך."],
    ["prefs", "מה קורה אם לא מסמנים \"הישאר מחובר\"?", "ההתחברות עדיין נשמרת עם <code>remember_me=false</code>, כי המסכים צריכים לדעת מי מחובר. בפתיחה הבאה <code>shouldAutoLogin()</code> מחזיר false, ומוצג מסך ההתחברות."],
    ["prefs", "למה לא לשמור את הזריקות ב-SharedPreferences?", "הוא מתאים לכמה ערכים פשוטים. לזריקות יש מבנה, קשר למשתמש, ושאילתות כמו ממוצע לפי חודש, ולזה צריך מסד נתונים."],

    ["sqlite", "מה התפקיד של SQLiteOpenHelper?", "מחלקת אב שמנהלת את קובץ המסד: קוראת ל-<code>onCreate</code> כשהמסד לא קיים, ל-<code>onUpgrade</code> כשמעלים את מספר הגרסה, ונותנת <code>getWritableDatabase()</code> ו-<code>getReadableDatabase()</code>."],
    ["sqlite", "תאר את הטבלאות והקשר ביניהן.", "<code>users</code>: id, username (UNIQUE), password_hash, salt. <code>shots</code>: id, user_id, זוויות, ציון, משוב, הערה וזמן. <code>user_id</code> הוא מפתח זר ל-<code>users.id</code>: קשר של אחד-לרבים, כי למשתמש יש הרבה זריקות."],
    ["sqlite", "למה צריך onConfigure?", "ב-SQLite אכיפת מפתחות זרים כבויה כברירת מחדל. <code>setForeignKeyConstraintsEnabled(true)</code> מפעיל אותה, ובלי זה <code>ON DELETE CASCADE</code> לא היה עובד."],
    ["sqlite", "מה זה ON DELETE CASCADE?", "כשמוחקים משתמש, כל הזריקות שלו נמחקות אוטומטית. כך לא נשארות זריקות \"יתומות\" בלי משתמש."],
    ["sqlite", "הסבר את שאילתת ה-GROUP BY.", "<code>strftime('%Y-%m', timestamp / 1000, 'unixepoch', 'localtime')</code> הופך את הזמן לחודש כמו 2026-10. מחלקים ב-1000 כי הזמן שמור במילישניות. <code>GROUP BY month</code> מאחד את כל הזריקות של אותו חודש, ו-<code>AVG(score)</code> ו-<code>COUNT(*)</code> נותנים ממוצע וכמות."],
    ["sqlite", "הסבר את ה-JOIN ולמה דווקא LEFT JOIN.", "מחברים את <code>users</code> ל-<code>shots</code> לפי <code>u.id = s.user_id</code>, ומחשבים <code>COUNT</code>, <code>AVG</code> ו-<code>MAX</code>. LEFT JOIN מחזיר שורה גם למשתמש בלי זריקות. אז AVG ו-MAX הם NULL, ו-<code>IFNULL</code> הופך אותם ל-0."],
    ["sqlite", "למה משתמשים ב-? ולא משרשרים את הערך?", "שאילתה עם פרמטרים: SQLite מתייחס לערך כנתון ולא כחלק מהפקודה. זה מונע SQL Injection, וגם מטפל בגרשיים ובתווים מיוחדים."],
    ["sqlite", "מה זה Cursor ולמה סוגרים אותו?", "מצביע על שורות התוצאה של שאילתה. מזיזים אותו עם <code>moveToNext()</code> וקוראים עמודות עם <code>getInt</code>, <code>getString</code> וכו'. סוגרים עם <code>close()</code> כדי לשחרר את הזיכרון והחיבור."],
    ["sqlite", "מה זה ContentValues?", "מפה של שם עמודה ← ערך, שמעבירים ל-<code>insert</code> ול-<code>update</code>, במקום לכתוב את פקודת ה-SQL בעצמנו."],
    ["sqlite", "איפה ה-CRUD המלא?", "users: <code>insertUser</code>, <code>getUserByUsername</code>, <code>updateUserPassword</code>, <code>deleteUser</code>. shots: <code>insertShot</code>, <code>getShotsForUser</code>, <code>updateShotNote</code>, <code>updateShotResult</code>, <code>deleteShot</code>. בממשק משתמשים בפועל בהוספה, קריאה, עריכת הערה ומחיקה של זריקות, ובהוספה וקריאה של משתמשים."],

    ["media", "למה מבקשים הרשאה בזמן ריצה ולא רק ב-Manifest?", "מאנדרואיד 6 הרשאות \"מסוכנות\", כמו גישה לקבצים, צריכות אישור של המשתמש בזמן ריצה. ה-Manifest רק מצהיר שהאפליקציה עשויה לבקש אותן."],
    ["media", "למה יש שתי הרשאות שונות לקריאת וידאו?", "מאנדרואיד 13 (API 33) ההרשאה נקראת <code>READ_MEDIA_VIDEO</code>. לפני כן <code>READ_EXTERNAL_STORAGE</code>. הקוד בודק את <code>Build.VERSION.SDK_INT</code> ומבקש את המתאימה."],
    ["media", "מה זה ActivityResultLauncher?", "הדרך המודרנית לפתוח מסך ולקבל ממנו תוצאה (במקום <code>startActivityForResult</code>). רושמים אותו כשדה עם <code>registerForActivityResult</code>, ואז <code>launch(...)</code> פותח, והתוצאה מגיעה ל-callback."],
    ["media", "למה לא ביקשת הרשאת CAMERA?", "כי האפליקציה לא משתמשת במצלמה בעצמה. היא מבקשת מאפליקציית המצלמה לצלם, דרך Intent מרומז. אם הייתי מצהיר על CAMERA בלי שאושרה, פתיחת המצלמה דרך Intent הייתה נכשלת עם SecurityException."],
    ["media", "איך מחלצים פריים מסרטון?", "<code>MediaMetadataRetriever</code>: <code>setDataSource(context, uri)</code>, אורך עם <code>extractMetadata(METADATA_KEY_DURATION)</code>, ו-<code>getFrameAtTime(זמן במיקרו-שניות, OPTION_CLOSEST)</code>. בסוף <code>release()</code> בתוך <code>finally</code>."],
    ["media", "למה מקטינים כל פריים ל-640 פיקסלים?", "תמונה קטנה יותר מזוהה מהר יותר ותופסת פחות זיכרון, ולזיהוי מפרקים זו רזולוציה מספיקה."],

    ["thread", "למה הניתוח לא רץ על ה-thread הראשי?", "ה-thread הראשי מצייר את המסך ומגיב לנגיעות. ניתוח של עשרות פריימים לוקח שניות, והמסך היה קופא. אחרי כ-5 שניות אנדרואיד מציגה הודעת ANR. בנוסף, <code>Tasks.await</code> של ML Kit זורק חריגה אם קוראים לו על ה-thread הראשי."],
    ["thread", "מה זה ExecutorService?", "מנגנון שמנהל threads ומריץ עליהם משימות. <code>newSingleThreadExecutor()</code> יוצר thread עובד אחד, ומשימות רצות בו לפי הסדר."],
    ["thread", "מה זה Handler ו-Looper?", "ל-thread הראשי יש תור הודעות ו-Looper שמריץ אותן. <code>new Handler(Looper.getMainLooper())</code> מחובר לתור הזה, ו-<code>post(...)</code> מכניס אליו קוד שירוץ על ה-thread הראשי."],
    ["thread", "למה צריך לחזור ל-thread הראשי כדי לעדכן את המסך?", "רכיבי המסך לא בנויים לגישה מכמה threads בבת אחת. אנדרואיד מתירה לגעת בהם רק מה-thread הראשי, ואחרת נזרקת <code>CalledFromWrongThreadException</code>."],
    ["thread", "מה קורה אם יוצאים מהמסך באמצע ניתוח?", "<code>onDestroy</code> קורא ל-<code>shutdownNow()</code> ול-<code>analyzer.close()</code>. אם תוצאה בכל זאת מגיעה, <code>showResult</code> בודק <code>isFinishing() || isDestroyed()</code> ולא נוגע במסך."],
    ["thread", "איפה עוד משתמשים ב-ExecutorService חוץ מהניתוח הראשון?", "בכפתורי \"פריים אחורה / קדימה\". גם שם צריך לחלץ פריים ולהריץ ML Kit, ולכן <code>stepFrame</code> רץ באותו executor ברקע, מעדכן את המסד, וחוזר ל-thread הראשי עם <code>mainHandler.post</code>. בזמן הריצה הכפתורים מושבתים."],
    ["thread", "באיזה thread נשמרת הזריקה במסד?", "ברקע, ב-<code>saveShot</code>, לפני שחוזרים ל-thread הראשי. כתיבה למסד היא פעולת דיסק, ועדיף לא לבצע אותה על ה-thread הראשי."],

    ["mlkit", "מה זה ML Kit Pose Detection?", "ספרייה של Google שמריצה מודל למידת מכונה על המכשיר, בלי אינטרנט. היא מחזירה 33 נקודות גוף, ולכל נקודה מיקום וסבירות שהיא נראית בתמונה."],
    ["mlkit", "מה ההבדל בין SINGLE_IMAGE_MODE ל-STREAM_MODE?", "SINGLE_IMAGE_MODE מנתח כל תמונה בנפרד, בדיוק גבוה. STREAM_MODE מיועד למצלמה חיה, עוקב אחרי האדם בין פריימים ומהיר יותר. אצלנו כל פריים נשלף בנפרד, ולכן מצב תמונה בודדת."],
    ["mlkit", "מה זה getInFrameLikelihood ולמה הסף 0.5?", "ההסתברות (0 עד 1) שהנקודה באמת נראית בתמונה ולא הוערכה. מתחת ל-0.5 הנקודה כנראה מוסתרת, ואז לא משתמשים בה."],
    ["mlkit", "איך יודעים איזו יד זורקת?", "בחיפוש הגס: היד ששורש כף היד שלה גבוה יותר בתמונה (y קטן יותר). בחיפוש המדויק נשארים עם אותה יד שנמצאה בשיא, כדי לא להתבלבל באמצע."],
    ["mlkit", "מה קורה אם ML Kit מזהה \"אדם\" שהוא לא אדם?", "זה קרה בסרטון בדיקה: ML Kit זיהה את רשת הסל עם הכדור כגוף, כי הוא מחזיר גוף אחד בכל תמונה וכשאין אחד ברור הוא מנחש. לכן <code>isUprightBody</code> בודקת שהגוף בנוי כמו אדם עומד: כתף מעל ירך מעל ברך, וירך באורך של לפחות חצי מהגו. פריים שלא עומד בזה נפסל."],
    ["mlkit", "למה דווקא הברך ולא הקרסול?", "הברך מספיקה כדי לוודא שזה גוף עומד, והיא כמעט תמיד בתמונה. אם הייתי דורש קרסול, כל סרטון שבו כפות הרגליים קצת חתוכות היה נכשל."],
    ["mlkit", "מה זה Task ו-Tasks.await?", "<code>detector.process()</code> עובד בצורה אסינכרונית ומחזיר Task, כלומר תוצאה שתגיע בעתיד. <code>Tasks.await</code> מחכה לה באותו thread. מותר רק ב-thread ברקע."],

    ["math", "מה עושה Math.atan2(dy, dx)?", "מחזיר את הזווית של הווקטור (dx, dy) מול ציר ה-x החיובי, ברדיאנים בין ‎-π ל-π. בניגוד ל-<code>atan(dy/dx)</code>, הוא מבחין בין ארבעת הרביעים ולא נכשל כש-dx=0."],
    ["math", "איך מחושבת זווית המרפק?", "<code>angleAtPoint(כתף, מרפק, שורש כף יד)</code>: מחשבים את הכיוון מהמרפק לכתף ואת הכיוון מהמרפק לשורש כף היד עם atan2, מחסירים, לוקחים ערך מוחלט וממירים למעלות. אם יצא מעל 180, מחזירים <code>360 פחות</code> התוצאה."],
    ["math", "למה \"אם מעל 180 אז 360 פחות\"?", "ההפרש בין שני כיוונים יכול להגיע עד 360 מעלות. אנחנו רוצים את הזווית הפנימית בין שני הקטעים, שהיא תמיד בין 0 ל-180."],
    ["math", "למה ב-releaseAngle הופכים את dy?", "בתמונה ציר ה-Y גדל כלפי מטה. כשהיד מעל המרפק, ה-y שלה קטן יותר. <code>elbowY - wristY</code> יוצא חיובי כשהיד למעלה, כמו בציר רגיל."],
    ["math", "למה Math.abs על dx בזווית השחרור?", "כדי שהתוצאה תהיה זהה בין אם השחקן פונה ימינה או שמאלה בסרטון."],
    ["math", "למה ב-torsoLean כתוב atan2(dx, dy) בסדר הפוך?", "כי מודדים מול הקו האנכי ולא מול האופקי. החלפת הארגומנטים נותנת את הזווית מהאנך: גוף זקוף נותן 0."],
    ["math", "חשב ציון: שחרור 55°, נטייה 12°, מרפק בסוף 150°.", "שחרור: סטייה 6.5° ← 100 − 13 = 87. יציבות: 2° מעל 10 ← 96. המשך תנועה: 10° מתחת ל-160 ← 80. סה\"כ 0.6×87 + 0.2×96 + 0.2×80 = 87.4, אחרי עיגול 87."],
    ["math", "מה קורה אם הירך לא זוהתה?", "נטיית הגוף היא <code>NaN</code> (\"לא מספר\"). בציון רכיב היציבות נחשב 100, כלומר בלי קנס, ובמסך מופיע \"הירך לא זוהתה\"."],
    ["math", "מה עושה partScore?", "ציון של רכיב אחד: <code>100 - max(0, חריגה) × 2</code>, מעוגל ומוגבל ל-0–100. חריגה שלילית, כלומר בתוך הטווח, לא מורידה נקודות."],

    ["release", "איך מוצאים את רגע השחרור?", "בשלושה צעדים. (1) חיפוש גס: פריים כל 250ms לאורך הסרטון, ומציאת הדגימה שבה היד הכי גבוהה ביחס לגוף. (2) חזרה אחורה לתחילת \"היד למעלה\": הדגימה הראשונה שבה היד כבר בגובה של לפחות 90% מהשיא. (3) חיפוש מדויק, כל 33ms, סביב הרגע הזה: לכל פריים שומרים את זווית המרפק, ו-<code>releaseIndex</code> בוחר את הפריים הראשון שבו המרפק כמעט ישר."],
    ["release", "למה רגע השחרור הוא כשהמרפק מסיים להתיישר?", "בזריקת קפיצה היד דוחפת את הכדור למעלה ומיישרת את המרפק. הכדור עוזב את היד בסוף היישור. אחרי זה היד נשארת ישרה (follow-through), ורק כף היד מצליפה. לכן הפריים הראשון שבו המרפק כמעט ישר הוא רגע השחרור."],
    ["release", "למה גובה היד מחולק באורך הגו?", "כדי שהגודל של השחקן בתמונה לא ישפיע. בסרטון הבדיקה, כשהשחקן עמד צמוד למצלמה הוא היה ענק בתמונה, וכל תזוזה של היד \"ניצחה\" בפיקסלים את הזריקה האמיתית שצולמה מרחוק. ביחידות של אורך גו (מרחק כתף-ירך) הגובה לא תלוי במרחק מהמצלמה."],
    ["release", "למה פוסלים פריים אם נקודה מחוץ לתמונה?", "כשרק חלק מהגוף נראה, למשל רק רגליים או רק פנים וידיים, ML Kit מנחש איפה שאר הנקודות, לפעמים מחוץ לתמונה. החישובים על ניחוש כזה שגויים, ולכן <code>isInFrame</code> דורש שהכתף, המרפק, שורש כף היד והירך יהיו מזוהים ובתוך גבולות התמונה."],
    ["release", "למה חוזרים אחורה מהשיא ולא מחפשים סביבו?", "כי אחרי זריקה טובה מחזיקים את היד למעלה, לפעמים יותר משנייה. הרגע שבו היד הכי גבוהה מגיע אז הרבה אחרי שהכדור עזב. בסרטון הבדיקה זה גרם לטעות של חצי שנייה. לכן <code>plateauStartIndex</code> הולך אחורה כל עוד היד עדיין למעלה, ומשם מחפשים."],
    ["release", "למה מחליקים עם חציון ולא עם ממוצע?", "ML Kit לפעמים טועה בפריים בודד ונותן זווית לא הגיונית. חציון של 3 פריימים מתעלם מערך קיצוני אחד. ממוצע היה מושפע ממנו, וגם \"מאחר\" כשהזווית עולה בהדרגה: בסדרה עולה, החציון של שלושה ערכים הוא בדיוק הערך האמצעי, ולכן אין עיכוב."],
    ["release", "איך בדקת שהזיהוי באמת עובד?", "על סרטון אמיתי של 27 שניות עם שתי זריקות. הרצתי את אותו אלגוריתם בסביבת בדיקה עם MediaPipe Pose (אותה משפחת מודלים כמו ML Kit), וסימנתי בעין, פריים אחרי פריים, את הרגע שבו הכדור נפרד מהאצבעות. אחרי התיקונים: בזריקה אחת מדויק, ובשנייה פריים אחד (33ms) מאוחר. לפני התיקונים האפליקציה בכלל לא מצאה את הזריקה."],
    ["release", "מה עושים כפתורי \"פריים אחורה / קדימה\"?", "תיקון ידני של רגע השחרור. <code>ShotAnalyzer.stepFrame</code> מחלץ את הפריים השכן (33ms), מריץ עליו ML Kit עם אותה יד, ומחשב מחדש זוויות, ציון ומשוב. אם בפריים לא זוהתה היד, מדלגים עד 3 פריימים. זה רץ ב-thread ברקע, והתוצאה מעדכנת גם את המסך וגם את הזריקה במסד."],
    ["release", "למה מרווח של 8 מעלות מהמקסימום ולא המקסימום עצמו?", "אחרי השחרור היד ממשיכה להתיישר עוד קצת, ולכן הזווית המקסימלית מגיעה מעט אחרי שהכדור כבר עזב. סף של 8° מתחת למקסימום תופס את הרגע שבו היד \"כבר כמעט ישרה\". בסרטון הבדיקה ניסיתי גם 12°: זה תיקן זריקה אחת וקלקל את השנייה, ולכן נשארתי עם 8."],
    ["release", "למה לא לנתח את כל הפריימים?", "בסרטון של דקה ב-30 פריימים לשנייה יש 1,800 פריימים, וכל אחד לוקח זמן לחלץ ולזהות. בשיטה הדו-שלבית מנתחים כ-240 פריימים בחיפוש הגס ועוד כ-29 בחיפוש המדויק."],
    ["release", "למה בחיפוש הגס מוחקים את התמונה מכל דגימה?", "כל פריים תופס כמה מגה-בייט. 240 דגימות היו תופסות מאות מגה-בייט, והאפליקציה הייתה עלולה לקרוס. לחישובים מספיקות נקודות הגוף, ואת התמונה של רגע השחרור מחלצים מחדש בסוף."],
    ["release", "מה זה follow-through ואיך מודדים אותו?", "המשך התנועה אחרי השחרור: יד שנשארת מושטת אל הסל. מודדים את זווית המרפק בדגימה שבה היד הכי גבוהה. 160° ומעלה נחשב יד ישרה."],

    ["view", "איך יוצרים View מותאם אישית?", "יורשים מ-<code>View</code>, כותבים בנאי שמקבל <code>(Context, AttributeSet)</code> כדי שאפשר יהיה לשים אותו ב-XML, ודורסים את <code>onDraw(Canvas)</code>."],
    ["view", "מה ההבדל בין Canvas ל-Paint?", "Canvas הוא משטח הציור, עם הפקודות: <code>drawLine</code>, <code>drawCircle</code>, <code>drawText</code>, <code>drawBitmap</code>. Paint מגדיר את הסגנון: צבע, עובי קו, גודל ופונט."],
    ["view", "מה עושה invalidate()?", "מסמן שה-View צריך ציור מחדש. המערכת תקרא ל-<code>onDraw</code> בפריים הבא. אף פעם לא קוראים ל-<code>onDraw</code> בעצמנו."],
    ["view", "למה יוצרים את ה-Paint בבנאי ולא ב-onDraw?", "<code>onDraw</code> נקראת הרבה פעמים, למשל בכל תזוזה של זום. יצירת אובייקטים בתוכה מעמיסה על איסוף הזבל וגורמת לגמגום."],
    ["view", "איך ממירים נקודה מהתמונה למסך?", "<code>scale = min(רוחב View / רוחב תמונה, גובה View / גובה תמונה)</code>, כדי שהתמונה תיכנס בלי עיוות. ההיסט ממרכז אותה. כל נקודה: <code>x × scale + offsetX</code>."],
    ["view", "איך עובד הזום?", "<code>ScaleGestureDetector</code> מזהה צביטה ו-<code>GestureDetector</code> מזהה גרירה והקשה כפולה. השינוי נשמר ב-<code>Matrix</code>, וב-<code>onDraw</code> מחילים אותו עם <code>canvas.concat</code>, בין <code>save()</code> ל-<code>restore()</code>."],
    ["view", "מה עושה requestDisallowInterceptTouchEvent?", "מבקש מה-ScrollView שמכיל את הרכיב לא \"לגנוב\" את הנגיעה. בזמן זום, גרירה מזיזה את התמונה ולא גוללת את המסך."],

    ["recycler", "למה RecyclerView ולא סתם הרבה Views?", "הוא יוצר רק את השורות שנראות במסך וממחזר אותן בגלילה. ככה גם רשימה ארוכה מאוד לא צורכת הרבה זיכרון."],
    ["recycler", "מה התפקיד של ה-Adapter?", "מחבר בין רשימת הנתונים לשורות במסך. <code>onCreateViewHolder</code> יוצר שורה מ-XML, <code>onBindViewHolder</code> ממלא אותה בנתוני זריקה, ו-<code>getItemCount</code> אומר כמה שורות יש."],
    ["recycler", "מה זה ViewHolder?", "אובייקט ששומר הפניות לרכיבים של שורה אחת, כדי לא לקרוא ל-<code>findViewById</code> בכל גלילה."],
    ["recycler", "מה זה LayoutManager?", "קובע איך השורות מסודרות על המסך. <code>LinearLayoutManager</code> יוצר רשימה אנכית אחת."],
    ["recycler", "תאר מה קורה כשמוחקים זריקה.", "לחיצה על \"מחיקה\" ← ה-Adapter קורא ל-<code>listener.onDelete(shot)</code> ← <code>HistoryActivity</code> מציגה דיאלוג אישור ← באישור <code>dbHelper.deleteShot(id)</code> ← <code>refresh()</code> טוען מחדש את הרשימה והסטטיסטיקה ← <code>notifyDataSetChanged()</code> ← Snackbar \"הזריקה נמחקה\"."],

    ["ui", "למה כל הטקסטים נמצאים ב-strings.xml?", "כדי שכל הטקסט יהיה במקום אחד: קל לשנות, לתרגם לשפה אחרת, ולשמור על עקביות. זו גם דרישה של המחוון: בלי מחרוזות קשיחות ב-layout."],
    ["ui", "מה המשמעות של %1$.1f?", "הארגומנט הראשון, כמספר עשרוני עם ספרה אחת אחרי הנקודה. <code>%1$d</code> הוא מספר שלם ו-<code>%1$s</code> מחרוזת. ממלאים עם <code>getString(R.string.x, ערכים...)</code>."],
    ["ui", "איך האפליקציה תומכת בעברית מימין לשמאל?", "<code>android:supportsRtl=\"true\"</code> ב-Manifest, <code>marginStart</code>/<code>marginEnd</code> במקום Left/Right, ואייקונים עם <code>autoMirrored</code> שמתהפכים לבד."],
    ["ui", "מה זה Theme ו-Style?", "Theme הוא ערכת הנושא של כל האפליקציה (צבעים ראשיים, רקע, שורת סטטוס). Style הוא אוסף מאפיינים לרכיב, כמו <code>Widget.App.Button</code>, שמשתמשים בו בכמה מקומות במקום לחזור על אותם מאפיינים."],
    ["ui", "מה ההבדל בין Toast ל-Snackbar?", "שניהם הודעה זמנית. Snackbar הוא רכיב של Material Design שמופיע בתחתית המסך ויכול לכלול כפתור פעולה. Toast הוא הודעה פשוטה של המערכת. השתמשתי ב-Snackbar לשגיאות ולמחיקה, וב-Toast להרשמה מוצלחת."],

    ["ai", "האם כתבת את הקוד בעצמך?", "השתמשתי ב-Claude Code ככלי, וכל מחלקה שנכתבה כך מסומנת בהערה בראשה, לפי דרישת בית הספר. אני הגדרתי את הדרישות, בדקתי והרצתי את האפליקציה, ואני מבין כל שורה. (ענה כאן בכנות לפי מה שקרה אצלך.)"],
    ["ai", "תסביר שורה שאני אבחר בקוד.", "התשובה הנכונה היא להסביר בדיוק מה השורה עושה ולמה היא שם. כדי להתכונן, עבור על המדריך פרק אחרי פרק, ותרגל עד שאתה מסמן \"אני יודע את זה\" על כל השאלות."]
  ];

  var GLOSS = [
    ["Activity", "מסך אחד באפליקציה, עם מחזור חיים שהמערכת מנהלת (onCreate, onDestroy)."],
    ["Intent", "בקשה להפעיל רכיב: מסך אחר, או אפליקציה אחרת כמו המצלמה."],
    ["Manifest", "קובץ שמתאר את האפליקציה למערכת: מסכים, הרשאות, ערכת נושא."],
    ["Layout (XML)", "קובץ שמתאר את מבנה המסך: אילו רכיבים יש ואיך הם מסודרים."],
    ["View / ViewGroup", "רכיב מסך (כפתור, טקסט). ViewGroup מכיל רכיבים אחרים, כמו LinearLayout."],
    ["Canvas / Paint", "משטח ציור ופקודות הציור / הגדרת הסגנון של מה שמצויר."],
    ["Bitmap", "תמונה בזיכרון, מורכבת מפיקסלים."],
    ["Uri", "כתובת של משאב, למשל סרטון בגלריה."],
    ["UI thread", "ה-thread הראשי: מצייר את המסך ומגיב לנגיעות."],
    ["ExecutorService", "מנגנון שמריץ משימות על threads ברקע."],
    ["Handler / Looper", "Looper מריץ תור הודעות של thread. Handler שולח אליו קוד."],
    ["ANR", "Application Not Responding: האפליקציה חסמה את ה-thread הראשי יותר מדי זמן."],
    ["SQLite", "מסד נתונים יחסי קטן, שנשמר כקובץ בתוך האפליקציה."],
    ["Primary / Foreign key", "מזהה ייחודי לשורה / עמודה שמפנה למזהה בטבלה אחרת."],
    ["Cursor", "מצביע על שורות התוצאה של שאילתה."],
    ["ContentValues", "מפה של עמודה ← ערך, להוספה ולעדכון."],
    ["CRUD", "Create, Read, Update, Delete: ארבע הפעולות הבסיסיות על נתונים."],
    ["Hash", "פונקציה חד-כיוונית: מסיסמה מקבלים מחרוזת, אבל לא להפך."],
    ["Salt", "ערך אקראי שמצורף לסיסמה לפני ה-hash, שונה לכל משתמש."],
    ["SharedPreferences", "אחסון קטן של זוגות מפתח-ערך."],
    ["RecyclerView", "רשימה שממחזרת שורות בזמן גלילה."],
    ["Adapter / ViewHolder", "מחבר נתונים לשורות / שומר הפניות לרכיבי שורה."],
    ["ML Kit", "ספריית למידת מכונה של Google שרצה על המכשיר."],
    ["Landmark", "נקודת גוף ש-ML Kit מזהה, כמו מרפק או כתף."],
    ["atan2", "פונקציה שמחזירה את הכיוון של וקטור לפי dx ו-dy."],
    ["Radian", "יחידת זווית: π רדיאנים = 180 מעלות."],
    ["Gradle / AGP", "מערכת הבנייה של הפרויקט / הפלאגין שלה לאנדרואיד."],
    ["Material Design", "שפת העיצוב של Google: רכיבים, צבעים וטיפוגרפיה אחידים."],
    ["RTL", "Right-to-Left: תמיכה בשפות שנכתבות מימין לשמאל."]
  ];

  function esc(s) { return String(s).replace(/[&<>"]/g, function (c) { return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]; }); }

  // ---- per-chapter Q&A ----
  document.querySelectorAll(".qa[data-topic]").forEach(function (box) {
    var t = box.getAttribute("data-topic");
    var items = Q.filter(function (q) { return q[0] === t; });
    if (!items.length) { box.remove(); return; }
    var h = '<h4>שאלות בוחן</h4>';
    items.forEach(function (q) { h += '<details><summary>' + esc(q[1]) + '</summary><div class="ans"><p>' + q[2] + '</p></div></details>'; });
    box.innerHTML = h;
  });
  var qc = document.getElementById("qcount"); if (qc) qc.textContent = Q.length;

  // ---- glossary ----
  var gl = document.getElementById("glossList"); if (gl) gl.innerHTML = GLOSS.map(function (g) { return "<dt>" + esc(g[0]) + "</dt><dd>" + esc(g[1]) + "</dd>"; }).join("");

  // ---- math ports (identical to ShotMathUtils) ----
  var IDEAL = 48.5, PPD = 2, GOOD_RANGE = 5, MAX_LEAN = 10, MIN_FOLLOW = 160;
  function toDeg(r) { return r * 180 / Math.PI; }
  function angleAtPoint(ax, ay, bx, by, cx, cy) {
    var a = Math.atan2(ay - by, ax - bx), c = Math.atan2(cy - by, cx - bx);
    var d = Math.abs(toDeg(a - c));
    return d > 180 ? 360 - d : d;
  }
  function releaseAngle(ex, ey, wx, wy) { return toDeg(Math.atan2(ey - wy, Math.abs(wx - ex))); }
  function partScore(over) { var s = Math.round(100 - Math.max(0, over) * PPD); return Math.max(0, Math.min(100, s)); }
  function calcScore(rel) { return partScore(Math.abs(rel - IDEAL)); }
  function totalScore(rel, lean, follow) {
    var stab = isNaN(lean) ? 100 : partScore(lean - MAX_LEAN);
    return Math.round(0.6 * calcScore(rel) + 0.2 * stab + 0.2 * partScore(MIN_FOLLOW - follow));
  }
  function feedback(rel, lean, follow) {
    var dev = rel - IDEAL, f;
    if (dev < -GOOD_RANGE) f = "זווית השחרור נמוכה מדי - נסה לשחרר את הכדור בקשת גבוהה יותר.";
    else if (dev > GOOD_RANGE) f = "זווית השחרור גבוהה מדי - כוון יותר קדימה אל הסל.";
    else f = "זווית שחרור מצוינת! המשך כך.";
    if (lean > MAX_LEAN) f += "\nהגוף נוטה בזמן הזריקה - נסה לקפוץ ישר למעלה ולנחות באותה נקודה.";
    if (follow < MIN_FOLLOW) f += "\nהיד לא נשארה ישרה אחרי השחרור - סיים את התנועה עם יד מושטת אל הסל.";
    return f;
  }
  function f1(x) { return (Math.round(x * 10) / 10).toFixed(1); }
  function f3(x) { return (Math.round(x * 1000) / 1000).toFixed(3); }

  if (document.getElementById("angleSvg")) {
  // ---- angle lab ----
  var P = { A: { x: 110, y: 70 }, B: { x: 170, y: 160 }, C: { x: 250, y: 90 } };
  var svg = document.getElementById("angleSvg");
  function setLine(id, p, q) { var l = document.getElementById(id); l.setAttribute("x1", p.x); l.setAttribute("y1", p.y); l.setAttribute("x2", q.x); l.setAttribute("y2", q.y); }
  function drawAngle() {
    setLine("segBA", P.B, P.A); setLine("segBC", P.B, P.C);
    setLine("hRef", { x: P.B.x - 90, y: P.B.y }, { x: P.B.x + 90, y: P.B.y });
    ["A", "B", "C"].forEach(function (k) { document.getElementById("pt" + k).setAttribute("transform", "translate(" + P[k].x + "," + P[k].y + ")"); });
    var a = Math.atan2(P.A.y - P.B.y, P.A.x - P.B.x), c = Math.atan2(P.C.y - P.B.y, P.C.x - P.B.x);
    var ang = angleAtPoint(P.A.x, P.A.y, P.B.x, P.B.y, P.C.x, P.C.y);
    var rel = releaseAngle(P.B.x, P.B.y, P.C.x, P.C.y);
    document.getElementById("angleReadout").innerHTML =
      '<div class="k">angleAtPoint(A, B, C)</div><div class="big">' + f1(ang) + '°</div>' +
      '<div><span class="k">angleToA = atan2(' + Math.round(P.A.y - P.B.y) + ', ' + Math.round(P.A.x - P.B.x) + ') =</span> ' + f3(a) + ' rad (' + f1(toDeg(a)) + '°)</div>' +
      '<div><span class="k">angleToC = atan2(' + Math.round(P.C.y - P.B.y) + ', ' + Math.round(P.C.x - P.B.x) + ') =</span> ' + f3(c) + ' rad (' + f1(toDeg(c)) + '°)</div>' +
      '<div><span class="k">|toDegrees(A − C)| =</span> ' + f1(Math.abs(toDeg(a - c))) + '°' + (Math.abs(toDeg(a - c)) > 180 ? ' <span class="k">→ 360 − x</span>' : '') + '</div>' +
      '<div style="margin-top:10px" class="k">releaseAngle(B, C) = atan2(B.y − C.y, |C.x − B.x|)</div>' +
      '<div class="big" style="font-size:22px">' + f1(rel) + '°</div>';
  }
  var drag = null;
  function toSvg(e) {
    var pt = svg.createSVGPoint(); pt.x = e.clientX; pt.y = e.clientY;
    var p = pt.matrixTransform(svg.getScreenCTM().inverse());
    return { x: Math.max(14, Math.min(346, p.x)), y: Math.max(14, Math.min(250, p.y)) };
  }
  ["A", "B", "C"].forEach(function (k) {
    var g = document.getElementById("pt" + k);
    g.style.cursor = "grab";
    g.addEventListener("pointerdown", function (e) { drag = k; try { g.setPointerCapture(e.pointerId); } catch (_) {} e.preventDefault(); });
    g.addEventListener("pointermove", function (e) { if (drag === k) { P[k] = toSvg(e); drawAngle(); } });
    g.addEventListener("pointerup", function () { drag = null; });
    g.addEventListener("pointercancel", function () { drag = null; });
  });
  drawAngle();

  }

  if (document.getElementById("inRel")) {
  // ---- score lab ----
  var inRel = document.getElementById("inRel"), inLean = document.getElementById("inLean"),
      inFollow = document.getElementById("inFollow"), inNoHip = document.getElementById("inNoHip");
  function scoreColor(s) { return s >= 80 ? "var(--good)" : s >= 60 ? "var(--medium)" : "var(--bad)"; }
  function drawScore() {
    var rel = +inRel.value, follow = +inFollow.value, lean = inNoHip.checked ? NaN : +inLean.value;
    inLean.disabled = inNoHip.checked;
    document.getElementById("outRel").textContent = f1(rel) + "°";
    document.getElementById("outLean").textContent = inNoHip.checked ? "NaN" : f1(+inLean.value) + "°";
    document.getElementById("outFollow").textContent = follow + "°";
    var r = calcScore(rel), s = isNaN(lean) ? 100 : partScore(lean - MAX_LEAN), fo = partScore(MIN_FOLLOW - follow);
    var t = totalScore(rel, lean, follow);
    var tot = document.getElementById("scoreTotal");
    tot.textContent = t; tot.style.color = scoreColor(t);
    var bw = document.getElementById("scoreBarWrap"); bw.style.color = scoreColor(t);
    document.getElementById("scoreBar").style.width = t + "%";
    document.getElementById("scoreParts").innerHTML =
      'release   = 100 − |' + f1(rel) + ' − 48.5| × 2 = ' + r + '<br>' +
      'stability = ' + (isNaN(lean) ? '100 (NaN)' : '100 − max(0, ' + f1(lean) + ' − 10) × 2 = ' + s) + '<br>' +
      'follow    = 100 − max(0, 160 − ' + follow + ') × 2 = ' + fo + '<br>' +
      'total     = round(0.6×' + r + ' + 0.2×' + s + ' + 0.2×' + fo + ') = ' + t;
    document.getElementById("scoreFb").textContent = feedback(rel, lean, follow);
  }
  [inRel, inLean, inFollow, inNoHip].forEach(function (el) { el.addEventListener("input", drawScore); });
  drawScore();

  }

  // ---- release chart: real elbow angles from shot 2 of the test video (MediaPipe), same rule as ShotMathUtils.releaseIndex ----
  (function () {
    if (!document.getElementById("releaseSvg")) return;
    var T = [19050, 19083, 19116, 19149, 19182, 19215, 19248, 19281, 19314, 19347, 19380, 19413, 19446, 19479, 19512, 19545, 19578, 19611, 19644, 19677, 19710, 19743, 19776, 19809, 19842, 19875, 19908, 19941, 19974];
    var A = [0.8, 10.5, 1.7, 2.1, 1.4, 6.5, 6.2, 7.6, 3.1, 1.0, 2.5, 0.3, 4.2, 3.6, 6.7, 8.8, 0.5, 151.1, 165.0, 170.1, 175.2, 177.2, 176.9, 158.8, 154.1, 151.8, 159.2, 163.3, 158.7];
    var TRUE_MS = 19685, MARGIN = 8, n = A.length;
    function med3(a, b, c) { return Math.max(Math.min(a, b), Math.min(Math.max(a, b), c)); }
    var S = A.map(function (_, i) { var c = Math.max(1, Math.min(n - 2, i)); return med3(A[c - 1], A[c], A[c + 1]); });
    var max = Math.max.apply(null, S), thr = max - MARGIN, idx = n - 1;
    for (var i = 0; i < n; i++) { if (S[i] >= thr) { idx = i; break; } }
    var L = 64, R = 620, Tp = 20, B = 250, yMin = 0, yMax = 190, t0 = T[0], t1 = T[n - 1];
    function X(t) { return L + (R - L) * (t - t0) / (t1 - t0); }
    function Y(v) { return B - (B - Tp) * (v - yMin) / (yMax - yMin); }
    var s = '';
    [0, 45, 90, 135, 180].forEach(function (v) {
      s += '<line x1="' + L + '" x2="' + R + '" y1="' + Y(v) + '" y2="' + Y(v) + '" stroke="var(--rule)" stroke-dasharray="3 5"/>';
      s += '<text x="' + (L - 8) + '" y="' + (Y(v) + 4) + '" text-anchor="end" font-size="11" fill="var(--muted)" font-family="JetBrains Mono, monospace">' + v + '°</text>';
    });
    s += '<line x1="' + L + '" x2="' + R + '" y1="' + Y(thr) + '" y2="' + Y(thr) + '" stroke="var(--accent)" stroke-width="1.5"/>';
    s += '<text x="' + L + '" y="' + (Y(thr) - 6) + '" font-size="11.5" fill="var(--accent)">max − 8° = ' + thr.toFixed(1) + '°</text>';
    s += '<line x1="' + X(TRUE_MS) + '" x2="' + X(TRUE_MS) + '" y1="' + Tp + '" y2="' + B + '" stroke="var(--brass)" stroke-width="2" stroke-dasharray="6 4"/>';
    s += '<text x="' + (X(TRUE_MS) + 6) + '" y="' + (Y(60)) + '" font-size="12" font-weight="700" fill="var(--brass)">' + 'true release ' + (TRUE_MS / 1000).toFixed(3) + 's</text>';
    [19100, 19300, 19500, 19700, 19900].forEach(function (t) {
      s += '<text x="' + X(t) + '" y="' + (B + 18) + '" text-anchor="middle" font-size="11" fill="var(--muted)" font-family="JetBrains Mono, monospace">' + (t / 1000).toFixed(1) + 's</text>';
    });
    s += '<polyline fill="none" stroke="var(--ink)" stroke-width="2" points="' + S.map(function (v, i) { return X(T[i]) + "," + Y(v); }).join(" ") + '"/>';
    A.forEach(function (v, i) { s += '<circle cx="' + X(T[i]) + '" cy="' + Y(v) + '" r="3.5" fill="var(--paper)" stroke="var(--muted)" stroke-width="1.5"/>'; });
    s += '<circle cx="' + X(T[idx]) + '" cy="' + Y(S[idx]) + '" r="8" fill="var(--accent)"/>';
    s += '<text x="' + (X(T[idx]) - 12) + '" y="' + (Y(S[idx]) + 4) + '" text-anchor="end" font-size="12.5" font-weight="700" fill="var(--accent)">releaseIndex</text>';
    document.getElementById("releaseSvg").innerHTML = s;
    document.getElementById("releaseReadout").innerHTML =
      '<span class="k">max (median-of-3) =</span> ' + max.toFixed(1) + '°  <span class="k">threshold =</span> ' + thr.toFixed(1) + '°<br>' +
      '<span class="k">chosen sample:</span> ' + (T[idx] / 1000).toFixed(3) + 's (' + A[idx] + '°) → frame 19.685s  ·  <span class="k">true release:</span> 19.685s';
  })();

  if (document.getElementById("quizTopic")) {
  // ---- quiz ----
  var KEY = "bsa-guide-known";
  var known = {};
  try { known = JSON.parse(localStorage.getItem(KEY) || "{}") || {}; } catch (_) { known = {}; }
  function saveKnown() { try { localStorage.setItem(KEY, JSON.stringify(known)); } catch (_) {} }
  var sel = document.getElementById("quizTopic");
  var opts = '<option value="all">כל הנושאים</option><option value="todo">שאלות שעוד לא סימנתי</option>';
  Object.keys(TOPICS).forEach(function (k) { opts += '<option value="' + k + '">' + esc(TOPICS[k]) + '</option>'; });
  sel.innerHTML = opts;
  var order = Q.map(function (_, i) { return i; }), pos = 0, list = [];
  function rebuild() {
    var v = sel.value;
    list = order.filter(function (i) { return v === "all" ? true : v === "todo" ? !known[i] : Q[i][0] === v; });
    pos = 0; showQ();
  }
  function showQ() {
    var a = document.getElementById("quizA"), chk = document.getElementById("quizKnown");
    a.hidden = true; document.getElementById("quizShow").textContent = "הצג תשובה";
    if (!list.length) {
      document.getElementById("quizTag").textContent = "";
      document.getElementById("quizQ").textContent = "אין כאן שאלות שנשארו. בחר נושא אחר או בטל סימונים.";
      document.getElementById("quizCount").textContent = "";
      chk.disabled = true; chk.checked = false; return;
    }
    var i = list[pos];
    chk.disabled = false; chk.checked = !!known[i];
    document.getElementById("quizTag").textContent = TOPICS[Q[i][0]];
    document.getElementById("quizQ").textContent = Q[i][1];
    a.innerHTML = "<p>" + Q[i][2] + "</p>";
    var done = Object.keys(known).filter(function (k) { return known[k]; }).length;
    document.getElementById("quizCount").textContent = "שאלה " + (pos + 1) + " מתוך " + list.length + " · סימנת " + done + " מתוך " + Q.length;
  }
  document.getElementById("quizShow").addEventListener("click", function () {
    var a = document.getElementById("quizA"); a.hidden = !a.hidden;
    this.textContent = a.hidden ? "הצג תשובה" : "הסתר תשובה";
  });
  document.getElementById("quizNext").addEventListener("click", function () { if (list.length) { pos = (pos + 1) % list.length; showQ(); } });
  document.getElementById("quizShuffle").addEventListener("click", function () {
    for (var i = order.length - 1; i > 0; i--) { var j = Math.floor(Math.random() * (i + 1)); var t = order[i]; order[i] = order[j]; order[j] = t; }
    rebuild();
  });
  document.getElementById("quizKnown").addEventListener("change", function () {
    if (!list.length) return;
    known[list[pos]] = this.checked; saveKnown(); showQ();
  });
  sel.addEventListener("change", rebuild);
  rebuild();

  }

})();
