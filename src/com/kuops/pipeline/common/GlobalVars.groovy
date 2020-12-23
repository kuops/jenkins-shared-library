#!/usr/bin/env groovy
package com.kuops.pipeline.common

class GlobalVars {
    static final DOCKER_REGISTRY = 'hub.kuops.com'
    static final DOCKER_IMAGE_REPO_DEV = this.DOCKER_REGISTRY + '/dev'
    static final DOCKER_IMAGE_REPO_TEST = this.DOCKER_REGISTRY + '/test'
    static final DOCKER_IMAGE_REPO_PROD = this.DOCKER_REGISTRY + '/prod'
    static final DOCKER_IMAGE_REPO_LIBRARY = this.DOCKER_REGISTRY + '/library'
    static final FEISHU_WEBHOOK_DEV = ''
    static final FEISHU_WEBHOOK_PROD = ''
    static final DEVELOP_CLUSTER_CHARTS_REPO = ''
    static final JENKINS_CI_BOT_NAME = 'ci-bot'
    static final JENKINS_CI_BOT_EMAIL = ''
    static final JENKINS_CI_KUBECONFIG_DEV = 'ci-dev-kubeconfig'
    static final JENKINS_CI_KUBECONFIG_DROD = 'ci-prod-kubeconfig'
}