/*
 * The MIT License
 * 
 * Copyright (c) 2015 IKEDA Yasuyuki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ParameterizedRunSelector}
 */
public class ParameterizedRunSelectorTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    /**
     * Also applicable for workflow jobs.
     */
    @Issue("JENKINS-30357")
    @Test
    public void testWorkflow() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        Run runToSelect = j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));

        WorkflowJob selecter = createWorkflowJob();

        ParameterDefinition paramDef = new StringParameterDefinition(
                "SELECTOR", "<StatusRunSelector><buildStatus>STABLE</buildStatus></StatusRunSelector>"
        );
        selecter.addProperty(new ParametersDefinitionProperty(paramDef));

        selecter.setDefinition(new CpsFlowDefinition(String.format("" +
                "def runWrapper = selectRun job: '%s', " +
                " selector: [$class: 'ParameterizedRunSelector', parameterName: '${SELECTOR}'] \n" +
                "assert(runWrapper.id == '%s')", jobToSelect.getFullName(), runToSelect.getId())));

        j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
    }

    /**
     * Should not cause a fatal error even for a broken selectors.
     */
    @Test
    public void testBrokenParameter() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        FreeStyleProject selecter = j.createFreeStyleProject();

        RunSelector selector = new ParameterizedRunSelector("${SELECTOR}");
        Run run = (Run) selecter.scheduleBuild2(
                0,
                new ParametersAction(
                        new StringParameterValue("SELECTOR", "<SomeBrokenSelector")
                )
        ).get();
        j.assertBuildStatusSuccess(run);

        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, nullValue());
    }

    /**
     * Should not cause a fatal error even for an unavailable selectors.
     */
    @Test
    public void testUnavailableSelector() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        FreeStyleProject selecter = j.createFreeStyleProject();

        RunSelector selector = new ParameterizedRunSelector("${SELECTOR}");
        Run run = (Run) selecter.scheduleBuild2(
                0,
                new ParametersAction(
                        new StringParameterValue("SELECTOR", "<NoSuchSelector />")
                )
        ).get();
        j.assertBuildStatusSuccess(run);

        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, nullValue());
    }

    /**
     * Should not cause a fatal error even for an empty selectors.
     */
    @Test
    public void testEmptySelector() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        FreeStyleProject selecter = j.createFreeStyleProject();

        RunSelector selector = new ParameterizedRunSelector("${SELECTOR}");
        Run run = (Run) selecter.scheduleBuild2(
                0,
                new ParametersAction(
                        new StringParameterValue("SELECTOR", "")
                )
        ).get();
        j.assertBuildStatusSuccess(run);

        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, nullValue());
    }

    /**
     * Also accepts immediate value.
     */
    @Test
    public void testImmediateValue() throws Exception {
        // Prepare a job to be selected.
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        Run runToSelect = j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));

        WorkflowJob selecter = createWorkflowJob();
        selecter.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("SELECTOR", "")
        ));
        selecter.setDefinition(new CpsFlowDefinition(String.format("" +
                "def runWrapper = selectRun job: '%s', " +
                " selector: [$class: 'ParameterizedRunSelector', parameterName: '${SELECTOR}'] \n" +
                "assert(runWrapper.id == '%s')", jobToSelect.getFullName(), runToSelect.getId())));

        j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                null,
                new ParametersAction(new StringParameterValue(
                        "SELECTOR", "<StatusRunSelector><buildStatus>STABLE</buildStatus></StatusRunSelector>"
                ))
        ));
    }

    /**
     * Also accepts variable expression.
     */
    @Test
    public void testVariableExpression() throws Exception {
        FreeStyleProject jobToSelect = j.createFreeStyleProject();
        Run runToSelect = j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));

        FreeStyleProject selecter = j.createFreeStyleProject();
        selecter.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("SELECTOR", "")
        ));
        RunSelector selector = new ParameterizedRunSelector("${SELECTOR}");

        Run run = j.assertBuildStatusSuccess(selecter.scheduleBuild2(
                0,
                (Cause) null,
                new ParametersAction(new StringParameterValue(
                        "SELECTOR",
                        "<StatusRunSelector><buildStatus>STABLE</buildStatus></StatusRunSelector>"
                ))
        ));
        Run selectedRun = selector.select(jobToSelect, new RunSelectorContext(j.jenkins, run, TaskListener.NULL));
        assertThat(selectedRun, is(runToSelect));
    }

    private static WorkflowJob createWorkflowJob() throws IOException {
        return j.jenkins.createProject(WorkflowJob.class, "test" + j.jenkins.getItems().size());
    }
}
