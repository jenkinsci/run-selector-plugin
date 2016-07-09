/*
 * The MIT License
 * 
 * Copyright (c) 2015 IKEDA Yasuyuki
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

package org.jenkinsci.plugins.runselector.filters;

import hudson.Extension;
import hudson.model.Run;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.context.RunSelectorPickContext;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Accepts a build only when every underlying filters accepts it.
 */
public class AndRunFilter extends RunFilter {
    @Nonnull
    private final List<RunFilter> runFilterList;
    
    /**
     * @param runFilterList run filter to conjunct
     */
    @DataBoundConstructor
    public AndRunFilter(@Nonnull List<RunFilter> runFilterList) {
        this.runFilterList = runFilterList;
    }
    
    /**
     * Convenient constructor.
     * 
     * @param runFilters run filter to conjunct
     */
    public AndRunFilter(@Nonnull RunFilter... runFilters) {
        this(Arrays.asList(runFilters));
    }
    
    /**
     * @return run filter to conjunct
     */
    @Nonnull
    public List<RunFilter> getRunFilterList() {
        return runFilterList;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectable(Run<?, ?> candidate, RunSelectorPickContext context) {
        for (RunFilter filter: getRunFilterList()) {
            if (!filter.isSelectable(candidate, context)) {
                context.logDebug(
                        "{0}: declined by the filters {1} (in {2})",
                        candidate.getFullDisplayName(),
                        filter.getDisplayName(),
                        getDisplayName()
                );
                return false;
            }
        }
        return true;
    }
    
    /**
     * the descriptor for {@link AndRunFilter}
     */
    @Extension(ordinal=-100)    // bottom most
    public static class DescriptorImpl extends RunFilterDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.AndRunFilter_DisplayName();
        }
    }
}
