package co.edu.uniandes.miso.test_toolbox.ripper

import co.edu.uniandes.miso.test_toolbox.report.TestExecutionProcessor
import co.edu.uniandes.miso.test_toolbox.report.TestSuite
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
            "am instrument -w -r " +
                    "-e debug false " +
                    "-e targetAndroidPackage ###targetAndroidPackage " +
                    " co.edu.uniandes.miso.test_toolbox.ripper.test/android.support.test.runner.AndroidJUnitRunner"

    private ConnectedDevice device

    private String variant

    private RipperPluginExtension extensions

    private File testResultDirectory

    RipperExecutor(
            @NonNull String variant,
            @NonNull ConnectedDevice device,
            @NonNull RipperPluginExtension extensions,
            @NonNull File testResultDirectory) {
        this.device = device;
        this.variant = variant;
        this.extensions = extensions
        this.testResultDirectory = testResultDirectory
    }

    def run() {
        CollectingOutputReceiver receiver = new CollectingOutputReceiver()

        def replacedCommand = command.replace("###targetAndroidPackage", extensions.targetPackageName)
        LOGGER.lifecycle("$device.name ($device.serialNumber) <-- Command: " + replacedCommand)

        device.executeShellCommand(replacedCommand, receiver, extensions.timeOut, TimeUnit.SECONDS)
        def output = receiver.output
        LOGGER.lifecycle("$device.name ($device.serialNumber)")
        LOGGER.lifecycle(output)

        Collection<TestSuite> parsed = new TestExecutionProcessor(output).parse()

        for (TestSuite suite : parsed) {
            File reportFile = new File(testResultDirectory, "TEST-${device.name.replaceAll("\\s", "_")}-${suite.name}.xml")

            suite.hostname = device.name + "-" + device.serialNumber
            suite.name = suite.name.reverse().replaceFirst("\\.", ".${device.serialNumber}.".reverse()).reverse()

            suite.cases.forEach({it.classname=suite.name} )

            def reportsDir = reportFile.getParentFile()
            if (!reportsDir.exists() && !reportsDir.mkdirs()) {
                throw new GradleException("Could not create reports directory: " + reportsDir.getAbsolutePath())
            }
            reportFile.write(suite.toXml(), "UTF-8")
        }
    }
}
