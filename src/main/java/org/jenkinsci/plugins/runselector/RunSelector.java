/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Alan Harder
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
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.runselector.context.RunSelectorPickContext;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Extension point for enumerating builds to copy artifacts from.
 * Subclasses should override {@link #getNextBuild(Job, RunSelectorPickContext)}.
 * use {@link RunSelectorDescriptor} for its descriptor.
 *
 * @author Alan Harder
 */
public abstract class RunSelector extends AbstractDescribableImpl<RunSelector> implements ExtensionPoint {
    /**
     * @param job       the job to pick a build from.
     * @param context   context for the current execution of runselector.
     * @return  the build matches this selectors and conditions stored in the context.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread interrupts the current thread.
     */
    public Run<?, ?> pickBuildToCopyFrom(@Nonnull Job<?,?> job, @Nonnull final RunSelectorPickContext context)
            throws IOException, InterruptedException
    {
        context.setLastMatchBuild(null);
        while (true) {
            Run<?, ?> candidate = getNextBuild(job, context);
            context.setLastMatchBuild(candidate);
            if (candidate == null) {
                context.logDebug("{0}: No more matching builds.", getDisplayName());
                return null;
            }
            context.logDebug("{0}: {1} found", getDisplayName(), candidate.getDisplayName());
            RunFilter filter = context.getRunFilter();
            if (!filter.isSelectable(candidate, context)) {
                context.logDebug(
                        "{0}: declined by the filter {1}",
                        candidate.getFullDisplayName(),
                        filter.getDisplayName()
                );
                continue;
            }
            context.logDebug("{0}: satisfied conditions.", candidate.getFullDisplayName());
            return candidate;
        }
    }

    /**
     * Override this method to implement {@link RunSelector}.
     * Use {@link RunSelectorPickContext#getLastMatchBuild()} to
     * continue enumerating builds.
     * Or you can save the execution state
     * with {@link RunSelectorPickContext#addExtension(Object)}
     *
     * @param job       the job to pick a build from.
     * @param context   context for the current execution of runselector.
     * @return  the build matches this selector.
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread interrupts the current thread.
     */
    public Run<?, ?> getNextBuild(@Nonnull Job<?, ?> job, @Nonnull RunSelectorPickContext context)
            throws IOException, InterruptedException
    {
        // Though this can be protected,
        // Util#isOverridden is applicable only for public methods.
        return null;
    }

    /**
     * Returns the display name for this selector.
     * You can override this to output configurations of this selector
     * in verbose logs.
     *
     * @return the display name for this selector.
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
