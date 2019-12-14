/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Alan Harder
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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStreamException;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Descriptor;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Alan Harder
 */
public class RunSelectorParameter extends SimpleParameterDefinition {
    private RunSelector defaultSelector;
    private static final Logger LOGGER = Logger.getLogger(RunSelectorParameter.class.getName());

    @DataBoundConstructor
    public RunSelectorParameter(String name, RunSelector defaultSelector, String description) {
        super(name, description);
        this.defaultSelector = defaultSelector;
    }

    public RunSelector getDefaultSelector() {
        return defaultSelector;
    }

    @Override
    public ParameterValue getDefaultParameterValue() {
        return toStringValue(defaultSelector);
    }

    @Override
    public ParameterValue createValue(String value) {
        getSelectorFromXml(value); // validate the input
        return new StringParameterValue(getName(), value, getDescription());
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        return toStringValue(req.bindJSON(RunSelector.class, jo));
    }

    private StringParameterValue toStringValue(RunSelector selector) {
        return new StringParameterValue(
                getName(), XSTREAM.toXML(selector).replaceAll("[\n\r]+", ""), getDescription());
    }

    /**
     * Convert xml fragment into a RunSelector object.
     * @param xml XML fragment to parse.
     * @return the RunSelector represented by the input XML.
     * @throws XStreamException if the object cannot be deserialized
     * @throws ClassCastException if input is invalid
     */
    public static RunSelector getSelectorFromXml(String xml) {
        return (RunSelector)XSTREAM.fromXML(xml);
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return org.jenkinsci.plugins.runselector.Messages.RunSelectorParameter_DisplayName();
        }

        public DescriptorExtensionList<RunSelector,Descriptor<RunSelector>> getRunSelectors() {
            Jenkins jenkins = Jenkins.getInstance();
            return jenkins.getDescriptorList(RunSelector.class);
        }

        /**
         * @return {@link RunSelector}s available for RunSelectorParameter.
         */
        public List<Descriptor<RunSelector>> getAvailableRunSelectorList() {
            Jenkins jenkins = Jenkins.getInstance();
            return Lists.newArrayList(Collections2.filter(
                    jenkins.getDescriptorList(RunSelector.class),
                    new Predicate<Descriptor<RunSelector>>() {
                        public boolean apply(Descriptor<RunSelector> input) {
                            return !"ParameterizedRunSelector".equals(input.clazz.getSimpleName());
                        };
                    }
            ));
        }
    }

    private static final XStream2 XSTREAM = new XStream2();

    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    public static void initAliases() {
        Jenkins jenkins = Jenkins.getInstance();
        // Alias all RunSelectors to their simple names
        for (Descriptor<RunSelector> d : jenkins.getDescriptorByType(DescriptorImpl.class).getRunSelectors())
            XSTREAM.alias(d.clazz.getSimpleName(), d.clazz);
    }
}
