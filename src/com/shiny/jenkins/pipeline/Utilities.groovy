package com.shiny.jenkins.pipeline

import java.util.regex.Matcher

class Utilities implements Serializable {
    def script
    def env
    def currentBuild

    Utilities(script) {
        this.script = script
        this.env = script.env
        this.currentBuild = script.currentBuild
    }

    String buildUser() {
        String buildUser
        script.wrap([$class: 'BuildUser']) { buildUser = env.BUILD_USER }
        if (buildUser == null) {
            buildUser = '自动触发'
        }
        return buildUser
    }

    String gitShortCommit() {
        String gitCommit = env.GIT_COMMIT
        return gitCommit[0..7]
    }

    String gitPreviousShortCommit() {
        String previousCommit = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT
        if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT) {
            return previousCommit[0..7]
        }
    }

    void gitDiffFiles() {
        if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT != null) {
            script.sh "git --no-pager diff  --name-status ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT} ${env.GIT_COMMIT}|tr '\\t' ' '"
        }
    }

    String gitCommitUser() {
        return script.sh(
                script: "git --no-pager show -s --pretty=%an",
                returnStdout: true
        ).trim()
    }

    String gitBranchName() {
        def currentBranch
        if (env.BRANCH_NAME != null) {
            currentBranch = env.BRANCH_NAME
        } else {
            currentBranch = script.sh(
                    script: "git show-ref | grep `git rev-parse HEAD` | grep remotes | grep -v HEAD | sed -e 's/.*remotes.origin.//'",
                    returnStdout: true
            ).trim()
        }

        return currentBranch
    }

    String gitTagName() {
        return script.sh(
                script: "git --no-pager tag --points-at `git rev-parse HEAD`|xargs",
                returnStdout: true
        ).trim()
    }

    String gitRepoName() {
        Matcher matcherRepoName = env.GIT_URL =~ '([^/]+).git$'
        matcherRepoName ? matcherRepoName[0][1] : null
    }

    String gitHttpsUrl() {
        if (env.GIT_URL =~ 'https') {
            return env.GIT_URL
        }
        def scheme = 'https://'
        Matcher matcherUrl = env.GIT_URL =~ 'git@(.+).git$'
        String url = matcherUrl[0][1]
        def gitHttpUrl = scheme + url.replace(":", "/")
        return gitHttpUrl
    }

    String gitGroupName() {
        Matcher matcherGroupName = env.GIT_URL =~ ':.*?([^/]+)/[^/]+\\.git$'
        matcherGroupName ? matcherGroupName[0][1] : null
    }

    void setCurrentBuildDescription() {
        currentBuild.description = "构建人: ${env.BUILD_USER}"
    }

    void printEnv() {
        script.sh "printenv"
    }
}
