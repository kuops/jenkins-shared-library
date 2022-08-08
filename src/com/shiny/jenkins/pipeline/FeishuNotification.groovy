package com.shiny.jenkins.pipeline

import groovy.json.JsonOutput

class FeishuNotification implements Serializable {
    def script
    def env
    Utilities utils

    FeishuNotification(script) {
        this.script = script
        this.env = script.env
        this.utils = new Utilities(script)
    }

    String messageTitle() {
        if (env.FEISHU_NOTIFICATION_TITLE) {
            return env.FEISHU_NOTIFICATION_TITLE
        } else {
            return "Jenkins 构建消息"
        }
    }

    def notification() {
        def header = [
                'title': [
                        'tag'    : 'plain_text',
                        'content': messageTitle()
                ],
                'template': env.FEISHU_NOTIFICATION_COLOR
        ]
        def elements = [
                [
                        'tag': 'div',
                        'text': [
                                'tag': 'lark_md',
                                'content': "** 🚁 Job 名称：** ${env.JOB_NAME}"
                        ]
                ],
                [
                        'tag': 'div',
                        'text': [
                                'tag': 'lark_md',
                                'content': "** 📝 Git 地址：** [${env.GIT_GROUP_NAME}/${env.GIT_REPO_NAME}](${env.GIT_HTTPS_URL})"
                        ]
                ],
                [
                        'tag': 'div',
                        'text': [
                                'tag': 'lark_md',
                                'content': "** 🌱 Git 分支：** ${env.GIT_BRANCH_NAME}"
                        ]
                ],
                [
                        'tag': 'div',
                        'text': [
                                'tag': 'lark_md',
                                'content': "** 🔐 Commit ID：** ${env.GIT_COMMIT}"
                        ]
                ],
                [
                        'tag': 'div',
                        'text': [
                                'tag': 'lark_md',
                                'content': "** 🧘 Build User：** ${env.BUILD_USER}"
                        ]
                ],
                [
                        'tag': 'action',
                        'actions': [
                                [
                                        'tag': 'button',
                                        'text': [
                                                'tag': 'lark_md',
                                                'content': ' 📄 构建日志'
                                        ],
                                        'url': "${env.BUILD_URL}/console",
                                        'type': 'default'

                                ],
                                [
                                        'tag': 'button',
                                        'text': [
                                                'tag': 'lark_md',
                                                'content': ' 📊 变更详情'
                                        ],
                                        'url': "${env.BUILD_URL}/last-changes/",
                                        'type': 'default'

                                ]
                        ]
                ]
        ]
        def card = [
                'config': ['enable_forward': true],
                'header': header,
                'elements': elements

        ]
        def requestBody = [
                'msg_type': 'interactive',
                'card'    : card
        ]
        script.httpRequest(
                'url': env.FEISHU_WEBHOOK,
                'httpMode': 'POST',
                'contentType': 'APPLICATION_JSON_UTF8',
                'requestBody': JsonOutput.toJson(requestBody)
        )
    }
}
