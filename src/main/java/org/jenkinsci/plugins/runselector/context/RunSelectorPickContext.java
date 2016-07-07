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

package org.jenkinsci.plugins.runselector.context;

import hudson.model.Run;
import org.jenkinsci.plugins.runselector.filters.RunFilter;
import org.jenkinsci.plugins.runselector.selectors.RunSelector;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Context for the task of runselector picking the build to copy.
 * This allows us to adding new fields without affecting
 * existing plugins.
 */
public class RunSelectorPickContext extends RunSelectorCommonContext {
    private String projectName;
    private RunFilter runFilter;
    private Run<?,?> lastMatchBuild;

    /**
     * @param projectName the project name to copy from.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * The project name to copy from.
     * Be aware that this might be different from the full name of the project
     * as it might be specified with relative expression.
     * 
     * @return the project name to copy from.
     */
    @Nonnull
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param runFilter filters for builds
     */
    public void setRunFilter(@Nonnull RunFilter runFilter) {
        this.runFilter = runFilter;
    }

    /**
     * @return a filters for builds
     */
    @Nonnull
    public RunFilter getRunFilter() {
        return runFilter;
    }

    /**
     * @param lastMatchBuild build picked at the last time
     */
    public void setLastMatchBuild(Run<?, ?> lastMatchBuild) {
        this.lastMatchBuild = lastMatchBuild;
    }

    /**
     * The build picked at the last time (but not matched with the filters).
     * {@link RunSelector}s should continue the enumeration from this.
     * 
     * @return build picked at the last time
     */
    @CheckForNull
    public Run<?, ?> getLastMatchBuild() {
        return lastMatchBuild;
    }

    /**
     * ctor
     */
    public RunSelectorPickContext() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RunSelectorPickContext clone() {
        return (RunSelectorPickContext)super.clone();
    }
}
