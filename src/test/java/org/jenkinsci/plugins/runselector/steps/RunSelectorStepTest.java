package org.jenkinsci.plugins.runselector.steps;

import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
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

import java.util.Locale;

import static java.lang.String.format;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link RunSelectorStep}.
 *
 * @author Alexandru Somai
 */
public class RunSelectorStepTest {

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
        WorkflowRun run = createWorkflowJobAndRun("def runWrapper = runSelector job: '' ");

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("ERROR: Job parameter not provided", run);
    }

    @Test
    public void missingProject() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun("def runWrapper = runSelector job: 'not-existent' ");

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("ERROR: Unable to find any job named: not-existent. This may be due to incorrect project name or permission settings", run);
    }

    @Test
    public void usingDefaultSelectorAndFilter() throws Exception {
        WorkflowRun upstreamRun = createWorkflowJobAndRun("echo 'foobar'");
        String projectName = upstreamRun.getParent().getFullName();
        j.assertBuildStatusSuccess(upstreamRun);

        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def runWrapper = runSelector job: '%s' \n" +
                "echo 'Selected run: ' + runWrapper.displayName", projectName));

        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Run Selector was not provided, using the default one: Latest specific status build (Stable)", run);
        j.assertLogContains("Run Filter was not provided", run);
        j.assertLogContains("Selected run: #1", run);
    }

    @Test
    public void upstreamHasNoLastStableBuild() throws Exception {
        WorkflowRun upstreamRun = createWorkflowJobAndRun("throw new Exception()");
        String projectName = upstreamRun.getParent().getFullName();
        j.assertBuildStatus(Result.FAILURE, upstreamRun);

        WorkflowRun run = createWorkflowJobAndRun(format("def runWrapper = runSelector job: '%s' ", projectName));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("Run Selector was not provided, using the default one: Latest specific status build (Stable)", run);
        j.assertLogContains("Run Filter was not provided", run);
        j.assertLogContains(format("ERROR: Unable to find Run for: %s, with selector: Latest specific status build (Stable) and filter: No Filter", projectName), run);
    }

    @Test
    public void specificBuildNumberDoesNotExist() throws Exception {
        WorkflowRun upstreamRun = createWorkflowJobAndRun("echo 'foobar'");
        String projectName = upstreamRun.getParent().getFullName();
        j.assertBuildStatusSuccess(upstreamRun);

        WorkflowRun run = createWorkflowJobAndRun(format("" +
                "def runWrapper = runSelector job: '%s', " +
                "selector: [$class: 'SpecificRunSelector', buildNumber: '-1'], " +
                "verbose: true", projectName));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(format("no such build -1 in %s", projectName), run);
        j.assertLogContains(format("ERROR: Unable to find Run for: %s, with selector: Specific build and filter: No Filter", projectName), run);
    }

    private WorkflowRun createWorkflowJobAndRun(String script) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(script));
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        assertThat(runFuture, notNullValue());

        return runFuture.get();
    }
}
