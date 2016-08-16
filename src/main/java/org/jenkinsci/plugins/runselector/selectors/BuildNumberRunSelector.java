package org.jenkinsci.plugins.runselector.selectors;

import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.runselector.RunSelectorDescriptor;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;


/**
 * Select the run based on its build number.
 *
 * @author Alexandru Somai
 */
public class BuildNumberRunSelector extends AbstractSpecificRunSelector {

    @Nonnull
    private final String buildNumber;

    @DataBoundConstructor
    public BuildNumberRunSelector(String buildNumber) {
        this.buildNumber = Util.fixNull(buildNumber).trim();
    }

    @Nonnull
    public String getBuildNumber() {
        return buildNumber;
    }

    @Override
    @CheckForNull
    public Run<?, ?> getBuild(@Nonnull Job<?, ?> job, @Nonnull RunSelectorContext context) throws IOException {
        String resolvedBuildNumber = context.getEnvVars().expand(buildNumber);
        if (resolvedBuildNumber.startsWith("$")) {
            context.logDebug("Unresolved variable {0}", resolvedBuildNumber);
            return null;
        }

        Run<?, ?> run;
        try {
            run = job.getBuildByNumber(Integer.parseInt(resolvedBuildNumber));
        } catch (NumberFormatException e) {
            throw new AbortException(Messages.BuildNumberRunSelector_NotANumber(resolvedBuildNumber));
        }

        if (run == null) {
            context.logDebug("No such build {0} in {1}", buildNumber, job.getFullName());
            return null;
        }

        return run;
    }

    @Symbol("buildNumber")
    @Extension
    public static class DescriptorImpl extends RunSelectorDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.BuildNumberRunSelector_DisplayName();
        }
    }
}
