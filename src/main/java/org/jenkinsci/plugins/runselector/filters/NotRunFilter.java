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
import org.jenkinsci.plugins.runselector.RunFilterDescriptor;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * Accepts a build when the underlying filters doesn't accept it.
 */
public class NotRunFilter extends RunFilter {
    @Nonnull
    private final RunFilter runFilter;
    
    /**
     * @param runFilter run filter to invert
     */
    @DataBoundConstructor
    public NotRunFilter(@Nonnull RunFilter runFilter) {
        this.runFilter = runFilter;
    }
    
    /**
     * @return run filter to invert
     */
    @Nonnull
    public RunFilter getRunFilter() {
        return runFilter;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectable(Run<?, ?> candidate, RunSelectorContext context) {
        boolean result = getRunFilter().isSelectable(candidate, context);
        context.logDebug(
                "{0}: filters result by {1} is reverted: {2} -> {3}",
                candidate.getFullDisplayName(),
                getRunFilter().getDisplayName(),
                result,
                !result
        );
        return !result;
    }
    
    /**
     * the descriptor for {@link NotRunFilter}
     */
    @Extension(ordinal=-102)    // bottom most
    public static class DescriptorImpl extends RunFilterDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.NotRunFilter_DisplayName();
        }
    }
}
