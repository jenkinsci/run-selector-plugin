package org.jenkinsci.plugins.runselector.selectors;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PermalinkProjectAction;
import hudson.model.Run;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
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
 * Tests for {@link PermalinkRunSelector}.
 *
 * @author Alexandru Somai
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PermalinkRunSelectorTest  {

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
    void testLastBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_BUILD.getId());
        verifySelectedRun(selector, abortedRun);
    }

    @Test
    void testLastFailedBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_FAILED_BUILD.getId());
        verifySelectedRun(selector, failureRun);
    }

    @Test
    void testLastStableBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_STABLE_BUILD.getId());
        verifySelectedRun(selector, successRun);
    }

    @Test
    void testLastSuccessfulBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_SUCCESSFUL_BUILD.getId());
        verifySelectedRun(selector, unstableRun);
    }

    @Test
    void testLastUnstableBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_UNSTABLE_BUILD.getId());
        verifySelectedRun(selector, unstableRun);
    }

    @Test
    void testLastCompletedBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_UNSUCCESSFUL_BUILD.getId());
        verifySelectedRun(selector, abortedRun);
    }

    @Test
    void testWorkflow() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(String.format("" +
                        "def runWrapper = selectRun job: '%s', " +
                        " selector: [$class: 'PermalinkRunSelector', id: 'lastStableBuild'] \n" +
                        "assert runWrapper.id == '%s'",
                jobToSelect.getFullName(), successRun.getId())));

        j.assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    @Test
    void testPermalinkSelectorParameter() throws Exception {
        FreeStyleProject selecter = j.createFreeStyleProject();
        selecter.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("NUM", "")
        ));
        RunSelector selector = new PermalinkRunSelector("$NUM");

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "lastSuccessfulBuild")
                )
        ));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, Matchers.<Run>is(jobToSelect.getLastSuccessfulBuild()));

        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "lastStableBuild")
                )
        ));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, Matchers.<Run>is(jobToSelect.getLastStableBuild()));

        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "lastBuild")
                )
        ));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, Matchers.<Run>is(jobToSelect.getLastBuild()));

        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "lastFailedBuild")
                )
        ));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, Matchers.<Run>is(jobToSelect.getLastFailedBuild()));

        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "lastUnstableBuild")
                )
        ));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, Matchers.<Run>is(jobToSelect.getLastUnstableBuild()));

        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "lastUnsuccessfulBuild")
                )
        ));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, Matchers.<Run>is(jobToSelect.getLastUnsuccessfulBuild()));
    }

    private void verifySelectedRun(RunSelector selector, Run expectedRun) throws Exception {
        FreeStyleProject selecter = j.createFreeStyleProject();

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, is(expectedRun));
    }
}
