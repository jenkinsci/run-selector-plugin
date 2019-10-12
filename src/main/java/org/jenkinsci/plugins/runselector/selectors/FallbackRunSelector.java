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

package org.jenkinsci.plugins.runselector.selectors;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunFilterDescriptor;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.RunSelectorDescriptor;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.runselector.filters.AndRunFilter;
import org.jenkinsci.plugins.runselector.filters.NoRunFilter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Tries multiple selectors consequently.
 */
public class FallbackRunSelector extends RunSelector {
    /**
     * An entry for {@link FallbackRunSelector}
     */
    public static class Entry extends AbstractDescribableImpl<Entry>{
        @Nonnull
        private final RunSelector runSelector;
        
        @Nonnull
        private final RunFilter runFilter;
        
        /**
         * @param runSelector run selector
         * @param runFilter   run filter used with the run selector
         */
        @DataBoundConstructor
        public Entry(@Nonnull RunSelector runSelector, @Nonnull RunFilter runFilter) {
            this.runSelector = runSelector;
            this.runFilter = runFilter;
        }
        
        /**
         * @param runSelector run selector
         */
        public Entry(@Nonnull RunSelector runSelector) {
            this(runSelector, new NoRunFilter());
        }
        
        /**
         * @return run selector
         */
        public RunSelector getRunSelector() {
            return runSelector;
        }
        
        /**
         * @return run filter
         */
        public RunFilter getRunFilter() {
            return runFilter;
        }
        
        /**
         * the descriptor for {@link FallbackRunSelector}
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<Entry> {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return Messages.FallbackRunSelector_Entry_DisplayName();
            }
            
            /**
             * @return descriptors of all {@link RunSelector} except {@link FallbackRunSelector}
             */
            public Iterable<? extends Descriptor<? extends RunSelector>> getRunSelectorDescriptorList() {
                Jenkins jenkins = Jenkins.getInstance();
                // remove FallbackRunSelector itself.
                return Iterables.filter(
                        jenkins.getDescriptorList(RunSelector.class),
                        new Predicate<Descriptor<? extends RunSelector>>() {
                            @Override
                            public boolean apply(Descriptor<? extends RunSelector> d) {
                                return !FallbackRunSelector.class.isAssignableFrom(d.clazz);
                            }
                        }
                );
            }
            
            /**
             * @return descriptors for all {@link RunFilter}s.
             */
            public List<RunFilterDescriptor> getRunFilterDescriptorList() {
                return RunFilter.allWithNoRunFilter();
            }
        }
        
    }
    
    @Nonnull
    private final List<Entry> entryList;
    
    /**
     * @param entryList run selector to try
     */
    @DataBoundConstructor
    public FallbackRunSelector(@Nonnull List<Entry> entryList) {
        this.entryList = entryList;
    }

    /**
     * Convenient constructor.
     * 
     * @param runSelectors run selector to try
     */
    public FallbackRunSelector(@Nonnull RunSelector... runSelectors) {
        this(Lists.transform(
                Arrays.asList(runSelectors),
                new Function<RunSelector, Entry>() {
                    @Override
                    public Entry apply(RunSelector runSelector) {
                        return new Entry(runSelector);
                    }
                }
        ));
    }

    /**
     * @return run selector to try
     */
    public List<Entry> getEntryList() {
        return entryList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Run<?, ?> select(@Nonnull Job<?, ?> job, @Nonnull RunSelectorContext context)
            throws IOException, InterruptedException
    {
        for (Entry entry : getEntryList()) {
            RunSelectorContext childContext = context.clone();
            if (entry.getRunFilter() instanceof NoRunFilter) {
                // nothing to do.
            } else if (context.getRunFilter() instanceof NoRunFilter) {
                childContext.setRunFilter(entry.getRunFilter());
            } else {
                // RunFilters are provided both in context and this selectors.
                // Merge them.
                childContext.setRunFilter(new AndRunFilter(Arrays.asList(
                        childContext.getRunFilter()
                        , entry.getRunFilter()
                )));
            }
            // Ensure this is the first match.
            childContext.setLastMatchBuild(null);
            
            context.logDebug("Try {0}", entry.getRunSelector().getDisplayName());
            Run<?, ?> candidate = entry.getRunSelector().select(job, childContext);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    @Symbol("fallback")
    @Extension(ordinal = -100)    // bottom most
    public static class DescriptorImpl extends RunSelectorDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.FallbackRunSelector_DisplayName();
        }
    }
}
