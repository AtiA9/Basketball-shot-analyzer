// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshotanalyzer.db.DatabaseHelper;
import com.example.basketballshotanalyzer.model.User;
import com.example.basketballshotanalyzer.util.AuthUtils;
import com.example.basketballshotanalyzer.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * מסך הרשמה: יצירת משתמש חדש.
 * בודק את הקלט, מוודא ששתי הסיסמאות זהות, שומר את המשתמש עם hash + salt ומחבר אותו.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout layoutUsername;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private TextInputEditText editConfirmPassword;
    private CheckBox checkRemember;

    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);
        layoutUsername = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        checkRemember = findViewById(R.id.checkRemember);
        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> register());
        // חזרה למסך ההתחברות: פשוט סוגרים את המסך הזה, ומסך ההתחברות שמתחתיו חוזר
        findViewById(R.id.btnGoToLogin).setOnClickListener(v -> finish());
    }

    /** בודק את כל השדות ומציג שגיאה מתחת לכל שדה בעייתי. מחזיר true אם הכול תקין. */
    private boolean validateInput(String username, String password, String confirm) {
        boolean usernameOk = AuthUtils.isValidUsername(username);
        boolean passwordOk = AuthUtils.isValidPassword(password);
        boolean matchOk = password.equals(confirm);
        layoutUsername.setError(usernameOk ? null : getString(R.string.error_username));
        layoutPassword.setError(passwordOk ? null : getString(R.string.error_password));
        layoutConfirmPassword.setError(matchOk ? null : getString(R.string.error_password_mismatch));
        return usernameOk && passwordOk && matchOk;
    }

    /** הרשמה: יוצר salt ו-hash, שומר משתמש חדש ומחבר אותו. */
    private void register() {
        String username = getText(editUsername);
        String password = getText(editPassword);
        String confirm = getText(editConfirmPassword);
        if (!validateInput(username, password, confirm)) {
            return;
        }
        String salt = AuthUtils.generateSalt();
        String hash = AuthUtils.hashPassword(password, salt);
        long id = dbHelper.insertUser(new User(username, hash, salt));
        if (id == -1) {
            layoutUsername.setError(getString(R.string.error_user_exists));
            return;
        }
        Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
        session.login(id, username, checkRemember.isChecked());
        openMainScreen();
    }

    /**
     * עובר למסך הראשי ומנקה את כל המסכים הקודמים (גם את מסך ההתחברות),
     * כדי שלחיצה על "חזרה" לא תחזיר להרשמה.
     */
    private void openMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** מחזיר את הטקסט שבשדה בלי רווחים מיותרים בהתחלה ובסוף. */
    private String getText(TextInputEditText edit) {
        return edit.getText() == null ? "" : edit.getText().toString().trim();
    }
}
