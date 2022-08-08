package groovy

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.yaml.snakeyaml.Yaml

class BaseTest extends DeclarativePipelineTest {
    Map env = [:]
    def value = 'IamASecret'
    @Override
    void setUp() throws Exception {
        super.setUp()
        binding.setVariable('env', env)
        helper.registerAllowedMethod('readYaml', [Map.class], { Map parameters ->
            Yaml yamlParser = new Yaml()
            if (parameters.text) {
                return yamlParser.load(parameters.text)
            }
            if (parameters.file) {
                String text = new File(parameters.file).getText('UTF-8')
                return  yamlParser.load(text)
            }
        })
        helper.registerAllowedMethod('httpRequest', [Map.class], {
            return [content: '{ "key": "value" }', status: 200]
        })
        env.GIT_COMMIT = value.digest('SHA-1')
        env.GIT_PREVIOUS_SUCCESSFUL_COMMIT = value.digest('SHA-1')
        env.GIT_URL = 'git@git.example.com:group/repository.git'
        env.BUILD_USER = 'developer'
        env.FEISHU_NOTIFICATION_TITLE = 'Test'
        env.FEISHU_NOTIFICATION_COLOR = 'green'
        env.JOB_NAME = 'example'
        env.GIT_GROUP_NAME = 'group'
    }
}
