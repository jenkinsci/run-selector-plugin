package org.jenkinsci.plugins.runselector.selectors;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class SpecificRunSelectorTest {

    @ClassRule
    public static final JenkinsRule j = new JenkinsRule();

    @Issue("JENKINS-14266")
    @Test
    public void testUnsetVar() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(3));

        FreeStyleProject selecter = j.createFreeStyleProject();
        RunSelector selector = new SpecificRunSelector("$NUM");

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "2")
                )
        ));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun.getNumber(), is(2));

        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("HUM", "two")
                )
        ));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, nullValue());
    }

    @Issue("JENKINS-19693")
    @Test
    public void testDisplayName() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(3));
        jobToSelect.getBuildByNumber(2).setDisplayName("RC1");

        FreeStyleProject selecter = j.createFreeStyleProject();
        RunSelector selector = new SpecificRunSelector("$NUM");

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "RC1")
                )
        ));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun.getNumber(), is(2));

        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "RC2")
                )
        ));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, nullValue());
    }

    @Test
    public void testPermalink() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(3));

        FreeStyleProject selecter = j.createFreeStyleProject();
        RunSelector selector = new SpecificRunSelector("$NUM");

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
}
