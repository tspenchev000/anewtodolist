import jetbrains.buildServer.configs.kotlin.*
//import jetbrains.buildServer.configs.kotlin.buildFeatures.CommitStatusPublisher
//import jetbrains.buildServer.configs.kotlin.buildFeatures.approval
//import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

project {
    val bts =
    sequential {
        buildType(Maven("Build", "clean compile"))
        parallel {
            buildType(Maven("Test Slow", "clean test", "-Dmaven.test.failure.ignore=true -Dtest=*.integration.*Test"))
            buildType(Maven("Fast Slow", "clean test", "-Dmaven.test.failure.ignore=true -Dtest=*.unit.*Test"))
        }
        buildType(Maven("Package", "clean package", "-DskipTests"))
    }.buildTypes()

    bts.forEach {buildType(it)}
    bts.last().triggers{
        vcs{

        }
    }
}

object VcsRoot : GitVcsRoot({
    name = ""
    url = ""
    branch = "refs/heads/master"
})

class Maven(strName: String, strGoals: String, strRunnerArgs: String ? = null) : BuildType({
    //id(name.toExtId())
    this.name = strName

    vcs {
        root(VcsRoot)
    }

    steps {
        script {
            scriptContent = "echo $strName"
        }
        maven {
            this.goals = strGoals
            this.runnerArgs = strRunnerArgs
        }

    }

    //features {
    //    perfmon {
    //    }
    //}

    //features {
    //    approval {
    //        approvalRules = "user:tspenchev"
    //    }
    //}

})
