/*
 * The MIT License
 * 
 * Copyright (c) 2014 IKEDA Yasuyuki
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

import hudson.maven.MavenModuleSet;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link TriggeredRunSelector}.
 * Some of tests of {@link TriggeredRunSelector} are also in {@link RunSelectorTest}
 */
@Ignore
public class TriggeredRunSelectorTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    /**
     * Tests that web configuration page works correct.
     * @throws Exception
     */
/*
    @Test
    @Ignore("Replaced to TriggeringRunSelector")
    public void testWebConfiguration() throws Exception {
        WebClient wc = j.createWebClient();
        {
            FreeStyleProject p = j.createFreeStyleProject();
            p.getBuildersList().add(RunSelectorUtil.createRunSelector(
                    "${upstream}",
                    "",
                    new TriggeredRunSelector(true, TriggeredRunSelector.UpstreamFilterStrategy.UseOldest, false),
                    "",
                    "",
                    false,
                    false, 
                    false
            ));
            p.save();
            
            j.submit(wc.getPage(p, "configure").getFormByName("config"));
            
            p = j.jenkins.getItemByFullName(p.getFullName(), FreeStyleProject.class);
            assertNotNull(p);
            
            RunSelector copyArtifact = p.getBuildersList().get(RunSelector.class);
            assertNotNull(p);
            
            assertNotNull(copyArtifact.getRunSelector());
            assertEquals(TriggeredRunSelector.class, copyArtifact.getRunSelector().getClass());
            
            TriggeredRunSelector selectors = (TriggeredRunSelector)copyArtifact.getRunSelector();
            
            assertTrue(selectors.isFallbackToLastSuccessful());
            assertEquals(TriggeredRunSelector.UpstreamFilterStrategy.UseOldest, selectors.getUpstreamFilterStrategy());
        }
        
        {
            FreeStyleProject p = j.createFreeStyleProject();
            p.getBuildersList().add(RunSelectorUtil.createRunSelector(
                    "${upstream}",
                    "",
                    new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseNewest, false),
                    "",
                    "",
                    false,
                    false, 
                    false
            ));
            p.save();
            
            j.submit(wc.getPage(p, "configure").getFormByName("config"));
            
            p = j.jenkins.getItemByFullName(p.getFullName(), FreeStyleProject.class);
            assertNotNull(p);
            
            RunSelector copyArtifact = p.getBuildersList().get(RunSelector.class);
            assertNotNull(p);
            
            assertNotNull(copyArtifact.getRunSelector());
            assertEquals(TriggeredRunSelector.class, copyArtifact.getRunSelector().getClass());
            
            TriggeredRunSelector selectors = (TriggeredRunSelector)copyArtifact.getRunSelector();
            
            assertFalse(selectors.isFallbackToLastSuccessful());
            assertEquals(TriggeredRunSelector.UpstreamFilterStrategy.UseNewest, selectors.getUpstreamFilterStrategy());
        }
        
        {
            FreeStyleProject p = j.createFreeStyleProject();
            p.getBuildersList().add(RunSelectorUtil.createRunSelector(
                    "${upstream}",
                    "",
                    new TriggeredRunSelector(true, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false),
                    "",
                    "",
                    false,
                    false, 
                    false
            ));
            p.save();
            
            j.submit(wc.getPage(p, "configure").getFormByName("config"));
            
            p = j.jenkins.getItemByFullName(p.getFullName(), FreeStyleProject.class);
            assertNotNull(p);
            
            RunSelector copyArtifact = p.getBuildersList().get(RunSelector.class);
            assertNotNull(p);
            
            assertNotNull(copyArtifact.getRunSelector());
            assertEquals(TriggeredRunSelector.class, copyArtifact.getRunSelector().getClass());
            
            TriggeredRunSelector selectors = (TriggeredRunSelector)copyArtifact.getRunSelector();
            
            assertTrue(selectors.isFallbackToLastSuccessful());
            assertEquals(TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, selectors.getUpstreamFilterStrategy());
        }
    }
*/

    @Test
    @Ignore("Replaced by TriggeringRunSelector")
    public void testGlobalConfiguration() throws Exception {
/*
        WebClient wc = j.createWebClient();
        TriggeredRunSelector.DescriptorImpl d = TriggeredRunSelector.DESCRIPTOR;
        {
            d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseNewest);
            j.submit(wc.getPage(j.jenkins, "configure").getFormByName("config"));
            assertEquals(TriggeredRunSelector.UpstreamFilterStrategy.UseNewest, d.getGlobalUpstreamFilterStrategy());
        }
        {
            d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseOldest);
            j.submit(wc.getPage(j.jenkins, "configure").getFormByName("config"));
            assertEquals(TriggeredRunSelector.UpstreamFilterStrategy.UseOldest, d.getGlobalUpstreamFilterStrategy());
        }
*/
    }
    
/*
    @Test
    public void testUseOldest() throws Exception {
        FreeStyleProject upstream = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("CONTENT", "foo");
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(paramDef);
        upstream.addProperty(paramsDef);
        FreeStyleProject downstream = j.createFreeStyleProject();
        
        upstream.getBuildersList().add(new FileWriteBuilder("artifact.txt", "${CONTENT}"));
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        RunSelector ca = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseOldest, false),
                "artifact.txt",
                "",
                false,
                false,
                true
        );
        ca.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca);
        downstream.setQuietPeriod(5); // this allows upstream trigger can be merged.
        
        upstream.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();
        
        // 3 upstream builds.
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value1"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value2"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value3"))));
        
        // wait till downstream will be triggered and completed
        j.waitUntilNoActivity();
        
        FreeStyleBuild b = downstream.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of downstream is too short in this environment: %s", b.getCauses()),
                3,
                Util.filters(b.getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertEquals("value1", b.getWorkspace().child("artifact.txt").readToString());
    }
*/

/*
    @Test
    public void testUseOldestByGlobalSetting() throws Exception {
        TriggeredRunSelector.DescriptorImpl d = TriggeredRunSelector.DESCRIPTOR;
        d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseOldest);
        
        FreeStyleProject upstream = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("CONTENT", "foo");
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(paramDef);
        upstream.addProperty(paramsDef);
        FreeStyleProject downstream = j.createFreeStyleProject();
        
        upstream.getBuildersList().add(new FileWriteBuilder("artifact.txt", "${CONTENT}"));
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        RunSelector ca = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false),
                "artifact.txt",
                "",
                false,
                false,
                true
        );
        ca.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca);
        downstream.setQuietPeriod(5); // this allows upstream trigger can be merged.
        
        upstream.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();
        
        // 3 upstream builds.
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value1"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value2"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value3"))));
        
        // wait till downstream will be triggered and completed
        j.waitUntilNoActivity();
        
        FreeStyleBuild b = downstream.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of downstream is too short in this environment: %s", b.getCauses()),
                3,
                Util.filters(b.getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertEquals("value1", b.getWorkspace().child("artifact.txt").readToString());
    }
*/

    
/*
    @Test
    public void testUseNewest() throws Exception {
        FreeStyleProject upstream = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("CONTENT", "foo");
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(paramDef);
        upstream.addProperty(paramsDef);
        FreeStyleProject downstream = j.createFreeStyleProject();
        
        upstream.getBuildersList().add(new FileWriteBuilder("artifact.txt", "${CONTENT}"));
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        RunSelector ca = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseNewest, false),
                "artifact.txt",
                "",
                false,
                false,
                true
        );
        ca.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca);
        downstream.setQuietPeriod(5); // this allows upstream trigger can be merged.
        
        upstream.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();
        
        // 3 upstream builds.
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value1"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value2"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value3"))));
        
        // wait till downstream will be triggered and completed
        j.waitUntilNoActivity();
        
        FreeStyleBuild b = downstream.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of downstream is too short in this environment: %s", b.getCauses()),
                3,
                Util.filters(b.getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertEquals("value3", b.getWorkspace().child("artifact.txt").readToString());
    }
*/

/*
    @Test
    public void testUseNewestByGlobalSetting() throws Exception {
        TriggeredRunSelector.DescriptorImpl d = TriggeredRunSelector.DESCRIPTOR;
        d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseNewest);
        
        FreeStyleProject upstream = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("CONTENT", "foo");
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(paramDef);
        upstream.addProperty(paramsDef);
        FreeStyleProject downstream = j.createFreeStyleProject();
        
        upstream.getBuildersList().add(new FileWriteBuilder("artifact.txt", "${CONTENT}"));
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        RunSelector ca = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false),
                "artifact.txt",
                "",
                false,
                false,
                true
        );
        ca.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca);
        downstream.setQuietPeriod(5); // this allows upstream trigger can be merged.
        
        upstream.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();
        
        // 3 upstream builds.
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value1"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value2"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value3"))));
        
        // wait till downstream will be triggered and completed
        j.waitUntilNoActivity();
        
        FreeStyleBuild b = downstream.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of downstream is too short in this environment: %s", b.getCauses()),
                3,
                Util.filters(b.getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertEquals("value3", b.getWorkspace().child("artifact.txt").readToString());
    }
*/

    
/*
    @Test
    public void testUseOldestNested() throws Exception {
        FreeStyleProject upstream = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("CONTENT", "foo");
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(paramDef);
        upstream.addProperty(paramsDef);
        FreeStyleProject intermediate1 = j.createFreeStyleProject();
        FreeStyleProject intermediate2 = j.createFreeStyleProject();
        FreeStyleProject downstream = j.createFreeStyleProject();
        
        upstream.getBuildersList().add(new FileWriteBuilder("artifact.txt", "${CONTENT}"));
        
        intermediate1.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        intermediate1.setQuietPeriod(5);
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        intermediate2.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        intermediate2.setQuietPeriod(5);
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        intermediate1.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        RunSelector ca = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseOldest, false),
                "artifact.txt",
                "",
                false,
                false,
                true
        );
        ca.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca);
        downstream.setQuietPeriod(5); // this allows upstream trigger can be merged.
        
        upstream.save();
        intermediate1.save();
        intermediate2.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();
        
        // 2 upstream builds for intermediate1.
        upstream.getPublishersList().clear();
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(intermediate1.getName(), Result.SUCCESS));
        upstream.save();
        j.jenkins.rebuildDependencyGraph();
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value1"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value2"))));
        
        // 2 upstream builds for intermediate2.
        upstream.getPublishersList().clear();
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(intermediate2.getName(), Result.SUCCESS));
        upstream.save();
        j.jenkins.rebuildDependencyGraph();
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value3"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value4"))));
        
        // wait till downstream will be triggered and completed
        j.waitUntilNoActivity();
        
        assertNotNull(intermediate1.getLastBuild());
        j.assertBuildStatusSuccess(intermediate1.getLastBuild());
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of intermediate1 is too short in this environment: %s", intermediate1.getLastBuild().getCauses()),
                2,
                Util.filters(intermediate1.getLastBuild().getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertNotNull(intermediate2.getLastBuild());
        j.assertBuildStatusSuccess(intermediate2.getLastBuild());
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of intermediate2 is too short in this environment: %s", intermediate2.getLastBuild().getCauses()),
                2,
                Util.filters(intermediate2.getLastBuild().getCauses(), Cause.UpstreamCause.class).size()
        );
        
        FreeStyleBuild b = downstream.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of downstream is too short in this environment: %s", b.getCauses()),
                2,
                Util.filters(b.getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertEquals("value1", b.getWorkspace().child("artifact.txt").readToString());
    }
*/

/*
    @Test
    public void testUseNewestNested() throws Exception {
        FreeStyleProject upstream = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("CONTENT", "foo");
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(paramDef);
        upstream.addProperty(paramsDef);
        FreeStyleProject intermediate1 = j.createFreeStyleProject();
        FreeStyleProject intermediate2 = j.createFreeStyleProject();
        FreeStyleProject downstream = j.createFreeStyleProject();
        
        upstream.getBuildersList().add(new FileWriteBuilder("artifact.txt", "${CONTENT}"));
        
        intermediate1.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        intermediate1.setQuietPeriod(5);
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        intermediate2.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        intermediate2.setQuietPeriod(5);
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        intermediate1.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        RunSelector ca = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseNewest, false),
                "artifact.txt",
                "",
                false,
                false,
                true
        );
        ca.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca);
        downstream.setQuietPeriod(5); // this allows upstream trigger can be merged.
        
        upstream.save();
        intermediate1.save();
        intermediate2.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();
        
        // 2 upstream builds for intermediate1.
        upstream.getPublishersList().clear();
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(intermediate1.getName(), Result.SUCCESS));
        upstream.save();
        j.jenkins.rebuildDependencyGraph();
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value1"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value2"))));
        
        // 2 upstream builds for intermediate2.
        upstream.getPublishersList().clear();
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(intermediate2.getName(), Result.SUCCESS));
        upstream.save();
        j.jenkins.rebuildDependencyGraph();
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value3"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value4"))));
        
        // wait till downstream will be triggered and completed
        j.waitUntilNoActivity();
        
        assertNotNull(intermediate1.getLastBuild());
        j.assertBuildStatusSuccess(intermediate1.getLastBuild());
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of intermediate1 is too short in this environment: %s", intermediate1.getLastBuild().getCauses()),
                2,
                Util.filters(intermediate1.getLastBuild().getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertNotNull(intermediate2.getLastBuild());
        j.assertBuildStatusSuccess(intermediate2.getLastBuild());
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of intermediate2 is too short in this environment: %s", intermediate2.getLastBuild().getCauses()),
                2,
                Util.filters(intermediate2.getLastBuild().getCauses(), Cause.UpstreamCause.class).size()
        );
        
        FreeStyleBuild b = downstream.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of downstream is too short in this environment: %s", b.getCauses()),
                2,
                Util.filters(b.getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertEquals("value4", b.getWorkspace().child("artifact.txt").readToString());
    }
*/

/*
    @Test
    public void testBackwardCompatibility() throws Exception {
        TriggeredRunSelector.DescriptorImpl d = TriggeredRunSelector.DESCRIPTOR;
        d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseOldest);
        
        FreeStyleProject upstream = j.createFreeStyleProject();
        ParameterDefinition paramDef = new StringParameterDefinition("CONTENT", "foo");
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(paramDef);
        upstream.addProperty(paramsDef);
        FreeStyleProject downstream = j.createFreeStyleProject();
        
        upstream.getBuildersList().add(new FileWriteBuilder("artifact.txt", "${CONTENT}"));
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        RunSelector ca = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(false, null, false),
                "artifact.txt",
                "",
                false,
                false,
                true
        );
        ca.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca);
        downstream.setQuietPeriod(5); // this allows upstream trigger can be merged.
        
        upstream.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();
        
        // 3 upstream builds.
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value1"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value2"))));
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "value3"))));
        
        // wait till downstream will be triggered and completed
        j.waitUntilNoActivity();
        
        FreeStyleBuild b = downstream.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertEquals(
                String.format("upstream triggers seem not to be merged into one downstream build. This means quietPeriod of downstream is too short in this environment: %s", b.getCauses()),
                3,
                Util.filters(b.getCauses(), Cause.UpstreamCause.class).size()
        );
        
        assertEquals("value1", b.getWorkspace().child("artifact.txt").readToString());
    }
*/

/*
    @Test
    public void testIsUseNewest() throws Exception {
        // |Descriptor      |RunSelector   |Result|
        // |:---------------|:---------------|:-----|
        // |null            |null            |false |
        // |UseGlobalSetting|UseGlobalSetting|false |
        // |UseOldest       |UseGlobalSetting|false |
        // |UseNewest       |UseGlobalSetting|true  |
        // |UseOldest       |UseNewest       |true  |
        // |UseNewest       |UseOldest       |false |
        TriggeredRunSelector.DescriptorImpl d = TriggeredRunSelector.DESCRIPTOR;
        
        d.setGlobalUpstreamFilterStrategy(null);
        assertFalse(new TriggeredRunSelector(false, null, false).isUseNewest());
        
        d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting);
        assertFalse(new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false).isUseNewest());
        
        d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseOldest);
        assertFalse(new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false).isUseNewest());
        
        d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseNewest);
        assertTrue(new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false).isUseNewest());
        
        d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseOldest);
        assertTrue(new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseNewest, false).isUseNewest());
        
        d.setGlobalUpstreamFilterStrategy(TriggeredRunSelector.UpstreamFilterStrategy.UseNewest);
        assertFalse(new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseOldest, false).isUseNewest());
    }
*/

    private String getDownstreamAfterOverlappingFlow(boolean allowUpstreamDependencies) throws Exception {
        /*
        //
        //     upstream   |   intermediate   |   downstream
        //
        // Initital build
        // 1     #1 -------.
        // 2               '-----> #1 ---------.
        // 3                                   '-----> #1
        //
        // Direct trigger of intermediate, then upstream
        // 4                 *---> #2 -------.
        // 5     #2 -----.                   |
        // 6             |                   '-------> #2
        // 7             '-------> #3 -----.
        // 8                               '---------> #3
        //

        FreeStyleProject upstream = j.createFreeStyleProject("upstream");
        ParameterDefinition paramDef = new StringParameterDefinition("CONTENT", "foo");
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(paramDef);
        upstream.addProperty(paramsDef);
        FreeStyleProject intermediate = j.createFreeStyleProject("intermediate");
        FreeStyleProject downstream = j.createFreeStyleProject("downstream");

        upstream.getBuildersList().add(new FileWriteBuilder("artifact.txt", "${CONTENT}"));
        upstream.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        upstream.getPublishersList().add(new BuildTrigger(intermediate.getName(), Result.SUCCESS));
        upstream.setQuietPeriod(0);

        RunSelector ca1 = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(true, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, allowUpstreamDependencies),
                "artifact.txt",
                "",
                false,
                false,
                true
        );
        ca1.upgradeFromCopyartifact10();
        intermediate.getBuildersList().add(ca1);
        intermediate.getBuildersList().add(new SleepBuilder(1000));
        intermediate.getPublishersList().add(new ArtifactArchiver("artifact.txt", "", false, false));
        intermediate.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        intermediate.setQuietPeriod(0);
        
        RunSelector ca2 = RunSelectorUtil.createRunSelector(
                upstream.getName(),
                "",
                new TriggeredRunSelector(true, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, allowUpstreamDependencies),
                "artifact.txt",
                "upstream/",
                false,
                false,
                true
        );
        ca2.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca2);
        RunSelector ca3 = RunSelectorUtil.createRunSelector(
                intermediate.getName(),
                "",
                new TriggeredRunSelector(true, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, allowUpstreamDependencies),
                "artifact.txt",
                "intermediate/",
                false,
                false,
                true
        );
        ca3.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca3);
        downstream.getPublishersList().add(new ArtifactArchiver("upstream/artifact.txt,intermediate/artifact.txt", "", false, false));
        downstream.setQuietPeriod(0);
        
        upstream.save();
        intermediate.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();

        // First (initial) build for each job
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "upstreamValue1"))));
        
        j.waitUntilNoActivity();

        // Trigger directly an 'intermediate#2' build, which depends on 'upstream#1'
        intermediate.scheduleBuild2(0, new Cause.UserCause()).waitForStart();

        // 'intermediate#2' build is running. Meanwhile, a new 'upstream#2' is completing and triggers 'intermediate#3':
        upstream.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(new StringParameterValue("CONTENT", "upstreamValue2")));
        
        j.waitUntilNoActivity();

        assertEquals("Number of upstream builds", 2, upstream.getBuilds().size());
        assertEquals("Number of intermediate builds", 3, intermediate.getBuilds().size());
        assertEquals("Number of downstream builds", 3, downstream.getBuilds().size());

        // Get the 'downstream#2' build ...
        FreeStyleBuild downstreamBuild2 = downstream.getBuildByNumber(2);
        assertNotNull(downstreamBuild2);
        j.assertBuildStatusSuccess(downstreamBuild2);
        // ... that were triggered by the directly triggered 'intermediate#2' build.
        Cause.UpstreamCause cause = downstreamBuild2.getCause(Cause.UpstreamCause.class);
        assertNotNull(cause);
        assertEquals("intermediate #2", cause.getUpstreamRun().getFullDisplayName());

        // Return artifacts from downstream#2. One of them was copied from 'upstream' that is
        // - either the last successful upstream#2 build
        // - or upstream#1, which is the same that the triggering intermediate#2 depends on
        // depending on the value of allowUpstreamDependencies
        String artifactFromUpstream = FileUtils.readFileToString(new File(downstreamBuild2.getArtifactsDir(), "upstream/artifact.txt"), "UTF-8");
        String artifactFromIntermediate = FileUtils.readFileToString(new File(downstreamBuild2.getArtifactsDir(), "intermediate/artifact.txt"), "UTF-8");

        return artifactFromIntermediate + "," + artifactFromUpstream;
        */
        return null;
    }

    @Test
    public void testTryUpstreamBuildDisabled() throws Exception {
        assertEquals("upstreamValue1,upstreamValue2", getDownstreamAfterOverlappingFlow(false));
    }

    @Test
    public void testTryUpstreamBuildEnabled() throws Exception {
        assertEquals("upstreamValue1,upstreamValue1", getDownstreamAfterOverlappingFlow(true));
    }

/*
    @Issue("JENKINS-18804")
    @Test
    public void testUpstreamWasRemoved() throws Exception {
        // upstream -> downstream
        // Remove the build of upstream.
        {
            FreeStyleProject upstream = j.createFreeStyleProject();
            FreeStyleProject downstream = j.createFreeStyleProject();
            
            upstream.getBuildersList().add(new FileWriteBuilder(
                    "artifact.txt",
                    "foobar"
            ));
            upstream.getPublishersList().add(new ArtifactArchiver(
                    "**\/*",
                    "",
                    false,
                    false
            ));
            upstream.getPublishersList().add(new BuildTrigger(
                    downstream.getFullName(),
                    false
            ));
            
            downstream.getBuildersList().add(new RemoveUpstreamBuilder());
            RunSelector ca = RunSelectorUtil.createRunSelector(
                    upstream.getFullName(),
                    "",
                    new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false),
                    "**\/*",
                    "",
                    "",
                    false,
                    true, // This results build succeed even if the upstream build has been removed.
                    false
            );
            ca.upgradeFromCopyartifact10();
            downstream.getBuildersList().add(ca);
            
            j.jenkins.rebuildDependencyGraph();
            
            upstream.scheduleBuild2(0);
            j.waitUntilNoActivity();
            
            assertNull(upstream.getLastBuild());
            j.assertBuildStatusSuccess(downstream.getLastBuild());
        }
        
        // upstream -> intermediate -> downstream
        // Remove the build of upstream.
        {
            FreeStyleProject upstream = j.createFreeStyleProject();
            FreeStyleProject intermediate = j.createFreeStyleProject();
            FreeStyleProject downstream = j.createFreeStyleProject();
            
            upstream.getBuildersList().add(new FileWriteBuilder(
                    "artifact.txt",
                    "foobar"
            ));
            upstream.getPublishersList().add(new ArtifactArchiver(
                    "**\/*",
                    "",
                    false,
                    false
            ));
            upstream.getPublishersList().add(new BuildTrigger(
                    intermediate.getFullName(),
                    false
            ));
            
            intermediate.getBuildersList().add(new RemoveUpstreamBuilder());
            intermediate.getPublishersList().add(new BuildTrigger(
                    downstream.getFullName(),
                    false
            ));
            
            RunSelector ca = RunSelectorUtil.createRunSelector(
                    upstream.getFullName(),
                    "",
                    new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false),
                    "**\/*",
                    "",
                    "",
                    false,
                    true, // This results build succeed even if the upstream build has been removed.
                    false
            );
            ca.upgradeFromCopyartifact10();
            downstream.getBuildersList().add(ca);
            
            j.jenkins.rebuildDependencyGraph();
            
            upstream.scheduleBuild2(0);
            j.waitUntilNoActivity();
            
            assertNull(upstream.getLastBuild());
            j.assertBuildStatusSuccess(intermediate.getLastBuild());
            j.assertBuildStatusSuccess(downstream.getLastBuild());
        }
        
        // upstream -> intermediate -> downstream
        // Remove the build of intermediate.
        {
            FreeStyleProject upstream = j.createFreeStyleProject();
            FreeStyleProject intermediate = j.createFreeStyleProject();
            FreeStyleProject downstream = j.createFreeStyleProject();
            
            upstream.getBuildersList().add(new FileWriteBuilder(
                    "artifact.txt",
                    "foobar"
            ));
            upstream.getPublishersList().add(new ArtifactArchiver(
                    "**\/*",
                    "",
                    false,
                    false
            ));
            upstream.getPublishersList().add(new BuildTrigger(
                    intermediate.getFullName(),
                    false
            ));
            
            intermediate.getPublishersList().add(new BuildTrigger(
                    downstream.getFullName(),
                    false
            ));
            
            downstream.getBuildersList().add(new RemoveUpstreamBuilder());
            RunSelector ca = RunSelectorUtil.createRunSelector(
                    upstream.getFullName(),
                    "",
                    new TriggeredRunSelector(false, TriggeredRunSelector.UpstreamFilterStrategy.UseGlobalSetting, false),
                    "**\/*",
                    "",
                    "",
                    false,
                    true, // This results build succeed even if the upstream build has been removed.
                    false
            );
            ca.upgradeFromCopyartifact10();
            downstream.getBuildersList().add(ca);
            
            j.jenkins.rebuildDependencyGraph();
            
            upstream.scheduleBuild2(0);
            j.waitUntilNoActivity();
            
            j.assertBuildStatusSuccess(upstream.getLastBuild());
            assertNull(intermediate.getLastBuild());
            j.assertBuildStatusSuccess(downstream.getLastBuild());
        }
    }
*/

/*
    @Issue("JENKINS-14653")
    @Test
    public void testMavenModule() throws Exception {
        ToolInstallations.configureDefaultMaven();
        MavenModuleSet upstream = createMavenProject();
        FreeStyleProject downstream = j.createFreeStyleProject();
        
        upstream.setGoals("clean package");
        upstream.setScm(new ExtractResourceSCM(getClass().getResource("maven-job.zip")));
        upstream.getPublishersList().add(new BuildTrigger(downstream.getName(), Result.SUCCESS));
        
        RunSelector ca = RunSelectorUtil.createRunSelector(
                String.format("%s/org.jvnet.hudson.main.test.multimod$moduleB", upstream.getName()),
                "",
                new TriggeredRunSelector(false, null, false),
                "**\/*",
                "",
                "",
                false,
                false,
                false
        );
        ca.upgradeFromCopyartifact10();
        downstream.getBuildersList().add(ca);
        
        upstream.save();
        downstream.save();
        j.jenkins.rebuildDependencyGraph();
        
        j.assertBuildStatusSuccess(upstream.scheduleBuild2(0));
        j.waitUntilNoActivity();
        
        FreeStyleBuild b = downstream.getLastBuild();
        j.assertBuildStatusSuccess(b);
        
        assertTrue(
                String.format(
                        "File not found: files are: %s",
                        Arrays.asList(b.getWorkspace().list("**\/*"))
                ),
                b.getWorkspace().child("org.jvnet.hudson.main.test.multimod/moduleB/1.0-SNAPSHOT/moduleB-1.0-SNAPSHOT.jar").exists()
        );
    }
*/

    /**
     * Creates an empty Maven project with an unique name.
     *
     * @return an empty Maven project with an unique name.
     */
    private MavenModuleSet createMavenProject() throws IOException {
        MavenModuleSet mavenModuleSet = j.jenkins.createProject(MavenModuleSet.class, "test"+j.jenkins.getItems().size());
        mavenModuleSet.setRunHeadless(true);
        return mavenModuleSet;
    }

}
