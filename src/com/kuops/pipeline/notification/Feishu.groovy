package com.kuops.pipeline.notification

import com.kuops.pipeline.common.*
import com.cloudbees.groovy.cps.NonCPS

String getWebhook() {
    if (env.GIT_CURRENT_BRANCH == 'master') {
        return GlobalVars.FEISHU_WEBHOOK_PROD
    }  else {
        return GlobalVars.FEISHU_WEBHOOK_DEV
    }
}

@NonCPS
String generatorMessage(stat,color) {
    def commonutil = new CommonUtils()
    def messageTemplate = libraryResource 'com/kuops/pipeline/notification/Message.json'
    def binding = [
            'GIT_REPO_NAME': env.GIT_REPO_NAME,
            'GIT_CURRENT_BRANCH': env.GIT_CURRENT_BRANCH,
            'GIT_SHORT_COMMIT': env.GIT_SHORT_COMMIT,
            'BUILD_STAT': stat,
            'BUILD_USER': env.BUILD_USER,
            'BUILD_STATUS_COLOR': color,
            'DOCKER_IMAGES': commonutil.generatorImageFullUrls(),
            'COMMIT_USER': env.COMMIT_USER,
            'GIT_GROUP_NAME': env.GIT_GROUP_NAME,
            'BUILD_URL': env.BUILD_URL
    ]
    def message =  commonutil.renderTemplate(messageTemplate, binding)
    return message
}

String buildNotification(webhook,stat,color) {
    def message = generatorMessage(stat,color)
    return sh(
          script: """#!/bin/bash +x
            curl -X POST -s ${webhook} -H "Content-Type: application/json"  -d '${message}'
          """
    )
}