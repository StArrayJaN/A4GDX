package starray.adofai.libgdx.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import starray.adofai.Level;
import starray.adofai.libgdx.R;
import starray.adofai.libgdx.Tools;

import java.io.IOException;

/**
 * Launches the Android application.
 */
public class AndroidLauncher extends AppCompatActivity {
    EditText editText;
    ToggleButton showBPM;
    ToggleButton dynamicCameraSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setSupportActionBar(findViewById(R.id.toolbar));
        editText = findViewById(R.id.filePath);
        Button button = findViewById(R.id.run);
        Button selectFile = findViewById(R.id.selectFile);
        showBPM = findViewById(R.id.showBPM);
        showBPM.setVisibility(View.GONE);
        dynamicCameraSpeed = findViewById(R.id.dynamicCameraSpeed);
        dynamicCameraSpeed.setVisibility(View.GONE);
        SharedDataManager sharedDataManager = SharedDataManager.getInstance(this);
        editText.setText(sharedDataManager.getData("lastPath",""));
        showBPM.setChecked(sharedDataManager.getBoolData("showBPM", false));
        dynamicCameraSpeed.setChecked(sharedDataManager.getBoolData("dynamicCameraSpeed", false));
        selectFile.setOnClickListener((arg0) -> {
            FileSelector.setActivity(this);
            FileSelector.selectFile("*/*");
        });
        button.setOnClickListener((arg0) -> {
            EditText editText = findViewById(R.id.filePath);
            if (!TextUtils.isEmpty(editText.getText())) {
                start(editText.getText().toString());
            }
        });
        if (Permission.checkPermission(this)) {
            try {
                Runtime.getRuntime().exec("logcat -f /sdcard/A4GDX.txt");
            } catch (Exception e) {
                log(e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FileSelector.REQUEST_CODE && resultCode == RESULT_OK) {
            String path = FileSelector.getPath(data.getData());
            if (path != null && !path.endsWith(".adofai")) {
                log(new IOException("不支持的格式：" + path.substring(path.lastIndexOf("."))));
            }
            editText.setText(FileSelector.getPath(data.getData()));
        }
    }

    public void start(String path) {
        try {
            Level.readLevelFile(path);
        } catch (Exception e) {
            log(e);
            return;
        }
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("path", path);
        startActivity(intent);
    }

    private void log(Throwable e){
        Log.e("ADOFAI", Tools.getStackTrace(e));
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this)
            .setMessage(Tools.getStackTrace(e))
            .setTitle("发生错误！")
            .setPositiveButton("确定并复制", (arg0, arg1) -> {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("error", Tools.getStackTrace(e));
                clipboardManager.setPrimaryClip(clipData);
            })
            .create();
        alertDialog.show();
    }
}
