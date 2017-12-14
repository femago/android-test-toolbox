package co.edu.uniandes.miso.test_toolbox.ripper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.uiautomator.UiDevice;

import java.io.IOException;

import static android.support.test.InstrumentationRegistry.getContext;

/**
 * Created by felipe.martinez on 13/12/2017.
 */

public class CommonRipper {

    public static final ExecArguments extractArguments(Bundle arguments) {
        ExecArguments execArguments = new ExecArguments();
        execArguments.targetAndroidPackage = arguments.getString("targetAndroidPackage");
        return execArguments;
    }

    public static class ExecArguments {
        String targetAndroidPackage;
    }

    public static final void dumpDevice(UiDevice mDevice) {
        try {
            mDevice.dumpWindowHierarchy(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final void sleep(int i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void goToMainActivity(ExecArguments args) {
        // Launch the app
        Context context = getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(args.targetAndroidPackage);
        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
