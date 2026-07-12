/*
 * The MIT License
 *
 * Copyright (c) 2016 IKEDA Yasuyuki
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

package org.jenkinsci.plugins.runselector.filters;

import hudson.model.Item;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.runselector.steps.SelectRunStep;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Tests for {@link RunFilterParameter} and {@link ParameterizedRunFilter}
 */
@WithJenkins
public class ParameterizedRunFilterTest {
    private JenkinsRule j;

    private WorkflowJob jobToSelect;
    private WorkflowRun runToSelect1;
    private WorkflowRun runToSelect2;

    @BeforeEach
    void prepareBuildsToSelect(JenkinsRule rule) throws Exception {
        this.j = rule;
        jobToSelect = j.jenkins.createProject(
            WorkflowJob.class,
            RandomStringUtils.randomAlphanumeric(7)
        );
        jobToSelect.setDefinition(new CpsFlowDefinition("// do nothing"));
        runToSelect1 = j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        runToSelect2 = j.assertBuildStatusSuccess(jobToSelect.scheduleBuild2(0));
        runToSelect1.keepLog();
    }

    @Test
    void testConfigureBuildFilterParameter() throws Exception {
        RunFilterParameter param = new RunFilterParameter(
            "PARAM",
            "description",
            new AndRunFilter(
                new ParametersRunFilter("PARAM1=VALUE1"),
                new SavedRunFilter()
            )
        );
        WorkflowJob job = j.jenkins.createProject(
                WorkflowJob.class,
                RandomStringUtils.randomAlphanumeric(7)
        );
        job.addProperty(new ParametersDefinitionProperty(param));
        j.configRoundtrip((Item)job);
        j.assertEqualDataBoundBeans(
            param,
            job.getProperty(ParametersDefinitionProperty.class)
                .getParameterDefinition("PARAM")
        );
    }

    @Test
    void testConfigureParameterizedBuildFilter() throws Exception {
        ParameterizedRunFilter filter = new ParameterizedRunFilter("${PARAM}");
        SelectRunStep step = new SelectRunStep("test");
        step.setFilter(filter);
        new StepConfigTester(j).configRoundTrip(step);
        j.assertEqualDataBoundBeans(filter, step.getFilter());
    }

    @Test
    void testIsSelectableWithDefault() throws Exception {
        WorkflowJob selecter = j.jenkins.createProject(
            WorkflowJob.class,
            RandomStringUtils.randomAlphanumeric(7)
        );
        selecter.addProperty(new ParametersDefinitionProperty(
            new RunFilterParameter(
                "FILTER",
                "description",
                new SavedRunFilter()
            )
        ));
        selecter.setDefinition(new CpsFlowDefinition(String.format(
            "def runWrapper = selectRun"
            + " job: '%s',"
            + " filter: [$class: 'ParameterizedRunFilter', parameter: '${FILTER}'],"
            + " verbose: true;"
            + "assert(runWrapper.id == '%s')",
            jobToSelect.getFullName(),
            runToSelect1.getId()
        )));

        j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
    }

    @Test
    void testIsSelectableWithParameter() throws Exception {
        WorkflowJob selecter = j.jenkins.createProject(
            WorkflowJob.class,
            RandomStringUtils.randomAlphanumeric(7)
        );
        selecter.addProperty(new ParametersDefinitionProperty(
            new RunFilterParameter(
                "FILTER",
                "description",
                new NoRunFilter()
            )
        ));
        selecter.setDefinition(new CpsFlowDefinition(String.format(
            "def runWrapper = selectRun"
            + " job: '%s',"
            + " filter: [$class: 'ParameterizedRunFilter', parameter: '${FILTER}'],"
            + " verbose: true;"
            + "assert(runWrapper.id == '%s')",
            jobToSelect.getFullName(),
            runToSelect1.getId()
        )));

        j.assertBuildStatusSuccess(selecter.scheduleBuild2(
            0,
            new ParametersAction(
                new StringParameterValue("FILTER", "<SavedRunFilter />")
            )
        ));
    }

    @Test
    void testIsSelectableWithUI() throws Exception {
        WorkflowJob selecter = j.jenkins.createProject(
            WorkflowJob.class,
            RandomStringUtils.randomAlphanumeric(7)
        );
        selecter.addProperty(new ParametersDefinitionProperty(
            new RunFilterParameter(
                "FILTER",
                "description",
                new SavedRunFilter()
            )
        ));
        selecter.setDefinition(new CpsFlowDefinition(String.format(
            "def runWrapper = selectRun"
            + " job: '%s',"
            + " filter: [$class: 'ParameterizedRunFilter', parameter: '${FILTER}'],"
            + " verbose: true;"
            + "assert(runWrapper.id == '%s')",
            jobToSelect.getFullName(),
            runToSelect1.getId()
        )));

        WebClient wc = j.createWebClient();
        // Jenkins sends 405 response for GET of build page.. deal with that:
        wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
        wc.getOptions().setPrintContentOnFailingStatusCode(false);
        j.submit(wc.getPage(selecter, "build").getFormByName("parameters"));
        j.waitUntilNoActivity();
        j.assertBuildStatusSuccess(selecter.getLastBuild());
    }

    @Test
    void testIsSelectableBadParameter() throws Exception {
        WorkflowJob selecter = j.jenkins.createProject(
            WorkflowJob.class,
            RandomStringUtils.randomAlphanumeric(7)
        );
        selecter.addProperty(new ParametersDefinitionProperty(
            new RunFilterParameter(
                "FILTER",
                "description",
                new NoRunFilter()
            )
        ));
        selecter.setDefinition(new CpsFlowDefinition(String.format(
            "def runWrapper = selectRun"
            + " job: '%s',"
            + " filter: [$class: 'ParameterizedRunFilter', parameter: '${FILTER}'],"
            + " verbose: true;",
            jobToSelect.getFullName()
        )));

        j.assertBuildStatus(
            Result.FAILURE,
            selecter.scheduleBuild2(
                0,
                new ParametersAction(
                    new StringParameterValue("FILTER", "Bad Parameter")
                )
            ).get()
        );
    }

    @Test
    void testIsSelectableEmptyParameter() throws Exception {
        WorkflowJob selecter = j.jenkins.createProject(
            WorkflowJob.class,
            RandomStringUtils.randomAlphanumeric(7)
        );
        selecter.addProperty(new ParametersDefinitionProperty(
            new StringParameterDefinition(
                "FILTER",
                "",
                "description"
            )
        ));
        selecter.setDefinition(new CpsFlowDefinition(String.format(
            "def runWrapper = selectRun"
            + " job: '%s',"
            + " filter: [$class: 'ParameterizedRunFilter', parameter: '${FILTER}'],"
            + " verbose: true;"
            + "assert(runWrapper.id == '%s')",
            jobToSelect.getFullName(),
            runToSelect2.getId()
        )));

        j.assertBuildStatusSuccess(selecter.scheduleBuild2(0));
    }
}
