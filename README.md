# jenkins-shared-library

simple jenkins library

## Function

- Feishu notification
- Request Prometheus metrics
- Get git variables

## Variables

- BUILD_USER
- GIT_SHORT_COMMIT
- GIT_PREVIOUS_SHORT_COMMIT
- GIT_COMMIT_USER
- GIT_BRANCH_NAME
- GIT_TAG_NAME
- GIT_REPO_NAME
- GIT_HTTPS_URL
- GIT_GROUP_NAME
- ANSIBLE_FORCE_COLOR
- ANSIBLE_HOST_KEY_CHECKING

## Usage 

example `jenkinsfile` use this jenkins shared library.

```
#!/usr/bin/env groovy

@Library('devops') _

import com..jenkins.pipeline.*
def cleanKubeNodeLog = new CleanKubeNodeLog(this)

pipeline {
    agent {
        docker {
            alwaysPull true
            image 'hub.shiny.net/library/ansible:2.9'
            registryCredentialsId 'hub.shiny.net'
            registryUrl 'https://hub.shiny.net/v1/'
            reuseNode true
        }
    }

    environment {
        FEISHU_NOTIFICATION_TITLE = "Kubernetes Clean Logs"
    }

    triggers{ cron('00 */2 * * *') }

    options {
        ansiColor('xterm')
        timestamps()
        disableConcurrentBuilds()
        disableResume()
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '180', numToKeepStr: '100')
    }

    stages {
        stage('initialization') {
            steps {
                script {
                    initialization()
                    def hosts = cleanKubeNodeLog.queryHighDataDiskUsageHost()
                    if (hosts) {
                        env.HOST_IP = hosts.join(',')
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                notification("green")
            }
        }
    }
}
```
