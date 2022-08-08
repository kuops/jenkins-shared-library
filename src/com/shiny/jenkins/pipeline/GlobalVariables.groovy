package com.shiny.jenkins.pipeline

class GlobalVariables implements Serializable {
    def script
    def env
    Utilities utils

    GlobalVariables(script) {
        this.script = script
        this.env = script.env
        this.utils = new Utilities(script)
    }

    void setGlobalVariables() {
        setJenkinsVariables()
        setGitVariables()
        setFeishuVariables()
        setAnsibleVariables()
    }

    void setJenkinsVariables() {
        env.JENKINS_BOT_NAME = 'jenkins-ci'
        env.JENKINS_BOT_EMAIL = 'jenkins-ci@shiny.io'
        env.BUILD_USER = utils.buildUser()
    }

    void setGitVariables() {
        env.GIT_SHORT_COMMIT = utils.gitShortCommit()
        env.GIT_PREVIOUS_SHORT_COMMIT = utils.gitPreviousShortCommit()
        env.GIT_COMMIT_USER = utils.gitCommitUser()
        env.GIT_BRANCH_NAME = utils.gitBranchName()
        env.GIT_TAG_NAME = utils.gitTagName()
        env.GIT_REPO_NAME = utils.gitRepoName()
        env.GIT_HTTPS_URL = utils.gitHttpsUrl()
        env.GIT_GROUP_NAME = utils.gitGroupName()
    }

    void setFeishuVariables() {
        Map config = script.readYaml(text: script.libraryResource('com/shiny/jenkins/pipeline/feishu/config.yaml'))
        env.FEISHU_WEBHOOK = config.notification_webhook
    }

    void setAnsibleVariables() {
        env.ANSIBLE_FORCE_COLOR = "true"
        env.ANSIBLE_HOST_KEY_CHECKING = "false"
    }

}
