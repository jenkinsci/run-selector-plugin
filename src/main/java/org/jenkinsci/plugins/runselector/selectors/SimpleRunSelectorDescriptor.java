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

import org.jvnet.localizer.Localizable;

/**
 * Descriptor type for common case where no overrides are needed.
 * Just do: {@code @Extension public static final Descriptor<RunSelector> DESCRIPTOR =
 *          new SimpleRunSelectorDescriptor(MySelector.class, Messages._My_DisplayName()); }
 * @author Alan Harder
 */
public class SimpleRunSelectorDescriptor extends RunSelectorDescriptor {
    private transient Localizable displayName;

    public SimpleRunSelectorDescriptor(Class<? extends RunSelector> clazz,
                                       Localizable displayName) {
        super(clazz);
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName.toString();
    }
    
    @Override
    public String getConfigPage() {
        if (!getClass().equals(SimpleRunSelectorDescriptor.class)) {
            return super.getConfigPage();
        }
        // Workaround for JENKINS-28972, JENKINS-29048
        // Jenkins tries to load view file
        // not from the plugin the RunSelector is located,
        // but from the plugin the Descriptor is located (JENKINS-29048).
        // This cause failures for RunSelectors
        // using SimpleRunSelectorDescriptor (JENKINS-28972).
        return getViewPage(SimpleRunSelectorDescriptor.class, "config.jelly");
    }
    
    public String getRunSelectorConfigPage() {
        return super.getConfigPage();
    }
}
