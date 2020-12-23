#!/usr/bin/env groovy
package com.kuops.pipeline.java
import com.kuops.pipeline.common.*

void mavenBuild() {
    sh '''
      mvn -DskipTests -Dmaven.test.skip=true \
        -U clean install 
    '''
}

String getNodeLabel() {
    String nodeLabel

    if (env.JAVA_VERSION == 8) {
        nodeLabel = 'maven-jdk-8'
    } else {
        nodeLabel = 'maven-jdk-11'
    }

    return nodeLabel
}

String getBaseImageTag() {
    String baseImageTag

    if (env.JAVA_VERSION == 8) {
        baseImageTag = '8-jdk'
    } else {
        baseImageTag = '11-jdk'
    }

    return baseImageTag
}

String getServicePort() {
    def servicePort = ""

    if (env.SERVICE_PORT) {
        if (env.SERVICE_PORT.isNumber()) {
            servicePort = env.SERVICE_PORT
        } else {
            error('SERVICE_PORT not a number.')
        }
    } else {
        servicePort = 8080
    }

    return servicePort
}

String generateDockerfile() {
    def commonutil = new CommonUtils()
    def dockerfileTemplate = libraryResource 'com/kuops/pipeline/java/Dockerfile'
    def binding = [
            "JAVA_BASEIMAGE_TAG": getBaseImageTag(),
            "SERVICE_PORT"      : getServicePort()
    ]
    return commonutil.renderTemplate(dockerfileTemplate, binding)
}

String getJarFile(modulePath) {
    if (env.MODULE_PATH) {
        modulePath = env.MODULE_PATH
    } else {
        modulePath = 'bootstrap'
    }

    jarFile = sh(
            script: "find . -name '*.jar'|grep -v sources|grep ${modulePath}",
            returnStdout: true
    ).trim()

    return jarFile
}

void buildImage() {
    def dockerfile = generateDockerfile()
    def commonutil = new CommonUtils()
    def jarFile = getJarFile()
    def buildingDir = 'buildImage'

    if (jarFile) {
        sh """
          mkdir -p ${buildingDir}
          cp ${jarFile} ${buildingDir}
        """
    }

    dir('buildImage') {
        commonutil.writeToFile(dockerfile, 'Dockerfile')
        def appImageUrls = commonutil.generatorImageFullUrls()
        for (appImageUrl in appImageUrls) {
            def appImage = docker.build(appImageUrl)
            appImage.push()
        }
    }
}

void deploy() {
    def commonutil = new CommonUtils()
    commonutil.setGitUser()
    withCredentials(
            [
                    kubeconfigFile(credentialsId: commonutil.generatorKubeconfigName(), variable: 'KUBECONFIG'),
                    sshUserPrivateKey(credentialsId: 'ci-bot', keyFileVariable: 'SSH_KEY')
            ]
    ) {
        sh 'echo ssh -i $SSH_KEY -l git -o StrictHostKeyChecking=no \\"\\$@\\" > local_ssh.sh'
        sh 'chmod +x local_ssh.sh'
        withEnv(['GIT_SSH=./local_ssh.sh']) {
            sh """
                git clone ssh://github.com:36622/kuops/charts-template.git
            """
        }
        sh """
            kubectl get node
            helm ls --all-namespaces
            ls -al charts-template
        """
    }
}