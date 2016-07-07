/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Alan Harder
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
package org.jenkinsci.plugins.runselector;

import hudson.model.Run;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Ignore
public class CopyArtifactWorkflowTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void test_simpleUntriggeredCopy() throws Exception {
        // create "project_1" with an archived artifact...
        WorkflowJob project_1 = createWorkflow("project_1",
                "writeFile text: 'hello', file: 'hello.txt'; " +
                "step([$class: 'ArtifactArchiver', artifacts: 'hello.txt', fingerprint: true])");
        WorkflowRun b = jenkinsRule.assertBuildStatusSuccess(project_1.scheduleBuild2(0));
        assertArtifactInArchive(b);

        // Now lets try copy the artifact from "project_1" to "project_2"
        WorkflowJob project_2 = createWorkflow("project_2",
                "step([$class: 'RunSelector', projectName: 'project_1', filters: 'hello.txt']); " +
                "step([$class: 'ArtifactArchiver', artifacts: 'hello.txt', fingerprint: true]);");
        b = jenkinsRule.assertBuildStatusSuccess(project_2.scheduleBuild2(0));
        assertArtifactInArchive(b);
    }

    /**
     * Test filtering on parameters works to copy from workflow jobs.
     */
/*
    @Issue("JENKINS-26694")
    @Test
    public void testFilterByParametersForWorkflow() throws Exception {
        WorkflowJob copiee = createWorkflow("copiee",
                "writeFile text: \"${PARAM}\", file:'artifact.txt';"
                + "archive includes:'artifact.txt';"
        );
        copiee.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("PARAM", "")
        ));
        copiee.setDefinition(new CpsFlowDefinition(
                "node {"
                        + "writeFile text: \"${PARAM}\", file:'artifact.txt';"
                        + "archive includes:'artifact.txt';"
                + "}",
                true
        ));
        
        FreeStyleProject copier = jenkinsRule.createFreeStyleProject();
        copier.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("PARAM_TO_COPY", "")
        ));
        RunSelector ca = RunSelectorUtil.createRunSelector(
                copiee.getFullName(),
                "PARAM=${PARAM_TO_COPY}",
                new LastCompletedRunSelector(),
                "artifact.txt",
                "",
                false,
                false
        );
        ca.upgradeFromCopyartifact10();
        copier.getBuildersList().add(ca);
        
        // #1: PARAM=foo
        jenkinsRule.assertBuildStatusSuccess(copiee.scheduleBuild2(0, new ParametersAction(
                new StringParameterValue("PARAM", "foo")
        )));
        // #2: PARAM=bar
        jenkinsRule.assertBuildStatusSuccess(copiee.scheduleBuild2(0, new ParametersAction(
                new StringParameterValue("PARAM", "bar")
        )));
        
        FreeStyleBuild build = copier.scheduleBuild2(0, new UserCause(), new ParametersAction(
                new StringParameterValue("PARAM_TO_COPY", "foo")
        )).get();
        jenkinsRule.assertBuildStatusSuccess(build);
        
        assertEquals("foo", build.getWorkspace().child("artifact.txt").readToString());
    }
*/

    /**
     * Demonstrate that we can run a downstream build and then copy artifacts from it.
     */
    @Issue("JENKINS-33577")
    @Test
    public void copyFromDownstreamBuild() throws Exception {
        WorkflowJob us = createWorkflow("us", "step([$class: 'RunSelector', projectName: 'ds', selectors: [$class: 'SpecificRunSelector', buildNumber: \"${build('ds').number}\"]]); echo readFile('art')");
        WorkflowJob ds = createWorkflow("ds", "writeFile file: 'art', text: env.BUILD_TAG; archive includes: 'art'");
        jenkinsRule.assertLogContains("jenkins-ds-1", jenkinsRule.assertBuildStatusSuccess(us.scheduleBuild2(0)));
    }

    private void assertArtifactInArchive(Run b) {
        List<WorkflowRun.Artifact> artifacts = b.getArtifacts();
        Assert.assertEquals(1, artifacts.size());
        Assert.assertEquals("hello.txt", artifacts.get(0).relativePath);
    }

    private WorkflowJob createWorkflow(String name, String script) throws IOException {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition("node {" + script + "}", true));
        return job;
    }
}
