package co.edu.uniandes.miso.test_toolbox.ripper

import com.android.annotations.NonNull
import com.android.build.gradle.AppExtension
import com.android.builder.testing.ConnectedDevice
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.utils.StdLogger
import com.google.common.collect.Lists
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class RipperTestTask extends DefaultTask {

    @InputFile
    @Optional
    File apkFile

    String variantName

    AppExtension android
    RipperPluginExtension ripper

    StdLogger stdLogger

    List<ConnectedDevice> selectedDevices
    String targetPackageName

    @TaskAction
    def runRipperTest(){

        android = project.extensions.getByType(AppExtension)
        ripper = project.extensions.getByType(RipperPluginExtension)

        targetPackageName = packageName()
        logger.lifecycle("Starting ripper task $variantName for package $targetPackageName")

        stdLogger = new StdLogger(StdLogger.Level.VERBOSE)

        collectDevices()
    }

    void collectDevices() {
        ConnectedDeviceProvider cdp = new ConnectedDeviceProvider(android.getAdbExecutable(), ripper.connectTimeoutMs, stdLogger)
        cdp.init()

        Collection<String> excludedDevices = ripper.excludedDevices

        //ArrayList<MonkeyResult> results = new ArrayList<>()
        selectedDevices = Lists.newArrayList()

        cdp.devices.each {
            ConnectedDevice device = it as ConnectedDevice
            if (!excludedDevices.contains(device.getSerialNumber())) {
                logger.lifecycle("Use device: $device.name")
                uninstallApkFromDevice(device, this.targetPackageName)
                selectedDevices.add(device)
            }
            else {
                logger.lifecycle("Skip device: $device.name")
            }
        }

        if (selectedDevices.empty) {
            throw new GradleException("No devices found")
        }
    }

    def uninstallApkFromDevice(@NonNull ConnectedDevice device, @NonNull String packageName) {
        if (apkFile != null) {
            logger.lifecycle("Uninstall APK $packageName from device $device.name")
            device.uninstallPackage(packageName, 30000, stdLogger)
            logger.lifecycle("Install APK $packageName into device $device.name")
            device.installPackage(apkFile, new ArrayList<String>(), 30000, stdLogger)
            logger.lifecycle("Uninstall/Install APK from $device.name ($device.serialNumber) done.")
        }
    }

    private String packageName() {
        def matchingVariants = android.applicationVariants.matching { var -> var.name == variantName }

        if (matchingVariants.isEmpty()) {
            throw new GradleException("Could not find the '$variantName' variant")
        }

        return matchingVariants.first().getGenerateBuildConfig().getAppPackageName()

    }
}
