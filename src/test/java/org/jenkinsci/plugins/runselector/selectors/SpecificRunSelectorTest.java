package org.jenkinsci.plugins.runselector.selectors;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Run;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.runselector.filters.ParametersRunFilter;
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

    @Test
    public void testSpecificRunSelector() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(2));

        FreeStyleProject selecter = j.createFreeStyleProject();
        SpecificRunSelector selector = new SpecificRunSelector("1");
        assertThat(selector.getBuildNumber(), is("1"));

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun.getNumber(), is(1));
    }

    @Test
    public void testSpecificRunSelectorParameter() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(2));

        FreeStyleProject selecter = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("BAR", "1");
        selecter.addProperty(new ParametersDefinitionProperty(paramDef));
        RunSelector selector = new SpecificRunSelector("$BAR");

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun.getNumber(), is(1));
    }

    @Test
    public void testSpecificRunSelectorWithParameterFilter() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0, (Cause) null,
                new ParametersAction(new StringParameterValue("FOO", "foo"))));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(2));

        FreeStyleProject selecter = j.createFreeStyleProject();
        SpecificRunSelector selector = new SpecificRunSelector("1");

        RunFilter runFilter = new ParametersRunFilter("FOO=bogus");
        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL, runFilter));
        assertThat(selectedRun, nullValue());

        runFilter = new ParametersRunFilter("FOO=foo");
        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL, runFilter));
        assertThat(selectedRun.getNumber(), is(1));
    }

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
