import com.kuops.pipeline.common.*
import com.kuops.pipeline.notification.*
import com.kuops.pipeline.java.*

void call() {
    TimeZone.setDefault(TimeZone.getTimeZone('Asia/Shanghai'))
    def commonUtils = new CommonUtils()
    def javaUtils = new JavaUtils()
    def nodeLabel = new JavaUtils().getNodeLabel()
    def feishuNotification = new Feishu()

    pipeline {
        agent {
            node {
                label nodeLabel
            }
        }
        environment {
            GIT_SHORT_COMMIT = commonUtils.getGitShortCommit()
            GIT_COMMIT_TAGS = commonUtils.getCommitTags(env.GIT_SHORT_COMMIT)
            GIT_CURRENT_BRANCH  = commonUtils.getBranch()
            GIT_REPO_NAME = commonUtils.getGitRepoName()
            GIT_GROUP_NAME = commonUtils.getGitGroupName()
            FEISHU_WEBHOOK = feishuNotification.getWebhook()
            BUILD_USER = commonUtils.buildUser()
            COMMIT_USER = commonUtils.commitUser()
            DOCKER_IMAGE_NAME = commonUtils.generatorImageName()
            BUILD_URL = commonUtils.buildUrl()
        }
        stages {
            stage ('printenv') {
                steps {
                    container ('maven') {
                        script {
                            commonUtils.printEnv()
                            feishuNotification.buildNotification(env.FEISHU_WEBHOOK,"开始构建","blue")
                        }
                    }
                }
            }
            stage ('build-jar') {
                steps {
                    container ('maven') {
                        script {
                            javaUtils.mavenBuild()
                        }
                    }
                }
            }
            stage ('sonar-scan') {
                steps {
                    container ('sonar-cli') {
                        script {
                            commonUtils.sonarScan('java')
                        }
                    }
                }
            }
            stage ('build-image') {
                steps {
                    container ('docker') {
                        script {
                            javaUtils.buildImage()
                        }
                    }
                }
            }
            stage ('deploy') {
                steps {
                    container ('helm') {
                        script {
                            javaUtils.deploy()
                        }
                    }
                }
            }
        }
        post {
            success {
                script {
                    feishuNotification.buildNotification(env.FEISHU_WEBHOOK,"构建成功","green")
                }
            }
            failure {
                script {
                    feishuNotification.buildNotification(env.FEISHU_WEBHOOK,"构建失败","red")
                }
            }
            aborted {
                script {
                    feishuNotification.buildNotification(env.FEISHU_WEBHOOK,"构建被终止","grey")
                }
            }
        }
    }
}