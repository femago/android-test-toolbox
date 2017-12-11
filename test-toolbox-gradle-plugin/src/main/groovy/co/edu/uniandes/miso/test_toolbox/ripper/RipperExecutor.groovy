package co.edu.uniandes.miso.test_toolbox.ripper

import com.android.annotations.NonNull
import com.android.builder.testing.ConnectedDevice
import com.android.ddmlib.CollectingOutputReceiver
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.util.concurrent.TimeUnit

/*
 * @(#)RipperExecutor.java
 *
 */

class RipperExecutor {

    private static final Logger LOGGER = Logging.getLogger(RipperExecutor.class);

    private static final String command =
            "am instrument -w -r -e debug false co.edu.uniandes.miso.test_toolbox.ripper.test/android.support.test.runner.AndroidJUnitRunner"

    private ConnectedDevice device

    private String variant
    private String packageForRipper

    private RipperPluginExtension extensions

    RipperExecutor(
            @NonNull String variant,
            @NonNull ConnectedDevice device,
            @NonNull String packageForRipper, @NonNull RipperPluginExtension extensions) {
        this.device = device;
        this.variant = variant;
        this.packageForRipper = packageForRipper
        this.extensions = extensions
    }

    def run() {
        CollectingOutputReceiver receiver = new CollectingOutputReceiver()

        LOGGER.lifecycle("$device.name ($device.serialNumber) <-- Command: " + command)

        device.executeShellCommand(command, receiver, extensions.timeOut, TimeUnit.SECONDS)
        String monkeyOutput = "$device.name ($device.serialNumber)\n" + receiver.output
        LOGGER.lifecycle(monkeyOutput)



//        File reportFile = new File(reportFileDirectory, "monkey${variantName.capitalize()}-${device.name.replaceAll("\\s", "_")}-${device.serialNumber}.txt")
//        def reportsDir = reportFile.getParentFile()
//        if (!reportsDir.exists() && !reportsDir.mkdirs()) {
//            throw new GradleException("Could not create reports directory: " + reportsDir.getAbsolutePath())
//        }
//        reportFile.write(monkeyOutput, "UTF-8")
    }
}
