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

package org.jenkinsci.plugins.runselector;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.jvnet.hudson.test.JenkinsRule;

//import hudson.plugins.runselector.RunSelector.RunSelectorPickResult;
//import hudson.plugins.runselector.operation.RunSelectorFiles;
//import hudson.plugins.runselector.operation.CopyWorkspaceFiles;
//import hudson.plugins.runselector.selectors.Version1RunSelector;
//import hudson.plugins.runselector.selectors.Version1RunSelector.MigratedConfiguration;


/**
 * Tests mainly for features introduced since 2.0
 */
@Ignore
public class CopyArtifact20Test {
    @ClassRule
    public final static JenkinsRule j = new JenkinsRule();
/*
    @SuppressWarnings("deprecation")
    private static class DummyVersion1RunSelector extends Version1RunSelector {
        private final MigratedConfiguration conf;

        public DummyVersion1RunSelector(
                @Nonnull RunSelector runSelector,
                @CheckForNull RunFilter runFilter,
                @CheckForNull RunSelectorOperation copyArtifactOperation
        ) {
            this.conf = new MigratedConfiguration(runSelector, runFilter);
            this.conf.copyArtifactOperation = copyArtifactOperation;
        }
        @Override
        public MigratedConfiguration migrateToVersion2() {
            return conf;
        }
    }

    @Test
    public void testUpgradeFromRunSelector10NotApplied() throws Exception {
        RunSelector ca = new RunSelector("test");
        ca.setSelector(new StatusRunSelector());
        assertFalse(ca.upgradeFromCopyartifact10());
    }

    @Test
    public void testUpgradeFromRunSelector10Applied() throws Exception {
        RunSelector ca = new RunSelector("test");
        RunSelector selectors = new StatusRunSelector();
        ca.setSelector(new DummyVersion1RunSelector(
                selectors,
                null,
                null
        ));
        assertTrue(ca.upgradeFromCopyartifact10());
        j.assertEqualDataBoundBeans(selectors, ca.getSelector());
    }

    @Test
    public void testUpgradeFromRunSelector10NullRunFilter() throws Exception {
        RunSelector ca = new RunSelector("test");
        NoRunFilter filters = new NoRunFilter();
        ca.setRunFilter(filters);
        ca.setSelector(new DummyVersion1RunSelector(
                new StatusRunSelector(),
                null,
                null
        ));
        assertTrue(ca.upgradeFromCopyartifact10());
        j.assertEqualDataBoundBeans(filters, ca.getRunFilter());
    }

    @Test
    public void testUpgradeFromRunSelector10NoRunFilter() throws Exception {
        RunSelector ca = new RunSelector("test");
        NoRunFilter filters = new NoRunFilter();
        ca.setRunFilter(filters);
        ca.setSelector(new DummyVersion1RunSelector(
                new StatusRunSelector(),
                new NoRunFilter(),
                null
        ));
        assertTrue(ca.upgradeFromCopyartifact10());
        j.assertEqualDataBoundBeans(filters, ca.getRunFilter());
    }

    @Test
    public void testUpgradeFromRunSelector10ReplaceRunFilter() throws Exception {
        RunSelector ca = new RunSelector("test");
        ParametersRunFilter filters = new ParametersRunFilter("param=value");
        ca.setSelector(new DummyVersion1RunSelector(
                new StatusRunSelector(),
                filters,
                null
        ));
        assertTrue(ca.upgradeFromCopyartifact10());
        j.assertEqualDataBoundBeans(filters, ca.getRunFilter());
    }

    @Test
    public void testUpgradeFromRunSelector10MergeRunFilter() throws Exception {
        RunSelector ca = new RunSelector("test");
        ParametersRunFilter filter1 = new ParametersRunFilter("param1=value1");
        ParameterizedRunFilter filter2 = new ParameterizedRunFilter("${PARAM}");
        ca.setRunFilter(filter1);
        ca.setSelector(new DummyVersion1RunSelector(
                new StatusRunSelector(),
                filter2,
                null
        ));
        assertTrue(ca.upgradeFromCopyartifact10());

        assertEquals(AndRunFilter.class, ca.getRunFilter().getClass());

        // sort elements with class hashes to ensure its order.
        Comparator<RunFilter> c = new Comparator<RunFilter>() {
            @Override
            public int compare(RunFilter o1, RunFilter o2) {
                return o1.getClass().hashCode() - o2.getClass().hashCode();
            }
        };
        List<RunFilter> expected = new ArrayList<RunFilter>();
        expected.add(filter1);
        expected.add(filter2);
        expected.sort(c);

        List<RunFilter> actual = new ArrayList<RunFilter>(((AndRunFilter)(ca.getRunFilter())).getRunFilterList());
        actual.sort(c);

        j.assertEqualDataBoundBeans(expected, actual);
    }

    @Test
    public void testUpgradeFromRunSelector10NullOperation() throws Exception {
        RunSelector ca = new RunSelector("test");
        RunSelectorFiles operation = new RunSelectorFiles();
        operation.setIncludes("artifact.txt");
        ca.setOperation(operation);

        ca.setSelector(new DummyVersion1RunSelector(
                new StatusRunSelector(),
                null,
                null
        ));
        assertTrue(ca.upgradeFromCopyartifact10());
        j.assertEqualDataBoundBeans(operation, ca.getOperation());
    }

    public static class DummyRunSelectorOperation extends RunSelectorOperation {
        private final String dummyParam;

        @DataBoundConstructor
        public DummyRunSelectorOperation(String dummyParam) {
            this.dummyParam = dummyParam;
        }

        public String getDummyParam() {
            return dummyParam;
        }

        @Override
        public Result perform(Run<?, ?> src, RunSelectorOperationContext context) throws IOException, InterruptedException {
            // do nothing
            return null;
        }
    }

    @Test
    public void testUpgradeFromRunSelector10ReplaceOperation() throws Exception {
        RunSelector ca = new RunSelector("test");
        RunSelectorFiles operation = new RunSelectorFiles();
        operation.setIncludes("artifact.txt");
        ca.setOperation(operation);

        DummyRunSelectorOperation newOperation = new DummyRunSelectorOperation("test");
        ca.setSelector(new DummyVersion1RunSelector(
                new StatusRunSelector(),
                null,
                newOperation
        ));
        assertTrue(ca.upgradeFromCopyartifact10());
        j.assertEqualDataBoundBeans(newOperation, ca.getOperation());
    }

    @Test
    public void testUpgradeFromRunSelector10MergeOperation() throws Exception {
        RunSelector ca = new RunSelector("test");
        RunSelectorFiles operation = new RunSelectorFiles();
        operation.setIncludes("artifact.txt");
        ca.setOperation(operation);

        CopyWorkspaceFiles newOperation = new CopyWorkspaceFiles();
        ca.setSelector(new DummyVersion1RunSelector(
                new StatusRunSelector(),
                null,
                newOperation
        ));
        assertTrue(ca.upgradeFromCopyartifact10());

        assertEquals(CopyWorkspaceFiles.class, ca.getOperation().getClass());
        // configuration of operation is populated to newOperation
        assertEquals(operation.getIncludes(), ((CopyWorkspaceFiles)ca.getOperation()).getIncludes());
    }

    @Test
    public void testPickBuildToCopyFromBuildFound() throws Exception {
        FreeStyleProject copier = j.createFreeStyleProject();
        FreeStyleProject copiee = j.createFreeStyleProject();
        FreeStyleBuild copierBuild = j.buildAndAssertSuccess(copier);
        FreeStyleBuild copieeBuild = j.buildAndAssertSuccess(copiee);

        RunSelectorContext context = new RunSelectorContext();
        context.setJenkins(j.jenkins);
        context.setCurrentRun(copierBuild);
        context.setListener(TaskListener.NULL);
        context.setEnvVars(new EnvVars());
        context.setVerbose(false);

        context.setProjectName(copiee.getFullName());
        context.setRunFilter(new NoRunFilter());

        SpecificRunSelector selectors = new SpecificRunSelector(Integer.toString(copieeBuild.getNumber()));

        RunSelector ca = new RunSelector(copiee.getFullName());

        RunSelectorPickResult r = ca.select(selectors, context);
        assertEquals(RunSelectorPickResult.Result.Found, r.result);
        assertEquals(copiee.getFullName(), r.getJob().getFullName());
        assertEquals(copieeBuild.getId(), r.getBuild().getId());
    }

    @Test
    public void testPickBuildToCopyFromBuildNotFound() throws Exception {
        FreeStyleProject copier = j.createFreeStyleProject();
        FreeStyleProject copiee = j.createFreeStyleProject();
        FreeStyleBuild copierBuild = j.buildAndAssertSuccess(copier);

        RunSelectorContext context = new RunSelectorContext();
        context.setJenkins(j.jenkins);
        context.setCurrentRun(copierBuild);
        context.setListener(TaskListener.NULL);
        context.setEnvVars(new EnvVars());
        context.setVerbose(false);

        context.setProjectName(copiee.getFullName());
        context.setRunFilter(new NoRunFilter());

        SpecificRunSelector selectors = new SpecificRunSelector("nosuchbuild");

        RunSelector ca = new RunSelector(copiee.getFullName());

        RunSelectorPickResult r = ca.select(selectors, context);
        assertEquals(RunSelectorPickResult.Result.BuildNotFound, r.result);
        assertEquals(copiee.getFullName(), r.getJob().getFullName());
    }

    @Test
    public void testPickBuildToCopyFromBuildNotFoundInSameFolder() throws Exception {
        MockFolder f = j.createFolder("folder");
        try {
            FreeStyleProject copier = f.createProject(FreeStyleProject.class, "copier");
            FreeStyleProject copiee = f.createProject(FreeStyleProject.class, "copiee");
            FreeStyleBuild copierBuild = j.buildAndAssertSuccess(copier);

            RunSelectorContext context = new RunSelectorContext();
            context.setJenkins(j.jenkins);
            context.setCurrentRun(copierBuild);
            context.setListener(TaskListener.NULL);
            context.setEnvVars(new EnvVars());
            context.setVerbose(false);

            context.setProjectName(copiee.getName());
            context.setRunFilter(new NoRunFilter());

            SpecificRunSelector selectors = new SpecificRunSelector("nosuchbuild");

            RunSelector ca = new RunSelector(copiee.getFullName());

            RunSelectorPickResult r = ca.select(selectors, context);
            assertEquals(RunSelectorPickResult.Result.BuildNotFound, r.result);
            assertEquals(copiee.getFullName(), r.getJob().getFullName());
        } finally {
            f.delete();
        }
    }

    @Test
    public void testPickBuildToCopyFromBuildNotFoundInDifferentFolder() throws Exception {
        MockFolder f1 = j.createFolder("folder1");
        MockFolder f2 = j.createFolder("folder2");
        try {
            FreeStyleProject copier = f1.createProject(FreeStyleProject.class, "copier");
            FreeStyleProject copiee = f2.createProject(FreeStyleProject.class, "copiee");
            FreeStyleBuild copierBuild = j.buildAndAssertSuccess(copier);

            RunSelectorContext context = new RunSelectorContext();
            context.setJenkins(j.jenkins);
            context.setCurrentRun(copierBuild);
            context.setListener(TaskListener.NULL);
            context.setEnvVars(new EnvVars());
            context.setVerbose(false);

            context.setProjectName("../folder2/copiee");
            context.setRunFilter(new NoRunFilter());

            SpecificRunSelector selectors = new SpecificRunSelector("nosuchbuild");

            RunSelector ca = new RunSelector(copiee.getFullName());

            RunSelectorPickResult r = ca.select(selectors, context);
            assertEquals(RunSelectorPickResult.Result.BuildNotFound, r.result);
            assertEquals(copiee.getFullName(), r.getJob().getFullName());
        } finally {
            f1.delete();
            f2.delete();
        }
    }

    @Test
    public void testPickBuildToCopyFromBuildNotFoundInUpperFolder() throws Exception {
        FreeStyleProject copiee = j.createFreeStyleProject();
        MockFolder f = j.createFolder("folder");
        try {
            FreeStyleProject copier = f.createProject(FreeStyleProject.class, "copier");
            FreeStyleBuild copierBuild = j.buildAndAssertSuccess(copier);

            RunSelectorContext context = new RunSelectorContext();
            context.setJenkins(j.jenkins);
            context.setCurrentRun(copierBuild);
            context.setListener(TaskListener.NULL);
            context.setEnvVars(new EnvVars());
            context.setVerbose(false);

            context.setProjectName(String.format("../%s", copiee.getName()));
            context.setRunFilter(new NoRunFilter());

            SpecificRunSelector selectors = new SpecificRunSelector("nosuchbuild");

            RunSelector ca = new RunSelector(copiee.getFullName());

            RunSelectorPickResult r = ca.select(selectors, context);
            assertEquals(RunSelectorPickResult.Result.BuildNotFound, r.result);
            assertEquals(copiee.getFullName(), r.getJob().getFullName());
        } finally {
            f.delete();
        }
    }

    @Test
    public void testPickBuildToCopyFromBuildNotFoundInSubfolder() throws Exception {
        FreeStyleProject copier = j.createFreeStyleProject();
        MockFolder f = j.createFolder("folder");
        try {
            FreeStyleProject copiee = f.createProject(FreeStyleProject.class, "copiee");
            FreeStyleBuild copierBuild = j.buildAndAssertSuccess(copier);

            RunSelectorContext context = new RunSelectorContext();
            context.setJenkins(j.jenkins);
            context.setCurrentRun(copierBuild);
            context.setListener(TaskListener.NULL);
            context.setEnvVars(new EnvVars());
            context.setVerbose(false);

            context.setProjectName(copiee.getFullName());
            context.setRunFilter(new NoRunFilter());

            SpecificRunSelector selectors = new SpecificRunSelector("nosuchbuild");

            RunSelector ca = new RunSelector(copiee.getFullName());

            RunSelectorPickResult r = ca.select(selectors, context);
            assertEquals(RunSelectorPickResult.Result.BuildNotFound, r.result);
            assertEquals(copiee.getFullName(), r.getJob().getFullName());
        } finally {
            f.delete();
        }
    }

    @Test
    public void testPickBuildToCopyFromProjectNotFound() throws Exception {
        FreeStyleProject copier = j.createFreeStyleProject();
        FreeStyleBuild copierBuild = j.buildAndAssertSuccess(copier);

        RunSelectorContext context = new RunSelectorContext();
        context.setJenkins(j.jenkins);
        context.setCurrentRun(copierBuild);
        context.setListener(TaskListener.NULL);
        context.setEnvVars(new EnvVars());
        context.setVerbose(false);

        context.setProjectName("nosuchproject");
        context.setRunFilter(new NoRunFilter());

        SpecificRunSelector selectors = new SpecificRunSelector("nosuchbuild");

        RunSelector ca = new RunSelector("nosuchproject");

        RunSelectorPickResult r = ca.select(selectors, context);
        assertEquals(RunSelectorPickResult.Result.ProjectNotFound, r.result);
    }

    @Test
    public void testPickBuildToCopyFromProjectNotFoundForPermission() throws Exception {
        try {
            j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
            j.jenkins.setAuthorizationStrategy(new GlobalMatrixAuthorizationStrategy());

            FreeStyleProject copier = j.createFreeStyleProject();
            FreeStyleProject copiee = j.createFreeStyleProject();
            FreeStyleBuild copierBuild = j.buildAndAssertSuccess(copier);
            FreeStyleBuild copieeBuild = j.buildAndAssertSuccess(copiee);

            RunSelectorContext context = new RunSelectorContext();
            context.setJenkins(j.jenkins);
            context.setCurrentRun(copierBuild);
            context.setListener(TaskListener.NULL);
            context.setEnvVars(new EnvVars());
            context.setVerbose(false);

            context.setProjectName(copiee.getFullName());
            context.setRunFilter(new NoRunFilter());

            SpecificRunSelector selectors = new SpecificRunSelector(Integer.toString(copieeBuild.getNumber()));

            RunSelector ca = new RunSelector(copiee.getFullName());

            RunSelectorPickResult r = ca.select(selectors, context);
            assertEquals(RunSelectorPickResult.Result.ProjectNotFound, r.result);
        } finally {
            j.jenkins.setSecurityRealm(null);
            j.jenkins.setAuthorizationStrategy(null);
        }
    }*/
}
