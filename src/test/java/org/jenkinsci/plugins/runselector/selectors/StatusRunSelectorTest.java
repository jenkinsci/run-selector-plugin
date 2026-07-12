package org.jenkinsci.plugins.runselector.selectors;

import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.runselector.testutils.JenkinsRuleHelper;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link StatusRunSelector}.
 *
 * @author Alexandru Somai
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatusRunSelectorTest {

    private JenkinsRule j;
    private WorkflowJob jobToSelect;
    private WorkflowRun successRun;
    private WorkflowRun unstableRun;
    private WorkflowRun failureRun;
    private WorkflowRun abortedRun;

    @BeforeAll
    void setUpJenkins() throws Throwable {
        j = JenkinsRuleHelper.createAndStart(getClass());
    }

    @AfterAll
    void tearDownJenkins() throws Throwable {
        j.after();
    }

    @BeforeAll
    @SuppressWarnings("Duplicates")
    void setUp() throws Exception {
        jobToSelect = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));

        jobToSelect.setDefinition(new CpsFlowDefinition("currentBuild.result = 'SUCCESS'"));
        successRun = jobToSelect.scheduleBuild2(0).get();

        jobToSelect.setDefinition(new CpsFlowDefinition("currentBuild.result = 'UNSTABLE'"));
        unstableRun = jobToSelect.scheduleBuild2(0).get();

        jobToSelect.setDefinition(new CpsFlowDefinition("currentBuild.result = 'FAILURE'"));
        failureRun = jobToSelect.scheduleBuild2(0).get();

        jobToSelect.setDefinition(new CpsFlowDefinition("currentBuild.result = 'ABORTED'"));
        abortedRun = jobToSelect.scheduleBuild2(0).get();
    }

    @Test
    void testLastStableBuild() throws Exception {
        RunSelector selector = new StatusRunSelector(StatusRunSelector.BuildStatus.STABLE);
        verifySelectedRun(selector, successRun);
    }

    @Test
    void testLastSuccessfulBuild() throws Exception {
        RunSelector selector = new StatusRunSelector(StatusRunSelector.BuildStatus.SUCCESSFUL);
        verifySelectedRun(selector, unstableRun);
    }

    @Test
    void testLastUnstableBuild() throws Exception {
        RunSelector selector = new StatusRunSelector(StatusRunSelector.BuildStatus.UNSTABLE);
        verifySelectedRun(selector, unstableRun);
    }

    @Test
    void testLastFailedBuild() throws Exception {
        RunSelector selector = new StatusRunSelector(StatusRunSelector.BuildStatus.FAILED);
        verifySelectedRun(selector, failureRun);
    }

    @Test
    void testLastCompletedBuild() throws Exception {
        RunSelector selector = new StatusRunSelector(StatusRunSelector.BuildStatus.COMPLETED);
        verifySelectedRun(selector, abortedRun);
    }

    @Test
    void testLastAnyBuild() throws Exception {
        RunSelector selector = new StatusRunSelector(StatusRunSelector.BuildStatus.ANY);
        verifySelectedRun(selector, abortedRun);
    }

    @Test
    void testWorkflow() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(String.format("" +
                        "def runWrapper = selectRun job: '%s', " +
                        " selector: [$class: 'StatusRunSelector', buildStatus: 'STABLE'] \n" +
                        "assert runWrapper.id == '%s'",
                jobToSelect.getFullName(), successRun.getId())));

        j.assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    private void verifySelectedRun(RunSelector selector, Run expectedRun) throws Exception {
        FreeStyleProject selecter = j.createFreeStyleProject();

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, is(expectedRun));
    }
}
