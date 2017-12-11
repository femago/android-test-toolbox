package co.edu.uniandes.miso.test_toolbox.ripper

import co.edu.uniandes.miso.test_toolbox.TestToolboxPlugin
import co.edu.uniandes.miso.test_toolbox.ToolboxTestType
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.nio.file.Paths

class RipperTestConfig extends ToolboxTestType {

    public static final RIPPER_APK = ["ripper-main.apk", "ripper-test.apk"]

    RipperTestConfig(Project project) {
        super(project)
    }

    @Override
    protected void initExtensions() {
        project.extensions.create('ripper', RipperPluginExtension, project)
    }

    @Override
    protected void initTasks() {
        project.logger.lifecycle('Start configuring ripper tasks')
        copyRipperApks()

        AppExtension android = project.extensions.getByType(AppExtension)
        android.applicationVariants.all { ApplicationVariant variant ->
            RipperTestTask task = project.tasks.create("ripper${variant.name.capitalize()}", RipperTestTask)
            task.group = TestToolboxPlugin.TASKS_GROUP
            task.description = "Runs a ripper rutine against the ${variant.name.capitalize()} variant on the connected devices"
            task.variantName = variant.name
            task.outputs.upToDateWhen { false }

            if (project.extensions.getByType(RipperPluginExtension).reinstallApk) {
                task.dependsOn(variant.assemble)
                variant.outputs.each { output ->
                    task.apkFile = output.outputFile
                }
            }
        }
    }

    void copyRipperApks() {
        RIPPER_APK.each {
            InputStream source = getClass().getResourceAsStream("/ripper-dist/$it")
            if (source == null) {
                throw new GradleException("Can't find apk $it")
            }
            File target = Paths.get(project.getBuildDir().getPath(), "ripper-dist", it).toFile()
            def path = target.getPath()
            project.logger.lifecycle("Copy $it to $path")
            FileUtils.copyInputStreamToFile(source, target);
        }
    }
}
