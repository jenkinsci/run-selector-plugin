package org.jenkinsci.plugins.runselector.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.runselector.Messages;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.runselector.filters.NoRunFilter;
import org.jenkinsci.plugins.runselector.selectors.StatusRunSelector;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;

/**
 * The execution of {@link RunSelectorStep}.
 *
 * @author Alexandru Somai
 */
public class RunSelectorExecution extends AbstractSynchronousStepExecution<RunWrapper> {

    private static final RunSelector DEFAULT_RUN_SELECTOR = new StatusRunSelector();
    private static final RunFilter DEFAULT_RUN_FILTER = new NoRunFilter();

    @Inject
    private transient RunSelectorStep step;

    @StepContextParameter
    private transient Run<?, ?> run;
    @StepContextParameter
    private transient TaskListener listener;

    @Override
    public RunWrapper run() throws Exception {

        String projectName = step.getProjectName();
        if (projectName == null) {
            throw new AbortException(Messages.RunSelectorStep_MissingProjectName());
        }

        Jenkins jenkins = Jenkins.getActiveInstance();
        Job<?, ?> upstreamJob = jenkins.getItem(projectName, run.getParent(), Job.class);
        if (upstreamJob == null) {
            throw new AbortException(Messages.RunSelectorStep_MissingProject(projectName));
        }

        RunSelector selector = step.getSelector();
        if (selector == null) {
            listener.getLogger().println(Messages.RunSelectorStep_MissingRunSelector(DEFAULT_RUN_SELECTOR.getDisplayName()));
            selector = DEFAULT_RUN_SELECTOR;
        }

        RunFilter runFilter = step.getRunFilter();
        if (runFilter == null) {
            listener.getLogger().println(Messages.RunSelectorStep_MissingRunFilter());
            runFilter = DEFAULT_RUN_FILTER;
        }

        RunSelectorContext context = new RunSelectorContext(jenkins, run, listener, runFilter);
        context.setVerbose(step.isVerbose());

        Run<?, ?> upstreamRun = selector.select(upstreamJob, context);
        if (upstreamRun == null) {
            throw new AbortException(Messages.RunSelectorStep_MissingRun(projectName, selector.getDisplayName(), runFilter.getDisplayName()));
        }

        return new RunWrapper(upstreamRun, false);
    }
}
