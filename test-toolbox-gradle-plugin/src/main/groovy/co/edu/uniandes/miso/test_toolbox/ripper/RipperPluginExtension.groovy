package co.edu.uniandes.miso.test_toolbox.ripper

import org.gradle.api.Project

class RipperPluginExtension {
    boolean install = false

    private final Project project

    RipperPluginExtension(Project project) {
        this.project = project
    }
}
