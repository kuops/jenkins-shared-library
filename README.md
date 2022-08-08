# jenkins-shared-library

simple jenkins library

## Function

- Feishu notification
- Request Prometheus metrics
- Get git variables

## Git variables

- 

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

        stage('Ansible Deploy') {
            steps {
                sshagent(['ansible-ssh']) {
                    script {
                        if (env.HOST_IP) {
                            sh "ansible-playbook -i ${HOST_IP}, -e 'target=${HOST_IP}' playbooks/jenkins/clean_kube_node_logs.yml"
                        } else {
                            currentBuild.result = 'ABORTED'
                            env.ERROR_MESSAGE = '未传递 HOST_IP 参数'
                            echo(env.ERROR_MESSAGE)
                        }
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
        failure {
            script {
                notification("red")
            }
        }
    }
}
```
