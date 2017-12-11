package co.edu.uniandes.miso.test_toolbox

import org.gradle.api.Project

abstract class ToolboxTestType {

    protected final Project project

    ToolboxTestType(Project project) {
        this.project = project
    }

    void init(){
        initExtensions()
        initTasks()
    }

    abstract protected void initExtensions()

    abstract protected void initTasks()

}
