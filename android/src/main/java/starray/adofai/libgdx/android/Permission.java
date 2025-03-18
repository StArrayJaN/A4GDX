package starray.adofai.libgdx.android;

//
// Decompiled by Jadx - 676ms
//
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

public class Permission {
    public static final int REQUEST_CODE = 5;
    private static final String[] permission = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isPermissionGranted(Activity activity) {
        int i = 0;
        while (true) {
            String[] strArr = permission;
            if (i < strArr.length) {
                int checkPermission = activity.checkSelfPermission(strArr[i]);
                if (checkPermission == 0) {
                    i++;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkPermission(Activity activity) {
        if (isPermissionGranted(activity)) {
            return true;
        }
        activity.requestPermissions(permission, 5);
        return false;
    }
}

