package org.jenkinsci.plugins.runselector.selectors;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.runselector.RunSelectorDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * Selects the build based on its display name.
 *
 * @author Alexandru Somai
 */
public class DisplayNameRunSelector extends AbstractSpecificRunSelector {

    @DataBoundConstructor
    public DisplayNameRunSelector(String runDisplayName) {
        super(runDisplayName);
    }

    @Nonnull
    public String getRunDisplayName() {
        return getParameter();
    }

    @Override
    public Run<?, ?> getBuild(@Nonnull Job<?, ?> job, @Nonnull String resolvedParameter) {
        for (Run<?, ?> build : job.getBuilds()) {
            if (resolvedParameter.equals(build.getDisplayName())) {
                // First named build found is the right one, going from latest build to oldest
                return build;
            }
        }

        return null;
    }

    @Symbol("displayName")
    @Extension
    public static class DescriptorImpl extends RunSelectorDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.DisplayNameRunSelector_DisplayName();
        }
    }
}
