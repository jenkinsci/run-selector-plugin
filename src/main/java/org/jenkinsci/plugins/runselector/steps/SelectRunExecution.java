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
 * The execution of {@link SelectRunStep}.
 *
 * @author Alexandru Somai
 * @since 1.0
 */
public class SelectRunExecution extends AbstractSynchronousStepExecution<RunWrapper> {

    private static final long serialVersionUID = 1L;

    private static final RunSelector DEFAULT_RUN_SELECTOR = new StatusRunSelector();
    private static final RunFilter DEFAULT_RUN_FILTER = new NoRunFilter();

    @Inject
    private transient SelectRunStep step;

    @StepContextParameter
    private transient Run<?, ?> run;
    @StepContextParameter
    private transient TaskListener listener;

    @Override
    public RunWrapper run() throws Exception {

        String jobName = step.getJob();
        if (jobName == null) {
            throw new AbortException(Messages.SelectRunStep_MissingJobParameter());
        }

        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not been started, or was already shut down");
        }
        Job<?, ?> upstreamJob = jenkins.getItem(jobName, run.getParent(), Job.class);
        if (upstreamJob == null) {
            throw new AbortException(Messages.SelectRunStep_MissingJob(jobName));
        }

        RunSelector selector = step.getSelector();
        if (selector == null) {
            listener.getLogger().println(Messages.SelectRunStep_MissingRunSelector(DEFAULT_RUN_SELECTOR.getDisplayName()));
            selector = DEFAULT_RUN_SELECTOR;
        }

        RunFilter filter = step.getFilter();
        if (filter == null) {
            listener.getLogger().println(Messages.SelectRunStep_MissingRunFilter());
            filter = DEFAULT_RUN_FILTER;
        }

        RunSelectorContext context = new RunSelectorContext(jenkins, run, listener, filter);
        context.setVerbose(step.isVerbose());

        Run<?, ?> upstreamRun = selector.select(upstreamJob, context);
        if (upstreamRun == null) {
            throw new AbortException(Messages.SelectRunStep_MissingRun(jobName, selector.getDisplayName(), filter.getDisplayName()));
        }

        return new RunWrapper(upstreamRun, false);
    }
}
