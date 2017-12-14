package co.edu.uniandes.miso.test_toolbox.ripper

import co.edu.uniandes.miso.test_toolbox.TestToolboxPlugin
import co.edu.uniandes.miso.test_toolbox.ToolboxTestType
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.core.BuilderConstants
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.reporting.Report

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
            //For each variant
            RipperTestTask task = project.tasks.create("ripper${variant.name.capitalize()}", RipperTestTask)
            task.group = TestToolboxPlugin.TASKS_GROUP
            task.description = "Runs a ripper rutine against the ${variant.name.capitalize()} variant on the connected devices"
            task.variantName = variant.name
            task.outputs.upToDateWhen { false }
            task.testResultDirectory = new File(project.buildDir, "test-results/ripper/${variant.name.toLowerCase()}")

            Task reportTask = buildReportTask(variant, task.testResultDirectory)

            if (project.extensions.getByType(RipperPluginExtension).reinstallApk) {
                task.dependsOn(variant.assemble)
                variant.outputs.each { output ->
                    task.apkFile = output.outputFile
                }
                task.finalizedBy(reportTask)
            }
        }
    }

    private Task buildReportTask(ApplicationVariant variant, File testResultDirectory) {
        def reportFileDirectory = new File(project.buildDir, BuilderConstants.FD_REPORTS+"/ripper/${variant.name.toLowerCase()}")
        if(reportFileDirectory.exists())
            FileUtils.deleteDirectory(reportFileDirectory)
        reportFileDirectory.mkdirs()

        def ripperReportTask = project.tasks.create("reportRipper${variant.name.capitalize()}").doLast {
            def ant = new AntBuilder()
            ant.taskdef(
                    name: 'junitReport',
                    classname: 'org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator',
                    classpath: project.configurations.antClasspath.asPath
            )

            ant.junitReport(todir: reportFileDirectory.toString(), tofile: "aggregated-test-results.xml") {
                fileset(dir: testResultDirectory.toString())
                Report(format: 'noframes', todir: reportFileDirectory)
            }
        }
        ripperReportTask.group = TestToolboxPlugin.TASKS_GROUP
        ripperReportTask.description = "Generates an html report with the results from the ripper execution"
        ripperReportTask
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
