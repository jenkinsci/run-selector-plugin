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
 * The selectRun step selects a specific run from a given project name based on the given selector
 * and, optionally, the run filter.
 *
 * @author Alexandru Somai
 * @since 1.0
 */
public class SelectRunStep extends AbstractStepImpl {

    @CheckForNull
    private final String job;

    private boolean verbose;

    @CheckForNull
    private RunSelector selector;

    @CheckForNull
    private RunFilter filter;

    @DataBoundConstructor
    public SelectRunStep(String job) {
        this.job = Util.fixEmptyAndTrim(job);
    }

    @CheckForNull
    public String getJob() {
        return job;
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
    public RunFilter getFilter() {
        return filter;
    }

    @DataBoundSetter
    public void setFilter(RunFilter filter) {
        this.filter = filter;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(SelectRunExecution.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.SelectRunStep_DisplayName();
        }

        @Override
        public String getFunctionName() {
            return "selectRun";
        }

        @Override
        public String getHelpFile(String fieldName) {
            if ("selector".equals(fieldName) || "filter".equals(fieldName) || "verbose".equals(fieldName)) {
                return "/plugin/run-selector/help-" + fieldName + ".html";
            }
            return super.getHelpFile(fieldName);
        }
    }
}
