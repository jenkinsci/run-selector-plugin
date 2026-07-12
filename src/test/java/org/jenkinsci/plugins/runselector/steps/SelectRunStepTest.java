package org.jenkinsci.plugins.runselector.steps;

import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.VersionNumber;
import org.apache.commons.lang3.RandomStringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.BuildWatcherExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.localizer.LocaleProvider;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Locale;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for the {@link SelectRunStep}.
 *
 * @author Alexandru Somai
 */
@WithJenkins
public class SelectRunStepTest {

    @RegisterExtension
    static final BuildWatcherExtension watcher = new BuildWatcherExtension();

    private JenkinsRule j;
    private LocaleProvider providerToRestore;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        this.j = rule;
        providerToRestore = LocaleProvider.getProvider();

        // expect English messages
        LocaleProvider.setProvider(new LocaleProvider() {
            @Override
            public Locale get() {
                return Locale.ENGLISH;
            }
        });
    }

    @AfterEach
    void tearDown() {
        LocaleProvider.setProvider(providerToRestore);
    }

    @Test
    void missingProjectName() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun("def runWrapper = selectRun '' ");

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("ERROR: Job parameter not provided", run);
    }

    @Test
    void missingProject() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun("def runWrapper = selectRun 'not-existent' ");

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("ERROR: Unable to find any job named: not-existent. This may be due to incorrect project name or permission settings", run);
    }

    @Test
    void usingDefaultSelectorAndFilter() throws Exception {
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
    void upstreamHasNoLastStableBuild() throws Exception {
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
    void testStatusSymbol() throws Exception {
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
    void testSpecificRunSymbol() throws Exception {
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
    void testPermalinkSymbol() throws Exception {
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
     * Checks if the given property is not null, and if it's greater than or equal to the given version.
     *
     * @param property the property to be checked
     * @param version  the version on which the property is checked against
     */
    private static void assumePropertyIsGreaterThanOrEqualTo(@CheckForNull String property, @NonNull String version) {
        assumeTrue(property != null, "property must not be null");
        assumeTrue(new VersionNumber(property).compareTo(new VersionNumber(version)) >= 0,
                "property version must be >= " + version);
    }

    private WorkflowRun createWorkflowJobAndRun(String script) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(script));
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        assertThat(runFuture, notNullValue());

        return runFuture.get();
    }
}
