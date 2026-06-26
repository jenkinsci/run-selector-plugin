/*
 * The MIT License
 *
 * Copyright (c) 2011, Alan Harder
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
package org.jenkinsci.plugins.runselector;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jenkinsci.plugins.runselector.filters.NoRunFilter;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Additional filters used by {@link RunSelector}.
 * Use {@link RunFilterDescriptor} for its descriptor.
 * @author Alan Harder
 */
public abstract class RunFilter extends AbstractDescribableImpl<RunFilter> implements ExtensionPoint {

    /**
     * @param candidate the build to check
     * @param context the context of current runselector execution.
     * @return whether this build can be selected.
     */
    public boolean isSelectable(@NonNull Run<?, ?> candidate, @NonNull RunSelectorContext context) {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RunFilterDescriptor getDescriptor() {
        return (RunFilterDescriptor)super.getDescriptor();
    }
    
    /**
     * @return all descriptors of {@link RunFilter} without {@link NoRunFilter}
     */
    public static List<RunFilterDescriptor> all() {
        Jenkins j = Jenkins.get();
        return j.getDescriptorList(RunFilter.class).stream()
                .map(d -> (RunFilterDescriptor) d)
                .collect(Collectors.toList());
    }
    
    /**
     * @return all descriptors of {@link RunFilter} including {@link NoRunFilter}
     */
    public static List<RunFilterDescriptor> allWithNoRunFilter() {
        List<RunFilterDescriptor> allFilters = new ArrayList<RunFilterDescriptor>(all());
        allFilters.add(0, NoRunFilter.DESCRIPTOR);
        
        return allFilters;
    }
    
    /**
     * Returns the display name for this filters.
     * You can override this to output configurations of this filters
     * in verbose logs.
     * 
     * @return the display name for this filters.
     */
    public String getDisplayName() {
        try {
            return getDescriptor().getDisplayName();
        } catch (AssertionError e) {
            // getDescriptor throws AssertionException
            // if there's no descriptor available
            // (e.g. selectors in unit tests)
            return getClass().getName();
        }
    }
}
