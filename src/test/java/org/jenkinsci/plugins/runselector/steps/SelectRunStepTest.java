package org.jenkinsci.plugins.runselector.steps;

import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.VersionNumber;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.localizer.LocaleProvider;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Locale;

import static java.lang.String.format;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

/**
 * Tests for the {@link SelectRunStep}.
 *
 * @author Alexandru Somai
 */
public class SelectRunStepTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @ClassRule
    public static BuildWatcher watcher = new BuildWatcher();

    private LocaleProvider providerToRestore;

    @Before
    public void setUp() {
        providerToRestore = LocaleProvider.getProvider();

        // expect English messages
        LocaleProvider.setProvider(new LocaleProvider() {
            @Override
            public Locale get() {
                return Locale.ENGLISH;
            }
        });
    }

    @After
    public void tearDown() {
        LocaleProvider.setProvider(providerToRestore);
    }

    @Test
    public void missingProjectName() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun("def runWrapper = selectRun '' ");

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("ERROR: Job parameter not provided", run);
    }

    @Test
    public void missingProject() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun("def runWrapper = selectRun 'not-existent' ");

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("ERROR: Unable to find any job named: not-existent. This may be due to incorrect project name or permission settings", run);
    }

    @Test
    public void usingDefaultSelectorAndFilter() throws Exception {
        WorkflowRun upstreamRun = createWorkflowJobAndRun("echo 'foobar'");
        String projectName = upstreamRun.getParent().getFullName();
        j.assertBuildStatusSuccess(upstreamRun);

        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def runWrapper = selectRun '%s' \n" +
                "echo 'Selected run: ' + runWrapper.displayName", projectName));

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Run Selector was not provided, using the default one: Latest specific status build (STABLE)", run);
        j.assertLogContains("Run Filter was not provided", run);
        j.assertLogContains("Selected run: #1", run);
    }

    @Test
    public void upstreamHasNoLastStableBuild() throws Exception {
        WorkflowRun upstreamRun = createWorkflowJobAndRun("throw new Exception()");
        String projectName = upstreamRun.getParent().getFullName();
        j.assertBuildStatus(Result.FAILURE, upstreamRun);

        WorkflowRun run = createWorkflowJobAndRun(format("def runWrapper = selectRun '%s' ", projectName));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("Run Selector was not provided, using the default one: Latest specific status build (STABLE)", run);
        j.assertLogContains("Run Filter was not provided", run);
        j.assertLogContains(format("ERROR: Unable to find Run for: %s, with selector: Latest specific status build (STABLE) and filter: No Filter", projectName), run);
    }

    @Test
    public void testStatusSymbol() throws Exception {
        assumeSymbolDependencies();

        WorkflowRun upstreamRun = createWorkflowJobAndRun("echo 'foobar'");
        String projectName = upstreamRun.getParent().getFullName();
        j.assertBuildStatusSuccess(upstreamRun);

        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def runWrapper = selectRun job: '%s', " +
                " selector: status('STABLE'), " +
                " verbose: true", projectName));

        j.assertBuildStatusSuccess(run);
    }

    @Test
    public void testSpecificRunSymbol() throws Exception {
        assumeSymbolDependencies();

        WorkflowRun upstreamRun = createWorkflowJobAndRun("echo 'foobar'");
        String projectName = upstreamRun.getParent().getFullName();
        j.assertBuildStatusSuccess(upstreamRun);

        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def runWrapper = selectRun job: '%s', " +
                " selector: buildNumber('1'), " +
                " verbose: true", projectName));

        j.assertBuildStatusSuccess(run);
    }

    @Test
    public void testPermalinkSymbol() throws Exception {
        assumeSymbolDependencies();

        WorkflowRun upstreamRun = createWorkflowJobAndRun("echo 'foobar'");
        String projectName = upstreamRun.getParent().getFullName();
        j.assertBuildStatusSuccess(upstreamRun);

        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def runWrapper = selectRun job: '%s', " +
                " selector: permalink('lastStableBuild'), " +
                " verbose: true", projectName));

        j.assertBuildStatusSuccess(run);
    }

    /**
     * To use the @Symbol annotation in tests, minimum workflow-cps version 2.10 is required.
     * This dependency comes with other dependency version requirements, as stated by this method.
     * To run tests restricted by this method, type
     * <pre>
     *  mvn clean install -Djenkins.version=1.642.1 -Djava.level=7 -Dworkflow-step-api.version=2.3 -Dworkflow-support.version=2.2 -Dworkflow-job.version=2.4 -Dworkflow-basic-steps.version=2.1 -Dworkflow-cps.version=2.10
     * </pre>
     */
    private static void assumeSymbolDependencies() {
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("jenkins.version"), "1.642.1");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("java.level"), "7");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-step-api.version"), "2.3");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-support.version"), "2.2");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-job.version"), "2.4");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-basic-steps.version"), "2.1");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-cps.version"), "2.10");
    }

    /**
     * Checks if the given property is not null, and if it's greater than or equal to the given version.
     *
     * @param property the property to be checked
     * @param version  the version on which the property is checked against
     */
    private static void assumePropertyIsGreaterThanOrEqualTo(@CheckForNull String property, @Nonnull String version) {
        assumeThat(property, notNullValue());
        assumeThat(new VersionNumber(property).compareTo(new VersionNumber(version)), is(greaterThanOrEqualTo(0)));
    }

    private static WorkflowRun createWorkflowJobAndRun(String script) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(script));
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        assertThat(runFuture, notNullValue());

        return runFuture.get();
    }
}
