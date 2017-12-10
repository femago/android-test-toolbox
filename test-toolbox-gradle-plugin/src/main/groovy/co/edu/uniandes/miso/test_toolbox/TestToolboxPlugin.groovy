package co.edu.uniandes.miso.test_toolbox

import co.edu.uniandes.miso.test_toolbox.ripper.RipperPluginExtension
import co.edu.uniandes.miso.test_toolbox.ripper.RipperTestTask
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

/*
 * @(#)TestToolboxPlugin.java
 *
 */

class TestToolboxPlugin implements Plugin<Project> {

    public static final String TASKS_GROUP = "test toolbox"

    @Override
    void apply(Project project) {
        applyExtensions(project)
        applyTasks(project)
    }

    void applyExtensions(Project project) {
        project.extensions.create('ripper', RipperPluginExtension, project)
    }

    void applyTasks(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin)) {
            throw new IllegalStateException("gradle-android-plugin not found")
        }
        ripperTasks(project)
        extraTasks(project)

    }

    void extraTasks(Project project) {
        def showDevicesTask = project.tasks.create("showDevices") << {
            def adbExe = project.android.getAdbExe().toString()
            println "${adbExe} devices".execute().text
        }
        showDevicesTask.group = TASKS_GROUP
        showDevicesTask.description = "Runs adb devices command"
    }

    void ripperTasks(Project project) {
        AppExtension android = project.extensions.getByType(AppExtension)
        android.applicationVariants.all { ApplicationVariant variant ->
            RipperTestTask task = project.tasks.create("ripper${variant.name.capitalize()}", RipperTestTask)
            task.group = TASKS_GROUP
            task.description = "Run a ripper rutine against the ${variant.name.capitalize()} variant on the first connected device"
            task.variantName = variant.name
            task.outputs.upToDateWhen { false }

            if (project.extensions.getByType(RipperPluginExtension).install) {
                task.dependsOn(variant.assemble)
                variant.outputs.each { output ->
                    task.apkFile = output.outputFile
                }
            }
        }
    }
}
