package org.jenkinsci.plugins.runselector.steps;

import hudson.Extension;
import hudson.Util;
import org.jenkinsci.plugins.runselector.Messages;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;

/**
 * The runSelector step selects a specific run from a given project name based on the given selector
 * and, optionally, the run filter.
 *
 * @author Alexandru Somai
 */
public class RunSelectorStep extends AbstractStepImpl {

    @CheckForNull
    private final String projectName;

    private boolean verbose;

    @CheckForNull
    private RunSelector selector;

    @CheckForNull
    private RunFilter runFilter;

    @DataBoundConstructor
    public RunSelectorStep(String projectName) {
        this.projectName = Util.fixEmptyAndTrim(projectName);
    }

    @CheckForNull
    public String getProjectName() {
        return projectName;
    }

    @CheckForNull
    public RunSelector getSelector() {
        return selector;
    }

    @DataBoundSetter
    public void setSelector(RunSelector selector) {
        this.selector = selector;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @DataBoundSetter
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @CheckForNull
    public RunFilter getRunFilter() {
        return runFilter;
    }

    @DataBoundSetter
    public void setRunFilter(RunFilter runFilter) {
        this.runFilter = runFilter;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(RunSelectorExecution.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.RunSelectorStep_DisplayName();
        }

        @Override
        public String getFunctionName() {
            return "runSelector";
        }

        @Override
        public String getHelpFile(String fieldName) {
            if ("selector".equals(fieldName) || "runFilter".equals(fieldName) || "verbose".equals(fieldName)) {
                return "/plugin/run-selector/help-" + fieldName + ".html";
            }
            return super.getHelpFile(fieldName);
        }
    }
}
