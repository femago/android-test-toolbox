package co.edu.uniandes.miso.test_toolbox.ripper

import com.android.build.gradle.AppExtension
import com.android.utils.StdLogger
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

    @TaskAction
    def runRipperTest(){

        android = project.extensions.getByType(AppExtension)
        ripper = project.extensions.getByType(RipperPluginExtension)

        String packageName = getPackageName()
        project.logger.lifecycle("Starting ripper task $variantName for package $packageName")
        
        stdLogger = new StdLogger(StdLogger.Level.VERBOSE)
    }

    private String getPackageName() {
        def matchingVariants = android.applicationVariants.matching { var -> var.name == variantName }

        if (matchingVariants.isEmpty()) {
            throw new GradleException("Could not find the '" + variantName + "' variant")
        }

        return matchingVariants.first().getGenerateBuildConfig().getAppPackageName()

    }
}
