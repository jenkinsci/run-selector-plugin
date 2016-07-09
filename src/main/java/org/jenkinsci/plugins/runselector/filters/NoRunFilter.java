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

import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunFilterDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Filter that accepts all builds.
 * Used for "Not configured"
 */
public class NoRunFilter extends RunFilter {
    /**
     * 
     */
    @DataBoundConstructor
    public NoRunFilter() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RunFilterDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
    
    /**
     *
     */
    // @Extension(ordinal=100)  Should not be automatically listed.
    public static class DescriptorImpl extends RunFilterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.NoRunFilter_DisplayName();
        }
    }
    
    /**
     * the descriptor for {@link NoRunFilter}.
     * Not listed as an extension.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
}
