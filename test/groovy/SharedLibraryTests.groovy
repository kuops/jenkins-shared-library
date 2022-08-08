package groovy

import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library

class SharedLibraryTests extends BaseTest {
    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
    }

    @Test
    void test_library() throws Exception {
        def library = library().name('commons')
                .defaultVersion('<notNeeded>')
                .allowOverride(true)
                .implicit(true)
                .targetPath('<notNeeded>')
                .retriever(projectSource())
                .build()
        helper.registerSharedLibrary(library)
        runScript("test/pipelines/example.Jenkinsfile")
        printCallStack()
        assertJobStatusSuccess()
    }
}
