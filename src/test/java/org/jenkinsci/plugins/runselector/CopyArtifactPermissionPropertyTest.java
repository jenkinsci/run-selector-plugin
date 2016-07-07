/*
 * The MIT License
 * 
 * Copyright (c) 2013 IKEDA Yasuyuki
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

package org.jenkinsci.plugins.runselector;

import org.junit.Ignore;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Tests for {@link CopyArtifactPermissionProperty}
 */
@Ignore
public class CopyArtifactPermissionPropertyTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();
    /*
    @Test
    public void testRunSelectorPermissionProperty() throws Exception {
        // single
        {
            RunSelectorPermissionProperty target = new RunSelectorPermissionProperty("project1");
            assertEquals(Arrays.asList("project1"), target.getProjectNameList());
        }

        // multiple
        {
            RunSelectorPermissionProperty target = new RunSelectorPermissionProperty("project1,project2,project3");
            assertEquals(Arrays.asList("project1","project2","project3"), target.getProjectNameList());
        }

        // single with blanks
        {
            RunSelectorPermissionProperty target = new RunSelectorPermissionProperty("  project1  ");
            assertEquals(Arrays.asList("project1"), target.getProjectNameList());
        }

        // multiple with blanks
        {
            RunSelectorPermissionProperty target = new RunSelectorPermissionProperty("  project1  ,  project2 ,  project3 ");
            assertEquals(Arrays.asList("project1","project2","project3"), target.getProjectNameList());
        }

        // mixed
        {
            RunSelectorPermissionProperty target = new RunSelectorPermissionProperty(",  project1 ,  project2  , ,,  project3 ,");
            assertEquals(Arrays.asList("project1","project2","project3"), target.getProjectNameList());
        }

        // only blank
        {
            RunSelectorPermissionProperty target = new RunSelectorPermissionProperty("  ");
            assertEquals(Collections.emptyList(), target.getProjectNameList());
        }

        // empty
        {
            RunSelectorPermissionProperty target = new RunSelectorPermissionProperty("");
            assertEquals(Collections.emptyList(), target.getProjectNameList());
        }

        // null
        {
            RunSelectorPermissionProperty target = new RunSelectorPermissionProperty(null);
            assertEquals(Collections.emptyList(), target.getProjectNameList());
        }
    }

    @Test
    public void testIsNameMatch() throws Exception {
        // no pattern
        assertTrue(RunSelectorPermissionProperty.isNameMatch("project1", "project1"));
        assertFalse(RunSelectorPermissionProperty.isNameMatch("xproject1", "project1"));
        assertFalse(RunSelectorPermissionProperty.isNameMatch("roject1", "project1"));

        // pattern
        assertTrue(RunSelectorPermissionProperty.isNameMatch("project1", "*"));
        assertTrue(RunSelectorPermissionProperty.isNameMatch("project1", "project1*"));
        assertTrue(RunSelectorPermissionProperty.isNameMatch("project1", "project*"));
        assertTrue(RunSelectorPermissionProperty.isNameMatch("project1", "p*1"));
        assertTrue(RunSelectorPermissionProperty.isNameMatch("project1", "p*oject*1"));
        assertTrue(RunSelectorPermissionProperty.isNameMatch("project1", "*project1"));
        assertFalse(RunSelectorPermissionProperty.isNameMatch("xproject1", "project*"));
        assertFalse(RunSelectorPermissionProperty.isNameMatch("xproject1", "p*1"));
        assertFalse(RunSelectorPermissionProperty.isNameMatch("proxject1", "p*oject*1"));

        // regex pattern (should not treat as special characters)
        assertTrue(RunSelectorPermissionProperty.isNameMatch("+).][(\\\\", "+).][(\\\\"));

        // null
        assertFalse(RunSelectorPermissionProperty.isNameMatch("project1", null));
        assertFalse(RunSelectorPermissionProperty.isNameMatch(null, "project1"));
        assertFalse(RunSelectorPermissionProperty.isNameMatch(null, null));
    }

    @Test
    public void testCanRunSelector() throws Exception {
        MockFolder folder = j.jenkins.createProject(MockFolder.class, "folder");

        {
            FreeStyleProject copiee = j.createFreeStyleProject();
            FreeStyleProject copier1 = j.createFreeStyleProject();
            FreeStyleProject copier2 = j.createFreeStyleProject();
            FreeStyleProject copier3 = j.createFreeStyleProject();
            copiee.addProperty(new RunSelectorPermissionProperty(StringUtils.join(Arrays.asList(
                    copier1.getFullName(), copier2.getFullName()
            ), ',')));

            assertTrue(RunSelectorPermissionProperty.canRunSelector(copier1, copiee));
            assertTrue(RunSelectorPermissionProperty.canRunSelector(copier2, copiee));
            assertFalse(RunSelectorPermissionProperty.canRunSelector(copier3, copiee));
        }

        // same folder
        {
            FreeStyleProject copiee = folder.createProject(FreeStyleProject.class, "sameCopiee");
            FreeStyleProject copier = folder.createProject(FreeStyleProject.class, "sameCopier");
            copiee.addProperty(new RunSelectorPermissionProperty("sameCopier"));

            assertTrue(RunSelectorPermissionProperty.canRunSelector(copier, copiee));

            // absolute
            copiee.removeProperty(RunSelectorPermissionProperty.class);
            copiee.addProperty(new RunSelectorPermissionProperty("/folder/sameCopier"));

            assertTrue(RunSelectorPermissionProperty.canRunSelector(copier, copiee));
        }

        // parent folder
        {
            FreeStyleProject copiee = folder.createProject(FreeStyleProject.class, "parentCopiee");
            FreeStyleProject copier = j.jenkins.createProject(FreeStyleProject.class, "parentCopier");
            copiee.addProperty(new RunSelectorPermissionProperty("../parentCopier"));

            assertTrue(RunSelectorPermissionProperty.canRunSelector(copier, copiee));

            // absolute
            copiee.removeProperty(RunSelectorPermissionProperty.class);
            copiee.addProperty(new RunSelectorPermissionProperty("/parentCopier"));

            assertTrue(RunSelectorPermissionProperty.canRunSelector(copier, copiee));
        }

        // child folder
        {
            FreeStyleProject copiee = j.jenkins.createProject(FreeStyleProject.class, "childCopiee");
            FreeStyleProject copier = folder.createProject(FreeStyleProject.class, "childCopier");
            copiee.addProperty(new RunSelectorPermissionProperty(String.format("%s/childCopier", folder.getName())));

            assertTrue(RunSelectorPermissionProperty.canRunSelector(copier, copiee));

            // absolute
            copiee.removeProperty(RunSelectorPermissionProperty.class);
            copiee.addProperty(new RunSelectorPermissionProperty("/folder/childCopier"));

            assertTrue(RunSelectorPermissionProperty.canRunSelector(copier, copiee));
        }
    }

    @Test
    public void testDescriptorNewInstance() throws Exception {
        WebClient wc = j.createWebClient();
        
        // not configured
        {
            FreeStyleProject p = j.createFreeStyleProject();
            assertNull(p.getProperty(RunSelectorPermissionProperty.class));
            
            j.submit(wc.getPage(p, "configure").getFormByName("config"));
            
            p = j.jenkins.getItemByFullName(p.getFullName(), FreeStyleProject.class);
            assertNotNull(p);
            assertNull(p.getProperty(RunSelectorPermissionProperty.class));
        }
        
        // configured
        {
            FreeStyleProject p = j.createFreeStyleProject();
            p.addProperty(new RunSelectorPermissionProperty("project1"));
            
            j.submit(wc.getPage(p, "configure").getFormByName("config"));
            
            p = j.jenkins.getItemByFullName(p.getFullName(), FreeStyleProject.class);
            assertNotNull(p);
            RunSelectorPermissionProperty prop = p.getProperty(RunSelectorPermissionProperty.class);
            assertNotNull(prop);
            assertEquals("project1", prop.getProjectNames());
        }
    }
    
    @Test
    public void testDescriptorCheckNotFoundProjects() throws Exception {
        RunSelectorPermissionProperty.DescriptorImpl d
                = (RunSelectorPermissionProperty.DescriptorImpl)j.jenkins.getDescriptor(RunSelectorPermissionProperty.class);
        j.createFreeStyleProject("project1");
        j.createFreeStyleProject("project2");
        MatrixProject matrix = createMatrixProject("matrix1");
        AxisList axes = new AxisList(new TextAxis("axis1", "value1"));
        matrix.setAxes(axes);
        MatrixConfiguration matrixConf = matrix.getItem(new Combination(axes, "value1"));
        
        MockFolder folder = j.jenkins.createProject(MockFolder.class, "folder");
        folder.createProject(FreeStyleProject.class, "child1");
        folder.createProject(FreeStyleProject.class, "child2");
        
        assertEquals(Collections.emptyList(), d.checkNotFoundProjects("folder/child1", j.jenkins));
        assertEquals(Collections.emptyList(), d.checkNotFoundProjects(" project1,, project2, matrix1,folder/child1, folder/child2", j.jenkins));
        assertEquals(Collections.emptyList(), d.checkNotFoundProjects("child1,child2,../project1", folder));
        assertEquals(Collections.emptyList(), d.checkNotFoundProjects(null, j.jenkins));
        assertEquals(Collections.emptyList(), d.checkNotFoundProjects("", j.jenkins));
        assertEquals(Collections.emptyList(), d.checkNotFoundProjects("project*,*,nosuch*", j.jenkins));
        
        assertEquals(Arrays.asList(matrixConf.getFullDisplayName()), d.checkNotFoundProjects(matrixConf.getFullDisplayName(), j.jenkins));
        assertEquals(Arrays.asList("nosuch1", "nosuch2"), d.checkNotFoundProjects("nosuch1,project1,,nosuch2", j.jenkins));
    }
    
    @Test
    public void testDescriptorDoAutoCompleteProjectNames() throws Exception {
        RunSelectorPermissionProperty.DescriptorImpl d
                = (RunSelectorPermissionProperty.DescriptorImpl)j.jenkins.getDescriptor(RunSelectorPermissionProperty.class);
        FreeStyleProject freestyle = j.createFreeStyleProject("project1");
        MatrixProject matrix = createMatrixProject("matrix1");
        AxisList axes = new AxisList(new TextAxis("axis1", "value1"));
        matrix.setAxes(axes);
        
        MockFolder folder = j.jenkins.createProject(MockFolder.class, "folder");
        FreeStyleProject child = folder.createProject(FreeStyleProject.class, "child1");
        
        assertEquals(Arrays.asList("project1"), d.doAutoCompleteProjectNames("p", freestyle).getValues());
        assertEquals(Arrays.asList("project1"), d.doAutoCompleteProjectNames(" p", freestyle).getValues());
        assertEquals(Arrays.asList("matrix1"), d.doAutoCompleteProjectNames("m", freestyle).getValues());
        assertEquals(Arrays.asList("folder/child1"), d.doAutoCompleteProjectNames("f", freestyle).getValues());
        assertEquals(Arrays.asList("child1"), d.doAutoCompleteProjectNames("c", child).getValues());
        assertEquals(Arrays.asList("../project1"), d.doAutoCompleteProjectNames("../p", child).getValues());
        assertEquals(Collections.emptyList(), d.doAutoCompleteProjectNames("x", freestyle).getValues());
        assertEquals(Collections.emptyList(), d.doAutoCompleteProjectNames("", freestyle).getValues());
    }

    *//**
     * Creates an empty Matrix project with the provided name.
     *
     * @param name Project name.
     * @return an empty Matrix project with the provided name.
     *//*
    private MatrixProject createMatrixProject(String name) throws IOException {
        return j.jenkins.createProject(MatrixProject.class, name);
    }
*/
}
