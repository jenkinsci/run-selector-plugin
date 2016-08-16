package org.jenkinsci.plugins.runselector.selectors;

import hudson.model.*;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.runselector.filters.ParametersRunFilter;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link BuildNumberRunSelector}.
 *
 * @author Alexandru Somai
 */
public class BuildNumberRunSelectorTest {

    @ClassRule
    public static final JenkinsRule j = new JenkinsRule();

    private static FreeStyleProject jobToSelect;

    @BeforeClass
    public static void setUp() throws Exception {
        jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(3));
    }

    @Test
    public void testBuildNumberSelector() throws Exception {
        FreeStyleProject selecter = j.createFreeStyleProject();
        BuildNumberRunSelector selector = new BuildNumberRunSelector("1");
        assertThat(selector.getBuildNumber(), is("1"));

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun.getNumber(), is(1));
    }

    @Test
    public void testBuildNumberSelectorParameter() throws Exception {
        FreeStyleProject selecter = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("BAR", "1");
        selecter.addProperty(new ParametersDefinitionProperty(paramDef));
        RunSelector selector = new BuildNumberRunSelector("$BAR");

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun.getNumber(), is(1));
    }

    @Test
    public void testBuildNumberSelectorWithParameterFilter() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0, (Cause) null,
                new ParametersAction(new StringParameterValue("FOO", "foo"))));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(2));

        FreeStyleProject selecter = j.createFreeStyleProject();
        RunSelector selector = new BuildNumberRunSelector("1");

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
        FreeStyleProject selecter = j.createFreeStyleProject();
        RunSelector selector = new BuildNumberRunSelector("$NUM");

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

    @Test
    public void testBuildNumberWorkflow() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun(String.format("" +
                "def runWrapper = selectRun job: '%s', " +
                " selector: [$class: 'BuildNumberRunSelector', buildNumber: '1'], " +
                " verbose: true \n" +
                "assert runWrapper.id == '%s'", jobToSelect.getFullName(), jobToSelect.getBuildByNumber(1).getId()));

        j.assertBuildStatusSuccess(run);
    }

    @Test
    public void testBuildNumberWorkflowDoesNotExist() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun(String.format("" +
                "def runWrapper = selectRun job: '%s', " +
                " selector: [$class: 'BuildNumberRunSelector', buildNumber: '-1'], " +
                " verbose: true", jobToSelect.getFullName()));

        j.assertBuildStatus(Result.FAILURE, run);
    }

    private static WorkflowRun createWorkflowJobAndRun(String script) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(script));
        return job.scheduleBuild2(0).get();
    }
}
