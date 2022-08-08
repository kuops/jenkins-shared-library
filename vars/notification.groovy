import  com.shiny.jenkins.pipeline.*

def call(String color) {
    FeishuNotification feishuNotification = new FeishuNotification(this)
    feishuNotification.env.FEISHU_NOTIFICATION_COLOR = color
    feishuNotification.notification()
}
