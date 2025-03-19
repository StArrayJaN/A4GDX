package starray.adofai.libgdx.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.io.IOException;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import starray.adofai.Level;
import starray.adofai.LevelUtils;
import starray.adofai.libgdx.ADOFAI;
import starray.adofai.libgdx.Tools;

public class GameActivity extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra("path");
        try {
            AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
            configuration.useImmersiveMode = true; // Recommended, but not required.
            initialize(new ADOFAI(path,false,false), configuration);
        } catch (Exception e) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
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
}
