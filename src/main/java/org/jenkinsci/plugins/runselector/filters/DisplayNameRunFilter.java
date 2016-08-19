package org.jenkinsci.plugins.runselector.filters;

import hudson.Extension;
import hudson.Util;
import hudson.model.Run;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunFilterDescriptor;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * Filters the build based on its display name.
 *
 * @author Alexandru Somai
 */
public class DisplayNameRunFilter extends RunFilter {

    @Nonnull
    private String runDisplayName;

    @DataBoundConstructor
    public DisplayNameRunFilter(String runDisplayName) {
        this.runDisplayName = Util.fixNull(runDisplayName).trim();
    }

    @Nonnull
    public String getRunDisplayName() {
        return runDisplayName;
    }

    @Override
    public boolean isSelectable(@Nonnull Run<?, ?> candidate, @Nonnull RunSelectorContext context) {
        String resolvedDisplayName = context.getEnvVars().expand(runDisplayName);
        if (resolvedDisplayName.startsWith("$")) {
            context.logDebug("Unresolved variable {0}", resolvedDisplayName);
            return false;
        }

        return resolvedDisplayName.equals(candidate.getDisplayName());
    }

    @Symbol("displayName")
    @Extension
    public static class DescriptorImpl extends RunFilterDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.DisplayNameRunFilter_DisplayName();
        }
    }
}
