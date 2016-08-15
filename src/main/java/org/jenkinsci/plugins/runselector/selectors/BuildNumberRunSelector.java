package org.jenkinsci.plugins.runselector.selectors;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.runselector.RunSelectorDescriptor;
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

    @DataBoundConstructor
    public BuildNumberRunSelector(String buildNumber) {
        super(buildNumber);
    }

    @Nonnull
    public String getBuildNumber() {
        return getParameter();
    }

    @Override
    @CheckForNull
    public Run<?, ?> getBuild(@Nonnull Job<?, ?> job, @Nonnull String resolvedParameter) throws IOException {
        try {
            return job.getBuildByNumber(Integer.parseInt(resolvedParameter));
        } catch (NumberFormatException e) {
            throw new AbortException(Messages.BuildNumberRunSelector_NotANumber(resolvedParameter));
        }
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
