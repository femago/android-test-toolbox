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

import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future

class RipperTestTask extends DefaultTask {

    File reportFileDirectory

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
    def runRipperTest() {

        android = project.extensions.getByType(AppExtension)
        ripper = project.extensions.getByType(RipperPluginExtension)

        targetPackageName = packageName()
        logger.lifecycle("Starting ripper task $variantName for package $targetPackageName")

        stdLogger = new StdLogger(StdLogger.Level.VERBOSE)

        collectDevices()
        runRipperInDevices()
    }

    void runRipperInDevices() {
        def threadPool = Executors.newFixedThreadPool(selectedDevices.size())
        try {
            List<Future> futures = selectedDevices.collect { device ->
                threadPool.submit({ ->
                    new RipperExecutor(variantName, device, targetPackageName, ripper).run();
                } as Callable);
            }
            futures.each {
                try {
                    it.get()
                } catch (ExecutionException e) {
                    logger.error("Error while running tests: " + e.toString())
                    //MonkeyResult result = new MonkeyResult(MonkeyResult.ResultStatus.Crash, monkey.eventCount, 0)
                    //results.add(result)
                }
            }
        } finally {
            threadPool.shutdown()
        }
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
                installApksIntoDevice(device, this.targetPackageName)
                selectedDevices.add(device)
            } else {
                logger.lifecycle("Skip device: $device.name")
            }
        }

        if (selectedDevices.empty) {
            throw new GradleException("No devices found")
        }
    }

    def installApksIntoDevice(@NonNull ConnectedDevice device, @NonNull String packageName) {

        RipperTestConfig.RIPPER_APK.each {
            logger.lifecycle("Installing ripper instrumentation $it into device $device.name")
            File ripperApk = Paths.get(project.getBuildDir().getPath(), "ripper-dist", it).toFile()
            device.installPackage(ripperApk, Collections.emptyList(), 30000, stdLogger)
        }

        if (apkFile != null) {
            logger.lifecycle("Uninstalling APK $packageName from device $device.name")
            device.uninstallPackage(packageName, 30000, stdLogger)
            logger.lifecycle("Installing APK $packageName into device $device.name")
            device.installPackage(apkFile, new ArrayList<String>(), 30000, stdLogger)
            logger.lifecycle("Uninstall/Install APK from $device.name ($device.serialNumber) done")
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
