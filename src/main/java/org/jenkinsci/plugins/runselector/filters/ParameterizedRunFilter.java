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
import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Run;
import hudson.util.XStream2;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunFilterDescriptor;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * Specifies {@link RunFilter} to use with a build parameter.
 */
public class ParameterizedRunFilter extends RunFilter {
    private static final Logger LOGGER = Logger.getLogger(ParameterizedRunFilter.class.getName());
    private static final XStream2 XSTREAM = new XStream2();

    @Nonnull
    private final String parameter;
    
    /**
     * @param parameter XML expression of the filters, usually including variable expression.
     */
    @DataBoundConstructor
    public ParameterizedRunFilter(@CheckForNull String parameter) {
        this.parameter = Util.fixNull(parameter);
    }
    
    /**
     * @return XML expression of the filters, usually including variable expression.
     */
    @Nonnull
    public String getParameter() {
        return parameter;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectable(Run<?, ?> candidate, RunSelectorContext context) {
        String xml = context.getEnvVars().expand(getParameter());
        context.logDebug("{0}: Expanded run filter: {1}", getDisplayName(), xml);
        RunFilter filter = getFilterFromXml(xml);
        if (filter == null) {
            context.logDebug("{0}: No filters is specified", getDisplayName());
            return true;
        }
        return filter.isSelectable(candidate, context);
    }
    
    /**
     * @param xml XML expression of the filters
     * @return filters
     */
    @CheckForNull
    public static RunFilter getFilterFromXml(@CheckForNull String xml) {
        if (StringUtils.isBlank(xml)) {
            return null;
        }
        return (RunFilter)XSTREAM.fromXML(xml);
    }
    
    /**
     * @param filter filters
     * @return XML expression of the filters
     */
    @CheckForNull
    public static String encodeToXml(@CheckForNull RunFilter filter) {
        if (filter == null) {
            return null;
        }
        return XSTREAM.toXML(filter).replaceAll("[\n\r]+", "");
    }
    
    
    /**
     * 
     */
    @Initializer(after=InitMilestone.PLUGINS_STARTED)
    public static void initAliases() {
        for (RunFilterDescriptor d : RunFilter.all()) {
            XSTREAM.alias(d.clazz.getSimpleName(), d.clazz);
        }
    }
    
    /**
     * the descriptor for {@link ParameterizedRunFilter}
     */
    @Symbol("parameterized")
    @Extension
    public static class DescriptorImpl extends RunFilterDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.ParameterizedRunFilter_DisplayName();
        }
    }
}
