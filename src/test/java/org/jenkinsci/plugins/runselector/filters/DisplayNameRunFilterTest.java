package org.jenkinsci.plugins.runselector.filters;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.runselector.selectors.StatusRunSelector;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link DisplayNameRunFilter}.
 *
 * @author Alexandru Somai
 */
public class DisplayNameRunFilterTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static FreeStyleProject jobToSelect;

    @BeforeClass
    @SuppressWarnings("Duplicates")
    public static void setUp() throws Exception {
        jobToSelect = j.createFreeStyleProject();
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        assertThat(jobToSelect.getLastBuild().getNumber(), is(3));
    }

    @Test
    public void testDisplayName() throws Exception {
        jobToSelect.getBuildByNumber(2).setDisplayName("RC1");

        FreeStyleProject selecter = j.createFreeStyleProject();
        RunSelector selector = new StatusRunSelector();
        RunFilter filter = new DisplayNameRunFilter("$NUM");

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "RC1")
                )
        ));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL, filter));
        assertThat(selectedRun.getNumber(), is(2));

        run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                new Cause.UserIdCause(),
                new ParametersAction(
                        new StringParameterValue("NUM", "RC2")
                )
        ));
        selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL, filter));
        assertThat(selectedRun, nullValue());
    }

    @Test
    public void testDisplayNameWorkflow() throws Exception {
        jobToSelect.getBuildByNumber(2).setDisplayName("RC1");

        WorkflowRun run = createWorkflowJobAndRun(String.format("" +
                "def runWrapper = selectRun job: '%s', " +
                " filter: [$class: 'DisplayNameRunFilter', runDisplayName: 'RC1'], " +
                " verbose: true \n" +
                "assert runWrapper.id == '%s'", jobToSelect.getFullName(), jobToSelect.getBuildByNumber(2).getId()));

        j.assertBuildStatusSuccess(run);
    }

    @Test
    public void testDisplayNameWorkflowWrongValue() throws Exception {
        WorkflowRun run = createWorkflowJobAndRun(String.format("" +
                "def runWrapper = selectRun job: '%s', " +
                " filter: [$class: 'DisplayNameRunFilter', runDisplayName: 'does-not-exist'], " +
                " verbose: true", jobToSelect.getFullName()));

        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(String.format("Unable to find Run for: %s", jobToSelect.getFullName()), run);
    }

    private static WorkflowRun createWorkflowJobAndRun(String script) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, RandomStringUtils.randomAlphanumeric(7));
        job.setDefinition(new CpsFlowDefinition(script));
        return job.scheduleBuild2(0).get();
    }
}
