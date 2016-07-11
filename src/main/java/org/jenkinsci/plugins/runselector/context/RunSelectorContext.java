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

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.filters.NoRunFilter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Context for an execution of runselector.
 * This allows us to adding new fields without affecting existing plugins.
 * <p>
 * You can manage plugin specific information using
 * {@link #addExtension(Object)} and {@link #getExtension(Class)}.
 */
public class RunSelectorContext implements Cloneable {

    private static final Logger LOGGER = Logger.getLogger(RunSelectorContext.class.getName());

    @Nonnull
    private final Jenkins jenkins;
    @Nonnull
    private final Run<?, ?> build;
    @Nonnull
    private final TaskListener listener;
    @Nonnull
    private EnvVars envVars;
    @Nonnull
    private RunFilter runFilter;
    @Nonnull
    private List<Object> extensionList;
    @CheckForNull
    private Run<?, ?> lastMatchBuild;

    private boolean verbose;

    /**
     * Creates a new {@link RunSelectorContext} with {@link NoRunFilter}.
     *
     * @param jenkins  the Jenkins instance
     * @param build    the build running runselector
     * @param listener listener for the build running runselector
     */
    public RunSelectorContext(@Nonnull Jenkins jenkins, @Nonnull Run<?, ?> build, @Nonnull TaskListener listener)
            throws IOException, InterruptedException {

        this(jenkins, build, listener, new NoRunFilter());
    }

    /**
     * Creates a new {@link RunSelectorContext}.
     *
     * @param jenkins  the Jenkins instance
     * @param build    the build running runselector
     * @param listener listener for the build running runselector
     */
    public RunSelectorContext(@Nonnull Jenkins jenkins, @Nonnull Run<?, ?> build, @Nonnull TaskListener listener,
                              @Nonnull RunFilter runFilter)
            throws IOException, InterruptedException {

        this.jenkins = jenkins;
        this.build = build;
        this.listener = listener;
        this.runFilter = runFilter;

        this.envVars = constructEnvVars();
        this.extensionList = new ArrayList<Object>();
    }

    /**
     * @return the Jenkins instance
     */
    @Nonnull
    public Jenkins getJenkins() {
        return jenkins;
    }

    /**
     * @return the build running runselector
     */
    @Nonnull
    public Run<?, ?> getBuild() {
        return build;
    }

    /**
     * @return the listener for the build running runselector
     */
    @Nonnull
    public TaskListener getListener() {
        return listener;
    }

    /**
     * @return environment variables for the current build
     */
    @Nonnull
    public EnvVars getEnvVars() {
        return envVars;
    }

    /**
     * Shortcut for {@code getListener().getLogger()}
     *
     * @return stream to output logs
     */
    @Nonnull
    public PrintStream getConsole() {
        return listener.getLogger();
    }

    /**
     * @param verbose whether output verbose (for diagnostics) logs
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @return whether output verbose (for diagnostics) logs
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param runFilter the filter for builds
     */
    public void setRunFilter(@Nonnull RunFilter runFilter) {
        this.runFilter = runFilter;
    }

    /**
     * @return a filter for builds
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
     * The build picked at the last time (but not matched with the filter).
     * {@link RunSelector}s should continue the enumeration from this.
     *
     * @return build picked at the last time
     */
    @CheckForNull
    public Run<?, ?> getLastMatchBuild() {
        return lastMatchBuild;
    }

    /**
     * @return additional information by plugins
     */
    @Nonnull
    public List<Object> getExtensionList() {
        return extensionList;
    }

    /**
     * Add an object to hold plugin specific information.
     *
     * @param extension extension object
     */
    public void addExtension(@Nonnull Object extension) {
        getExtensionList().add(extension);
    }

    /**
     * @param extension extension object to remove
     * @return true if the extension is contained
     */
    public boolean removeExtension(@Nonnull Object extension) {
        return getExtensionList().remove(extension);
    }

    /**
     * Removes extensions with the same class type before adding.
     *
     * @param extension extension object to replace with
     * @return true if an extension object of the same class class is contained
     */
    public boolean replaceExtension(@Nonnull Object extension) {
        boolean removed = false;
        while (true) {
            Object e = getExtension(extension.getClass());
            if (e == null) {
                break;
            }
            removeExtension(e);
            removed = true;
        }
        addExtension(extension);
        return removed;
    }

    /**
     * Extract an extension object of the specified class.
     *
     * @param <T>   specified with {@code clazz}
     * @param clazz class of the extension to extract
     * @return extension of the class
     */
    @CheckForNull
    public <T> T getExtension(@Nonnull Class<T> clazz) {
        for (Object e : getExtensionList())
            if (clazz.isInstance(e))
                return clazz.cast(e);
        return null;
    }

    private void log(@Nonnull String message) {
        getConsole().println(message);
    }

    private void log(@Nonnull String message, @Nonnull Throwable t) {
        getConsole().println(message);
        t.printStackTrace(getConsole());
    }

    /**
     * Outputs a log message
     *
     * @param message message to log
     */
    public void logInfo(@Nonnull String message) {
        log(message);
    }

    /**
     * Outputs a log message in {@link MessageFormat} formats.
     *
     * @param pattern   pattern for {@link MessageFormat}
     * @param arguments values to format
     */
    public void logInfo(@Nonnull String pattern, Object... arguments) {
        log(MessageFormat.format(pattern, arguments));
    }

    /**
     * Outputs a log message if {@link #isVerbose()} is {@code true}
     *
     * @param message message to log
     */
    public void logDebug(@Nonnull String message) {
        if (isVerbose()) {
            log(message);
        }
    }

    /**
     * Outputs a log message in {@link MessageFormat} formats
     * if {@link #isVerbose()} is {@code true}
     *
     * @param pattern   pattern for {@link MessageFormat}
     * @param arguments values to format
     */
    public void logDebug(@Nonnull String pattern, Object... arguments) {
        if (isVerbose()) {
            log(MessageFormat.format(pattern, arguments));
        }
    }

    /**
     * Outputs a log message with an exception
     *
     * @param string message to log
     * @param t      exception to log
     */
    public void logException(@Nonnull String string, @Nonnull Throwable t) {
        log(string, t);
    }

    private void copyExtensionListFrom(RunSelectorContext src) {
        this.extensionList = new ArrayList<Object>();
        for (Object ext : src.extensionList) {
            if (ext instanceof Cloneable) {
                try {
                    Method m = ext.getClass().getMethod("clone");
                    this.extensionList.add(m.invoke(ext));
                } catch (NoSuchMethodException e) {
                    LOGGER.log(
                            Level.WARNING,
                            "Could not clone {0} as clone() is not public.",
                            ext.getClass()
                    );
                    this.extensionList.add(ext);
                } catch (Exception e) {
                    LOGGER.log(
                            Level.WARNING,
                            MessageFormat.format("Could not clone {0}.", ext.getClass()),
                            e
                    );
                    this.extensionList.add(ext);
                }
            } else {
                this.extensionList.add(ext);
            }
        }
    }

    /**
     * Constructs the environment variables for the current build.
     *
     * @return the current build environment variables
     * @throws IOException
     * @throws InterruptedException
     */
    private EnvVars constructEnvVars() throws IOException, InterruptedException {
        EnvVars envVars = build.getEnvironment(listener);
        if (build instanceof AbstractBuild) {
            envVars.putAll(((AbstractBuild<?, ?>) build).getBuildVariables()); // Add in matrix axes..
        } else {
            // Abstract#getEnvironment(TaskListener) put build parameters to
            // environments, but Run#getEnvironment(TaskListener) doesn't.
            // That means we can't retrieve build parameters from WorkflowRun
            // as it is a subclass of Run, not of AbstractBuild.
            // We need expand build parameters manually.
            // See JENKINS-26694, JENKINS-30357 for details.
            for (ParametersAction pa : build.getActions(ParametersAction.class)) {
                // We have to extract parameters manually as ParametersAction#buildEnvVars
                // (overrides EnvironmentContributingAction#buildEnvVars)
                // is applicable only for AbstractBuild.
                for (ParameterValue pv : pa.getParameters()) {
                    pv.buildEnvironment(build, envVars);
                }
            }
        }

        return envVars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RunSelectorContext clone() {
        RunSelectorContext c;
        try {
            c = (RunSelectorContext) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
        c.envVars = new EnvVars(this.envVars);
        c.copyExtensionListFrom(this);

        return c;
    }
}
