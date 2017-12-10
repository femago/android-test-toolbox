package co.edu.uniandes.miso.test_toolbox.ripper

import com.android.build.gradle.AppExtension
import com.android.utils.StdLogger
import org.gradle.api.DefaultTask
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

    @TaskAction
    def runRipperTest(){
        println "Starting "
        android = project.extensions.getByType(AppExtension)
        ripper = project.extensions.getByType(RipperPluginExtension)

        stdLogger = new StdLogger(StdLogger.Level.VERBOSE)
        stdLogger.info("Mi Plugin funcionooooooooooo")
    }
}
