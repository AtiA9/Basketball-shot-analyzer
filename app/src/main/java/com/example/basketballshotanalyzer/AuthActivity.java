// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshotanalyzer.db.DatabaseHelper;
import com.example.basketballshotanalyzer.model.User;
import com.example.basketballshotanalyzer.util.AuthUtils;
import com.example.basketballshotanalyzer.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * מסך פתיחה: התחברות. ההרשמה נמצאת במסך נפרד (RegisterActivity).
 * אם המשתמש סימן בעבר "הישאר מחובר" - עוברים ישר למסך הראשי.
 */
public class AuthActivity extends AppCompatActivity {

    private TextInputLayout layoutUsername;
    private TextInputLayout layoutPassword;
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private CheckBox checkRemember;

    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (session.shouldAutoLogin()) {
            openMainScreen();
            return;
        }
        setContentView(R.layout.activity_auth);
        dbHelper = new DatabaseHelper(this);
        layoutUsername = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.layoutPassword);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        checkRemember = findViewById(R.id.checkRemember);
        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
        findViewById(R.id.btnGoToRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    /** בודק את הקלט ומציג הודעת שגיאה מתחת לשדה הבעייתי. מחזיר true אם הכול תקין. */
    private boolean validateInput(String username, String password) {
        boolean usernameOk = AuthUtils.isValidUsername(username);
        boolean passwordOk = AuthUtils.isValidPassword(password);
        layoutUsername.setError(usernameOk ? null : getString(R.string.error_username));
        layoutPassword.setError(passwordOk ? null : getString(R.string.error_password));
        return usernameOk && passwordOk;
    }

    /** התחברות: מחפש את המשתמש ובודק שה-hash של הסיסמה שהוקלדה תואם לשמור. */
    private void login() {
        String username = getText(editUsername);
        String password = getText(editPassword);
        if (!validateInput(username, password)) {
            return;
        }
        User user = dbHelper.getUserByUsername(username);
        if (user == null || !AuthUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
            layoutPassword.setError(getString(R.string.error_wrong_credentials));
            return;
        }
        completeLogin(user.getId(), username);
    }

    /** שומר את ההתחברות ב-SharedPreferences ועובר למסך הראשי. */
    private void completeLogin(long userId, String username) {
        session.login(userId, username, checkRemember.isChecked());
        openMainScreen();
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /** מחזיר את הטקסט שבשדה בלי רווחים מיותרים בהתחלה ובסוף. */
    private String getText(TextInputEditText edit) {
        return edit.getText() == null ? "" : edit.getText().toString().trim();
    }
}
