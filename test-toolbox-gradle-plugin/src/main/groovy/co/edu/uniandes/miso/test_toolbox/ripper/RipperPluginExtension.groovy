package co.edu.uniandes.miso.test_toolbox.ripper

import org.gradle.api.Project

class RipperPluginExtension {
    /**
     * Attempts to uninstall and then install the variant´s apk
     */
    boolean reinstallApk = false
    String targetPackageName = ""

    int connectTimeoutMs = 5000
    int timeOut = 60

    List<String> excludedDevices = new ArrayList<String>()

    private final Project project

    RipperPluginExtension(Project project) {
        this.project = project
    }
}
