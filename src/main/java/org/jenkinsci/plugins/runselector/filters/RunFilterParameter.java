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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Build parameter used with {@link ParameterizedRunFilter}
 */
public class RunFilterParameter extends SimpleParameterDefinition {
    @SuppressFBWarnings(value="SE_BAD_FIELD", justification="Serialized with XStream and doesn't require Serializable")
    @Nonnull
    private final RunFilter defaultFilter;
    
    /**
     * @param name name of the build parameter
     * @param description description of this parameter
     * @param defaultFilter run filter used as default
     */
    @DataBoundConstructor
    public RunFilterParameter(String name, String description, @CheckForNull RunFilter defaultFilter) {
        super(name, description);
        this.defaultFilter = (defaultFilter != null)?defaultFilter:new NoRunFilter();
    }

    /**
     * @return run filter used as default
     */
    public RunFilter getDefaultFilter() {
        return defaultFilter;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ParameterValue createValue(String value) {
        return new StringParameterValue(getName(), value, getDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        RunFilter filter = req.bindJSON(RunFilter.class, jo);
        return createValue(ParameterizedRunFilter.encodeToXml(filter));
    }
    
    /**
     * the descriptor for {@link RunFilterParameter}
     */
    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return org.jenkinsci.plugins.runselector.Messages.RunFilterParameter_DisplayName();
        }

        /**
         * @return descriptors of all {@link RunFilter}s except {@link RunFilterParameter}
         */
        public Iterable<RunFilterDescriptor> getRunFilterDescriptors() {
            return Iterables.filter(
                    RunFilter.allWithNoRunFilter(),
                    new Predicate<RunFilterDescriptor>() {
                        @Override
                        public boolean apply(RunFilterDescriptor d) {
                            return !d.clazz.equals(ParameterizedRunFilter.class);
                        }
                    }
            );
        }
    }
}
