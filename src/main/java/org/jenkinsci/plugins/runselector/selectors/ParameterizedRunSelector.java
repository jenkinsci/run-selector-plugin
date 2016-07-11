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
package org.jenkinsci.plugins.runselector.selectors;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Use a parameter to specify how the build is selected.
 * @see RunSelectorParameter
 * @author Alan Harder
 */
public class ParameterizedRunSelector extends RunSelector {
    private String parameterName;

    @DataBoundConstructor
    public ParameterizedRunSelector(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    @CheckForNull
    private RunSelector getSelector(@Nonnull RunSelectorContext context) {
        String xml = resolveParameter(context);
        if (xml == null) {
            return null;
        }
        try {
            return RunSelectorParameter.getSelectorFromXml(xml);
        } catch (Exception e) {
            context.logException(String.format("Failed to resolve selectors: %s", xml), e);
            return null;
        }
    }

    @Override
    @CheckForNull
    public Run<?, ?> select(@Nonnull Job<?, ?> job, @Nonnull RunSelectorContext context)
            throws IOException, InterruptedException
    {
        RunSelector selector = getSelector(context);
        if (selector == null) {
            context.logInfo("No selectors was resolved.");
            return null;
        }
        return selector.select(job, context);
    }

    /**
     * Expand the parameter and resolve it to a xstream expression.
     * <ol>
     *   <li>Considers an immediate value if contains '&lt;'.
     *       This is expected to be used in especially in workflow jobs.</li>
     *   <li>Otherwise, considers a variable expression if contains '$'.</li>
     *   <li>Otherwise, it will not be taken into consideration.</li>
     * </ol>
     *
     * @param context the run selector context
     * @return xstream expression
     */
    @CheckForNull
    private String resolveParameter(@Nonnull RunSelectorContext context) {
        if (StringUtils.isBlank(getParameterName())) {
            context.logInfo("Parameter name is not specified");
            return null;
        }
        if (getParameterName().contains("<")) {
            context.logDebug("{0} is considered a xstream expression", getParameterName());
            return getParameterName();
        }
        if (getParameterName().contains("$")) {
            context.logDebug("{0} is considered a variable expression", getParameterName());
            return context.getEnvVars().expand(getParameterName());
        }
        return null;
    }

    @Extension(ordinal=-20)
    public static final Descriptor<RunSelector> DESCRIPTOR =
            new SimpleRunSelectorDescriptor(
                ParameterizedRunSelector.class, org.jenkinsci.plugins.runselector.Messages._ParameterizedRunSelector_DisplayName());
}
