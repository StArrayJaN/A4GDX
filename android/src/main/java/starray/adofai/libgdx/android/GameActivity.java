package starray.adofai.libgdx.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import starray.adofai.Level;
import starray.adofai.libgdx.ADOFAI;
import starray.adofai.libgdx.Tools;

public class GameActivity extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedDataManager sharedDataManager = SharedDataManager.getInstance(this);
        String path = getIntent().getStringExtra("path");
        try {
            AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
            configuration.useImmersiveMode = true; // Recommended, but not required.
            sharedDataManager.putData("lastPath", path);
            sharedDataManager.putBoolData("dynamicCameraSpeed",getIntent().getBooleanExtra("dynamicCameraSpeed",false));
            sharedDataManager.putBoolData("showBPM", getIntent().getBooleanExtra("showBPM",false));
            initialize(new ADOFAI(Level.readLevelFile(path), false), configuration);
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
