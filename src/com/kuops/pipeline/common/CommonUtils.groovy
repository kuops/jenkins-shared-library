#!/usr/bin/env groovy
package com.kuops.pipeline.common
import com.cloudbees.groovy.cps.NonCPS

void printEnv() {
    sh 'printenv'
}

String jenkinsUrl() {
    return 'http://jenkins.kuops.com/'
}

String buildUrl() {
    def jenkinsUrl = jenkinsUrl()
    return sh(
            script: """#!/bin/bash +x
              echo ${BUILD_URL}|sed -r 's@http://[^/]+/@${jenkinsUrl}@g'
            """,
            returnStdout: true
    ).trim()
}

String getGitShortCommit() {
    return sh(
            script: "printf \$(git rev-parse --short=8 ${GIT_COMMIT})",
            returnStdout: true
    ).trim()
}

@NonCPS
String buildUser() {
    def userName = ""
    def causes = currentBuild.getBuildCauses()
    if (causes.size != 0 && causes[0].userName != null) {
        userName = causes[0].userName
    } else {
        userName = "自动触发"
    }
    return userName
}

String commitUser() {
    return sh(
            script: "git --no-pager show -s --pretty=%an",
            returnStdout: true
    ).trim()
}

String getBranch() {
    String branchName

    if (env.BRANCH_NAME != null) {
        branchName = env.BRANCH_NAME
    } else {
        sh "git show-ref | grep `git rev-parse HEAD` | grep remotes | grep -v HEAD | sed -e 's/.*remotes.origin.//' > branch.txt"
        branchName = readFile('branch.txt').trim()
    }

    return branchName
}

String getCommitTags(commit) {
    return sh(
            script: "git --no-pager tag --points-at ${commit}|xargs",
            returnStdout: true
    ).trim()
}

String getGitRepoName() {
    return sh(
            script: '''
                echo ${GIT_URL} | awk -F / \'{print $NF}\'|sed \'s@.git@@g\'
            ''',
            returnStdout: true
    ).trim()
}

String getGitGroupName() {
    if (env.GIT_URL.contains("kuops")) {
        return sh(
                script: 'echo ${GIT_URL}|awk -F / \'{print $5}\'',
                returnStdout: true
        ).trim()
    } else {
        return sh(
                script: 'echo ${GIT_URL}|awk -F / \'{print $4}\'',
                returnStdout: true
        ).trim()
    }
}

void  sonarScan(String lang) {
    if (env.GIT_CURRENT_BRANCH == 'test') {
        withSonarQubeEnv {
            if (lang == 'java') {
                BinnaryPath = sh(script: 'find . -name "classes"|sed \'s@^./@@g\'|awk \'{printf "%s,",$1}\'|sed \'s@,$@@g\'', returnStdout: true).trim()
                sh """
                   sonar-scanner -Dsonar.projectKey=gl-generic-template \
                   -Dsonar.sources=. \
                   -Dsonar.language=java \
                   -Dsonar.exclusions=docs/** \
                   -Dsonar.java.binaries=${BinnaryPath}
                """
            }
        }
    } else {
        println 'Skip The SonarScan Steps, Beacuse the branch name is not test'
    }
}

@NonCPS
String renderTemplate(Template, binding) {
    def engine = new groovy.text.SimpleTemplateEngine()
    def template = engine.createTemplate(Template).make(binding)
    return template.toString()
}

void writeToFile(text,filename) {
    def writeFileCmd = "cat > ${filename} <<'EOF'"
    writeFileCmd += "\n${text}\nEOF"
    sh writeFileCmd
}

void writeToFileConvertVars(text,filename) {
    def writeFileCmd = "cat > ${filename} <<EOF"
    writeFileCmd += "\n${text}\nEOF"
    sh writeFileCmd
}

String generatorImageName() {
    if (env.SERVICE_NAME) {
        return env.SERVICE_NAME
    } else {
        return getGitRepoName()
    }
}

@NonCPS
List generatorImageFullUrls() {
    def gitCommitTags = ""
    def imageName = env.DOCKER_IMAGE_NAME
    def imageUrls = []
    def now = new Date()
    def nowDate = now.format("yyyyMMddHHmm")
    if (env.GIT_COMMIT_TAGS) {
        gitCommitTags = env.GIT_COMMIT_TAGS.split(' ')
    }
    switch (env.GIT_CURRENT_BRANCH) {
        case 'master':
            if (gitCommitTags) {
                for (imageTag in gitCommitTags) {
                    imageUrls.add(GlobalVars.DOCKER_IMAGE_REPO_PROD + "/" + "${imageName}" + ":${imageTag}" + "-" + "${nowDate}")
                    if (imageTag == gitCommitTags.last()) {
                        currentBuild.description = "docker_image: ${imageTag}-${nowDate}"
                    }
                }
            } else {
                error('The branch not have tag')
            }
            return imageUrls
        case 'test':
            if (gitCommitTags) {
                for (imageTag in gitCommitTags) {
                    imageUrls.add(GlobalVars.DOCKER_IMAGE_REPO_PROD + "/" + "${imageName}" + ":${imageTag}" + "-" + "${nowDate}")
                    if (imageTag == gitCommitTags.last()) {
                        currentBuild.description = "docker_image_tag: ${imageTag}-${nowDate}"
                    }
                }
            } else {
                def imageTag = env.GIT_SHORT_COMMIT
                imageUrls.add(GlobalVars.DOCKER_IMAGE_REPO_TEST + "/" + "${imageName}" + ":${imageTag}" + "-" + "${nowDate}")
                currentBuild.description = "docker_image_tag: ${imageTag}-${nowDate}"
            }
            return imageUrls
        default:
            def imageTag = env.GIT_SHORT_COMMIT
            imageUrls.add(GlobalVars.DOCKER_IMAGE_REPO_DEV + "/" + "${imageName}" + ":${imageTag}" + "-" + "${nowDate}")
            currentBuild.description = "docker_image_tag: ${imageTag}-${nowDate}"
            return imageUrls
    }
}

void setGitUser() {
    gitUserName = GlobalVars.JENKINS_CI_BOT_NAME
    gitUserEmail = GlobalVars.JENKINS_CI_BOT_EMAIL
    sh """
      git config --global user.name ${gitUserName}
      git config --global user.email ${gitUserEmail}
    """
}

String generatorKubeconfigName() {
    switch (env.GIT_CURRENT_BRANCH) {
        case 'master':
            return GlobalVars.JENKINS_CI_KUBECONFIG_DROD
        default:
            return GlobalVars.JENKINS_CI_KUBECONFIG_DEV
    }
}