@Library('commons')
import com.shiny.jenkins.pipeline.*
def globalVariables = new GlobalVariables(this)
def feishuNotification = new FeishuNotification(this)
def cleanKubeNodeLog = new CleanKubeNodeLog(this)

pipeline {
    agent any
    stages {
        stage('Example Build') {
            steps {
                script {
                    globalVariables.setGlobalVariables()
                    feishuNotification.notification()
                    cleanKubeNodeLog.queryDataDiskUsage()
                }
            }
        }
    }
}
