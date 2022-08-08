import com.shiny.jenkins.pipeline.*


def call() {
    GlobalVariables globalVariables = new GlobalVariables(this)
    globalVariables.setGlobalVariables()
    Utilities utils = new Utilities(this)
    utils.setCurrentBuildDescription()
    lastChanges since: 'LAST_SUCCESSFUL_BUILD', format:'SIDE',matching: 'LINE'
    utils.printEnv()
}
