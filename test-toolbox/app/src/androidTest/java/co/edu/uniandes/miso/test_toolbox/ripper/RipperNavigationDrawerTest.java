package co.edu.uniandes.miso.test_toolbox.ripper;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getArguments;
import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RipperNavigationDrawerTest {
    private static final String BASIC_SAMPLE_PACKAGE
            = "info.frangor.laicare";
    private static final int LAUNCH_TIMEOUT = 5000;

    private UiDevice mDevice;

    @Before
    public void setUp() throws Exception {
        Instrumentation instrumentation = getInstrumentation();
        Bundle arguments = getArguments();
        Log.i(tag(), "Bundle Arguments: " + arguments);
        mDevice = UiDevice.getInstance(getInstrumentation());
        startMainActivityFromHomeScreen();
    }

    public void startMainActivityFromHomeScreen() {
        goToMainActivity();
        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT);
    }

    private void goToMainActivity() {
        // Launch the app
        Context context = getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Test
    public void shouldOpenDrawer() {

    }

    @Test
    public void shouldNotFailOnDrawerItemClick() {
        throw new ActivityNotFoundException();
    }

    private String tag() {
        return this.getClass().getName();
    }


}
