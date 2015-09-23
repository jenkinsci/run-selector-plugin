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
package hudson.plugins.copyartifact;

import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.model.Run;

/**
 * Additional filter used by BuildSelector.
 * @author Alan Harder
 */
public class BuildFilter {

    /**
     * @param run
     * @param env
     * @return
     * @deprecated implement {@link #isSelectable(Run, CopyArtifactPickContext)} instead.
     */
    @Deprecated
    public boolean isSelectable(Run<?,?> run, EnvVars env) {
        return true;
    }

    /**
     * @param candidate the build to check
     * @param context the context of current copyartifact execution.
     * @return whether this build can be selected.
     * 
     * @since 2.0
     */
    public boolean isSelectable(@Nonnull Run<?, ?> candidate, @Nonnull CopyArtifactPickContext context) {
        // for backward compatibility.
        return isSelectable(candidate, context.getEnvVars());
    }
}
