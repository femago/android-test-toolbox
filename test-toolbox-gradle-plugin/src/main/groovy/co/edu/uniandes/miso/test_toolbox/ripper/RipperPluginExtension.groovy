package co.edu.uniandes.miso.test_toolbox.ripper

import com.google.common.collect.ImmutableMap
import org.gradle.api.Project

class RipperPluginExtension {

    public static final ImmutableMap<String,String> TEST_TYPES = ImmutableMap.<String, String>builder()
            .put("explore-floating-button", "co.edu.uniandes.miso.test_toolbox.ripper.ExploreFloatingButtonTest")
            .put("explore-menu", "co.edu.uniandes.miso.test_toolbox.ripper.ExploreMenuTest")
            .put("explore-navigation-drawer", "co.edu.uniandes.miso.test_toolbox.ripper.ExploreNavigationDrawerTest")
            .build()
    /**
     * Attempts to uninstall and then install the variant´s apk
     */
    boolean reinstallApk = false

    /**
     * Allows to define an alternative android package for applying the ripper
     */
    String targetPackageName = ""

    /**
     *
     */
    List<String> selectedRippers = new ArrayList<>(TEST_TYPES.keySet())

    int connectTimeoutMs = 5000
    int timeOut = 60

    List<String> excludedDevices = new ArrayList<String>()

    private final Project project

    RipperPluginExtension(Project project) {
        this.project = project
    }

}
