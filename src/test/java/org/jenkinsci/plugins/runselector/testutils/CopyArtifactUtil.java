/*
 * The MIT License
 *
 * Copyright (c) 2013-2014, CloudBees, Inc.
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
package org.jenkinsci.plugins.runselector.testutils;

import hudson.tasks.Builder;
import org.jenkinsci.plugins.runselector.selectors.RunSelector;
//import hudson.plugins.runselector.RunSelector;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CopyArtifactUtil {

    private CopyArtifactUtil() {
    }

    public static Builder createRunSelector(String projectName, String parameters, RunSelector selector, String filter, String target,
                                             boolean flatten, boolean optional) {
        return createRunSelector(projectName, parameters, selector, filter, null, target, flatten, optional, false);
    }

    public static Builder createRunSelector(String projectName, String parameters, RunSelector selector, String filter, String target,
                                             boolean flatten, boolean optional, boolean fingerprintArtifacts) {
        return createRunSelector(projectName, parameters, selector, filter, null, target, flatten, optional, fingerprintArtifacts);
    }

    public static Builder createRunSelector(String projectName, String parameters, RunSelector selector, String filter, String excludes, String target,
                                             boolean flatten, boolean optional, boolean fingerprintArtifacts) {
        return createRunSelector(projectName, parameters, selector, filter, excludes, target, flatten, optional, fingerprintArtifacts, null);
    }
    
    public static Builder createRunSelector(String projectName, String parameters, RunSelector selector, String filter, String excludes, String target,
                                             boolean flatten, boolean optional, boolean fingerprintArtifacts, String resultVariableSuffix) {
//        RunSelector copyArtifact = new RunSelector(projectName);
//        copyArtifact.setSelector(selectors);
//        copyArtifact.setOptional(optional);
//        copyArtifact.setResultVariableSuffix(resultVariableSuffix);
//        copyArtifact.setVerbose(true);
//
//        copyArtifact.setParameters(parameters);
//
//        copyArtifact.setOperation(null);
//        copyArtifact.setFilter(filters);
//        copyArtifact.setExcludes(excludes);
//        copyArtifact.setTarget(target);
//        copyArtifact.setFlatten(flatten);
//        copyArtifact.setFingerprintArtifacts(fingerprintArtifacts);
//        return copyArtifact;
        return null;
    }
}
