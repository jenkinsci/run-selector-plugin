package org.jenkinsci.plugins.runselector.selectors;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.PermalinkProjectAction;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link PermalinkRunSelector}.
 *
 * @author Alexandru Somai
 */
public class PermalinkRunSelectorTest  {

    @ClassRule
    public static final JenkinsRule j = new JenkinsRule();

    private static WorkflowJob jobToSelect;
    private static WorkflowRun successRun;
    private static WorkflowRun unstableRun;
    private static WorkflowRun failureRun;
    private static WorkflowRun abortedRun;

    @BeforeClass
    @SuppressWarnings("Duplicates")
    public static void setUp() throws Exception {
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
    public void testLastBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_BUILD.getId());
        verifySelectedRun(selector, abortedRun);
    }

    @Test
    public void testLastFailedBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_FAILED_BUILD.getId());
        verifySelectedRun(selector, failureRun);
    }

    @Test
    public void testLastStableBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_STABLE_BUILD.getId());
        verifySelectedRun(selector, successRun);
    }

    @Test
    public void testLastSuccessfulBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_SUCCESSFUL_BUILD.getId());
        verifySelectedRun(selector, unstableRun);
    }

    @Test
    public void testLastUnstableBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_UNSTABLE_BUILD.getId());
        verifySelectedRun(selector, unstableRun);
    }

    @Test
    public void testLastCompletedBuild() throws Exception {
        RunSelector selector = new PermalinkRunSelector(PermalinkProjectAction.Permalink.LAST_UNSUCCESSFUL_BUILD.getId());
        verifySelectedRun(selector, abortedRun);
    }

    @Test
    public void testWorkflow() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(String.format("" +
                        "def runWrapper = selectRun job: '%s', " +
                        " selector: [$class: 'PermalinkRunSelector', id: 'lastStableBuild'] \n" +
                        "assert runWrapper.id == '%s'",
                jobToSelect.getFullName(), successRun.getId())));

        j.assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    @Test
    public void testPermalinkSelectorParameter() throws Exception {
        FreeStyleProject selecter = j.createFreeStyleProject();
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
        assertThat(selectedRun, Matchers.<Run>is(jobToSelect.getLastSuccessfulBuild()));

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

    private static void verifySelectedRun(RunSelector selector, Run expectedRun) throws Exception {
        FreeStyleProject selecter = j.createFreeStyleProject();

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, is(expectedRun));
    }
}
