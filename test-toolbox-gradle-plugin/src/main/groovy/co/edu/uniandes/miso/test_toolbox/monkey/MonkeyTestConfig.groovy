package co.edu.uniandes.miso.test_toolbox.monkey

import co.edu.uniandes.miso.test_toolbox.TestToolboxPlugin
import co.edu.uniandes.miso.test_toolbox.ToolboxTestType
import co.edu.uniandes.miso.test_toolbox.ripper.RipperPluginExtension
import co.edu.uniandes.miso.test_toolbox.ripper.RipperTestTask
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.core.BuilderConstants
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.reporting.Report

import java.nio.file.Paths

class MonkeyTestConfig extends ToolboxTestType {

    MonkeyTestConfig(Project project) {
        super(project)
    }

    @Override
    protected void initExtensions() {
        project.extensions.create('monkey', MonkeyPluginExtension, project)
    }

    @Override
    protected void initTasks() {
        project.logger.lifecycle('Start configuring monkey tasks')

        AppExtension android =  project.extensions.getByType(AppExtension)
        android.applicationVariants.all { ApplicationVariant variant ->

            MonkeyTestTask task = project.tasks.create("monkey${variant.name.capitalize()}", MonkeyTestTask)
            task.group = TestToolboxPlugin.TASKS_GROUP
            task.description = "Run the ${variant.name.capitalize()} monkey tests on the connected devices"
            task.variantName = variant.name
            task.reportFileDirectory = new File(project.buildDir, BuilderConstants.FD_REPORTS)
            task.outputs.upToDateWhen { false }

            if (project.extensions.getByType(MonkeyPluginExtension).install) {
                task.dependsOn(variant.assemble)
                variant.outputs.each { output ->
                    task.apkFile = output.outputFile
                }
            }
        }
    }

}
