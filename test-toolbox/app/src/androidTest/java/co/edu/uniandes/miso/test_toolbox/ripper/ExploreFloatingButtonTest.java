package co.edu.uniandes.miso.test_toolbox.ripper;

import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getArguments;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static co.edu.uniandes.miso.test_toolbox.ripper.CommonRipper.goToMainActivity;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExploreFloatingButtonTest {
    private static final int LAUNCH_TIMEOUT = 5000;

    private UiDevice mDevice;
    private CommonRipper.ExecArguments execArguments;

    @Before
    public void setUp() throws Exception {
        Instrumentation instrumentation = getInstrumentation();
        execArguments = CommonRipper.extractArguments(getArguments());

        mDevice = UiDevice.getInstance(getInstrumentation());
        startMainActivityFromHomeScreen();
    }

    public void startMainActivityFromHomeScreen() {
        goToMainActivity(execArguments);
        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(execArguments.targetAndroidPackage).depth(0)),
                LAUNCH_TIMEOUT);
    }


    @Test
    public void clickFloatingButton() {

    }

    private String tag() {
        return this.getClass().getName();
    }


}
