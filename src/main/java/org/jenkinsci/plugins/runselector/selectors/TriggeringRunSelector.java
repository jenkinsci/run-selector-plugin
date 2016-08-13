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
package org.jenkinsci.plugins.runselector.selectors;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Job;
import hudson.model.Run;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.RunSelectorDescriptor;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Select the build that triggered this build.
 * @author Alan Harder
 */
public class TriggeringRunSelector extends RunSelector {
    /**
     * Which build should be used if triggered by multiple upstream builds.
     * 
     * Specified in buildstep configurations and the global configuration.
     */
    public enum UpstreamFilterStrategy {
        /**
         * Use global configuration.
         * 
         * The default value for buildstep configurations.
         * Should not be specified in the global configuration.
         * 
         */
        UseGlobalSetting(false, Messages._TriggeringRunSelector_UpstreamFilterStrategy_UseGlobalSetting()),
        /**
         * Use the oldest build.
         * 
         * The default value for the global configuration.
         */
        UseOldest(true,  Messages._TriggeringRunSelector_UpstreamFilterStrategy_UseOldest()),
        /**
         * Use the newest build.
         */
        UseNewest(true, Messages._TriggeringRunSelector_UpstreamFilterStrategy_UseNewest()),
        ;
        
        private final boolean forGlobalSetting;
        private final Localizable displayName;
        
        UpstreamFilterStrategy(boolean forGlobalSetting, Localizable displayName) {
            this.forGlobalSetting = forGlobalSetting;
            this.displayName = displayName;
        }
        
        /**
         * @return the display name for the setting
         */
        public String getDisplayName() {
            return displayName.toString();
        }
        
        /**
         * @return configurable as a global setting
         */
        public boolean isForGlobalSetting() {
            return forGlobalSetting;
        }
    }
    
    /**
     * An extension for {@link RunSelectorContext}
     * that holds enumeration status.
     */
    private static class ContextExtension {
        /**
         * enumerated builds.
         */
        public Iterator<Run<?, ?>> nextBuild;
    }

    @CheckForNull
    private UpstreamFilterStrategy upstreamFilterStrategy;
    private boolean allowUpstreamDependencies;

    @DataBoundConstructor
    public TriggeringRunSelector() {
    }

    /**
     * @param upstreamFilterStrategy which build should be used if triggered by multiple upstream builds.
     */
    @DataBoundSetter
    public void setUpstreamFilterStrategy(UpstreamFilterStrategy upstreamFilterStrategy) {
        this.upstreamFilterStrategy = upstreamFilterStrategy;
    }

    /**
     * @param allowUpstreamDependencies whether to include upstream dependencies.
     */
    @DataBoundSetter
    public void setAllowUpstreamDependencies(boolean allowUpstreamDependencies) {
        this.allowUpstreamDependencies = allowUpstreamDependencies;
    }

    /**
     * @return Which build should be used if triggered by multiple upstream builds.
     */
    @CheckForNull
    public UpstreamFilterStrategy getUpstreamFilterStrategy() {
        return upstreamFilterStrategy;
    }

    /**
     * @return whether to use the newest upstream or not (use the oldest) when there are multiple upstreams.
     */
    public boolean isUseNewest() {
        UpstreamFilterStrategy strategy = getUpstreamFilterStrategy();
        if(strategy == null || strategy == UpstreamFilterStrategy.UseGlobalSetting) {
            strategy = ((DescriptorImpl)getDescriptor()).getGlobalUpstreamFilterStrategy();
        }
        if(strategy == null){
            return false;
        }
        switch(strategy) {
        case UseOldest:
            return false;
        case UseNewest:
            return true;
        default:
            // default behavior
            return false;
        }
    }
    
    /**
     * @return includes upstream dependencies.
     */
    public boolean isAllowUpstreamDependencies() {
        return allowUpstreamDependencies;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Run<?, ?> getNextBuild(@Nonnull Job<?, ?> job, @Nonnull RunSelectorContext context) {
        ContextExtension ext = context.getExtension(ContextExtension.class);
        if (ext == null) {
            // first time to be called.
            ext = new ContextExtension();
            List<Run<?, ?>> result = new ArrayList<Run<?, ?>>(
                    getAllUpstreamBuilds(job, context, context.getBuild())
            );
            // sort builds by the strategy.
            Collections.sort(
                    result,
                    new Comparator<Run<?, ?>>() {
                        @Override
                        public int compare(Run<?, ?> o1, Run<?, ?> o2) {
                            return isUseNewest()
                                    ?o2.getNumber() - o1.getNumber()
                                    :o1.getNumber() - o2.getNumber();
                        }
                    }
            );
            
            ext.nextBuild = result.iterator();
            context.addExtension(ext);
        }
        if (!ext.nextBuild.hasNext()) {
            // no matching build.
            context.removeExtension(ext);
            return null;
        }
        
        return ext.nextBuild.next();
    }
    
    @Nonnull
    private HashSet<Run<?, ?>> getAllUpstreamBuilds(@Nonnull Job<?, ?> job, @Nonnull RunSelectorContext context, @Nonnull Run<?, ?> parent) {
        HashSet<Run<?, ?>> result = new HashSet<Run<?, ?>>();
        
        // Upstream job for matrix will be parent project, not only individual configuration:
        List<String> jobNames = new ArrayList<String>();
        jobNames.add(job.getFullName());
        if ((job instanceof AbstractProject<?,?>) && ((AbstractProject<?,?>)job).getRootProject() != job) {
            jobNames.add(((AbstractProject<?,?>)job).getRootProject().getFullName());
        }

        List<Run<?, ?>> upstreamBuilds = new ArrayList<Run<?, ?>>();

        for (Cause cause: parent.getCauses()) {
            if (cause instanceof UpstreamCause) {
                UpstreamCause upstream = (UpstreamCause) cause;
                Run<?, ?> upstreamRun = upstream.getUpstreamRun();
                if (upstreamRun != null) {
                    upstreamBuilds.add(upstreamRun);
                }
            }
        }

        if (isAllowUpstreamDependencies() && (parent instanceof AbstractBuild)) {
            AbstractBuild<?, ?> parentBuild = (AbstractBuild<?,?>)parent;
            
            Map<AbstractProject, Integer> parentUpstreamBuilds = parentBuild.getUpstreamBuilds();
            for (Map.Entry<AbstractProject, Integer> buildEntry : parentUpstreamBuilds.entrySet()) {
                upstreamBuilds.add(buildEntry.getKey().getBuildByNumber(buildEntry.getValue()));
            }

        }

        for (Run<?, ?> upstreamBuild : upstreamBuilds) {
            if (jobNames.contains(upstreamBuild.getParent().getFullName())) {
                // Use the 'job' parameter instead of directly the 'upstreamBuild', because of Matrix jobs.
                result.add(job.getBuildByNumber(upstreamBuild.getNumber()));
            } else {
                // Figure out the parent job and do a recursive call to getBuild
                result.addAll(getAllUpstreamBuilds(job, context, upstreamBuild));
            }
        }
        
        return result;
    }
    
    /**
     * the descriptor for {@link TriggeringRunSelector}
     */
    @Symbol("triggering")
    @Extension
    public static class DescriptorImpl extends RunSelectorDescriptor {
        private UpstreamFilterStrategy globalUpstreamFilterStrategy;
        
        /**
         * ctor
         */
        public DescriptorImpl() {
            load();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.TriggeringRunSelector_DisplayName();
        }
        
        /**
         * set the strategy in the system configuration
         * 
         * @param globalUpstreamFilterStrategy the strategy in the system configuration
         */
        public void setGlobalUpstreamFilterStrategy(UpstreamFilterStrategy globalUpstreamFilterStrategy) {
            this.globalUpstreamFilterStrategy = globalUpstreamFilterStrategy;
        }
        
        /**
         * @return the strategy in the system configuration
         */
        public UpstreamFilterStrategy getGlobalUpstreamFilterStrategy() {
            return globalUpstreamFilterStrategy;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject json)
                throws hudson.model.Descriptor.FormException {
            setGlobalUpstreamFilterStrategy(UpstreamFilterStrategy.valueOf(json.getString("globalUpstreamFilterStrategy")));
            save();
            return super.configure(req, json);
        }
    }
}
