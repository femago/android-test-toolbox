/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AutoScout24 GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package co.edu.uniandes.miso.test_toolbox.monkey

import com.android.annotations.NonNull
import com.android.build.gradle.AppExtension
import com.android.builder.testing.ConnectedDevice
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.ddmlib.CollectingOutputReceiver
import com.android.utils.StdLogger
import com.google.common.collect.Lists
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class MonkeyTestTask extends DefaultTask {

    File reportFileDirectory

    @InputFile
    @Optional
    File apkFile

    String variantName

    AppExtension android
    MonkeyPluginExtension monkey

    StdLogger stdLogger

    @TaskAction
    def runMonkeyTest() throws IOException {

        android = project.extensions.getByType(AppExtension)
        monkey = project.extensions.getByType(MonkeyPluginExtension)

        String packageName = getPackageName()
        logger.info("Running tests for package: " + packageName)

        stdLogger = new StdLogger(StdLogger.Level.VERBOSE)


        ConnectedDeviceProvider cdp = new ConnectedDeviceProvider(android.getAdbExecutable(), monkey.connectTimeoutMs, stdLogger)
        cdp.init()

        Collection<String> excludedDevices = monkey.excludedDevices

        ArrayList<MonkeyResult> results = new ArrayList<>()
        List<ConnectedDevice> devices = Lists.newArrayList()

        cdp.devices.each {
            ConnectedDevice device = it as ConnectedDevice
            if (!excludedDevices.contains(device.getSerialNumber())) {
                logger.info("Use device: " + device.name)
                uninstallApkFromDevice(device, packageName)
                devices.add(device)
            }
            else {
                logger.info("Skip device: " + device.name)
            }
        }

        if (devices.empty) {
            throw new GradleException("No devices found")
        }

        def runTestOnDeviceClosure = { device ->

            CollectingOutputReceiver receiver = new CollectingOutputReceiver()
            String monkeyCommand = "monkey"
            if (monkey.delay > 0) {
                monkeyCommand += String.format(" --throttle %d", monkey.delay)
            }
            monkeyCommand += String.format(" -p %s -s %d -vv %d ", packageName, monkey.seed, monkey.eventCount)

            logger.info("Monkey command: " + monkeyCommand)

            device.executeShellCommand(monkeyCommand, receiver, monkey.timeOut, TimeUnit.SECONDS)
            String monkeyOutput = receiver.output
            MonkeyResult result = parseMonkeyOutput(monkeyOutput)
            results.add(result)

            if (logger.isDebugEnabled()) {
                println monkeyOutput
            }

            File reportFile = new File(reportFileDirectory, "monkey${variantName.capitalize()}-${device.name.replaceAll("\\s","_")}-${device.serialNumber}.txt")
            def reportsDir = reportFile.getParentFile()
            if (!reportsDir.exists() && !reportsDir.mkdirs()) {
                throw new GradleException("Could not create reports directory: " + reportsDir.getAbsolutePath())
            }
            reportFile.write(monkeyOutput, "UTF-8")
        }

        def threadPool = Executors.newFixedThreadPool(devices.size())

        try {
            List<Future> futures = devices.collect { device ->
                threadPool.submit({ ->
                    ConnectedDevice runningDevice = device as ConnectedDevice
                    runTestOnDeviceClosure runningDevice
                } as Callable);
            }
            futures.each {
                try {
                    it.get()
                }
                catch (ExecutionException e) {
                    logger.error("Error while running tests: " + e.toString())
                    MonkeyResult result = new MonkeyResult(MonkeyResult.ResultStatus.Crash, monkey.eventCount, 0)
                    results.add(result)
                }
            }
        } finally {
            threadPool.shutdown()
        }

        Boolean success = true
        int eventsCompleted = 0
        int totalEventCount = 0
        results.each {
            if (!it.status.isSuccess) {
                success = false
            }
            eventsCompleted += it.eventsCompleted
            totalEventCount += it.totalEventCount
        }

        if (results.size() == 0) {
            success = false
        }

        if (!success && monkey.failOnFailure) {
            System.exit(1)
        }
    }

    // Adapted from https://github.com/jenkinsci/android-emulator-plugin/blob/master/src/main/java/hudson/plugins/android_emulator/monkey/MonkeyBuilder.java
    def parseMonkeyOutput(String monkeyOutput) {
        // No input, no output
        if (monkeyOutput == null) {
            return new MonkeyResult(MonkeyResult.ResultStatus.NothingToParse, 0, 0);
        }

        // If we don't recognise any outcomes, then say so
        MonkeyResult.ResultStatus status = MonkeyResult.ResultStatus.UnrecognisedFormat;

        // Extract common data
        int totalEventCount = 0;
        Matcher matcher = Pattern.compile(":Monkey: seed=-?\\d+ count=(\\d+)").matcher(monkeyOutput);
        if (matcher.find()) {
            totalEventCount = Integer.parseInt(matcher.group(1));
        }

        // Determine outcome
        int eventsCompleted = 0;
        if (monkeyOutput.contains("// Monkey finished")) {
            status = MonkeyResult.ResultStatus.Success;
            eventsCompleted = totalEventCount;
        } else {
            // If it didn't finish, assume failure
            matcher = Pattern.compile("Events injected: (\\d+)").matcher(monkeyOutput);
            if (matcher.find()) {
                eventsCompleted = Integer.parseInt(matcher.group(1));
            }

            // Determine failure type
            matcher = Pattern.compile("// (CRASH|NOT RESPONDING)").matcher(monkeyOutput);
            if (matcher.find()) {
                String reason = matcher.group(1);
                if ("CRASH" == reason) {
                    status = MonkeyResult.ResultStatus.Crash;
                } else if ("NOT RESPONDING" == reason) {
                    status = MonkeyResult.ResultStatus.AppNotResponding;
                }
            }
        }

        return new MonkeyResult(status, totalEventCount, eventsCompleted)
    }

    def uninstallApkFromDevice(@NonNull ConnectedDevice device, @NonNull String packageName) {
        if (apkFile != null) {
            logger.lifecycle("Uninstall APK (" + device.name + ")")

            device.uninstallPackage(packageName, 30000, stdLogger)
            logger.lifecycle("Install APK (" + device.name + ")")
            device.installPackage(apkFile, new ArrayList<String>(), 30000, stdLogger)

            logger.lifecycle("Uninstall/Install APK (" + device.name + ") done.")

        }
    }

    private String getPackageName() {
        def matchingVariants = android.applicationVariants.matching { var -> var.name == variantName }

        if (matchingVariants.isEmpty()) {
            throw new GradleException("Could not find the '" + variantName + "' variant")
        }

        return matchingVariants.first().getGenerateBuildConfig().getAppPackageName()

    }
}
