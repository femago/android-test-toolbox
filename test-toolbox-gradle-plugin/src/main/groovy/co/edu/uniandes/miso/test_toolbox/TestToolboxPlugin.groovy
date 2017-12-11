package co.edu.uniandes.miso.test_toolbox

import co.edu.uniandes.miso.test_toolbox.ripper.RipperTestConfig
import co.edu.uniandes.miso.test_toolbox.ripper.RipperPluginExtension
import co.edu.uniandes.miso.test_toolbox.ripper.RipperTestTask
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.Paths

/*
 * @(#)TestToolboxPlugin.java
 *
 */

class TestToolboxPlugin implements Plugin<Project> {

    public static final String TASKS_GROUP = "test toolbox"

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin)) {
            throw new IllegalStateException("gradle-android-plugin not found")
        }
        new RipperTestConfig(project).init()
        extraTasks(project)

    }

    void extraTasks(Project project) {
        def showDevicesTask = project.tasks.create("showDevices").doLast {
            def adbExe = project.android.getAdbExe().toString()
            println "${adbExe} devices".execute().text
        }
        showDevicesTask.group = TASKS_GROUP
        showDevicesTask.description = "Runs adb devices command"
    }

}
