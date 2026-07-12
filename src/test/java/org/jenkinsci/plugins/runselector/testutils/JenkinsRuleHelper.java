package org.jenkinsci.plugins.runselector.testutils;

import org.junit.runner.Description;
import org.jvnet.hudson.test.JenkinsRule;

import java.lang.reflect.Field;

/**
 * Helper to set up a {@link JenkinsRule} for JUnit 5 class-level use
 * (equivalent to JUnit 4's {@code @ClassRule}).
 */
public final class JenkinsRuleHelper {

    private JenkinsRuleHelper() {
    }

    /**
     * Creates and starts a JenkinsRule suitable for class-level sharing.
     * The required {@code testDescription} is set via reflection since it is
     * a protected field in another package.
     *
     * @param testClass the test class for which the rule is being created
     * @return the configured JenkinsRule instance
     * @throws Throwable if Jenkins startup fails
     */
    public static JenkinsRule createAndStart(Class<?> testClass) throws Throwable {
        JenkinsRule rule = new JenkinsRule();
        setTestDescription(rule, Description.createTestDescription(
                testClass, (String) null, testClass.getAnnotations()));
        rule.before();
        return rule;
    }

    private static void setTestDescription(JenkinsRule rule, Description description) throws Throwable {
        try {
            Field field = JenkinsRule.class.getDeclaredField("testDescription");
            field.setAccessible(true);
            field.set(rule, description);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set testDescription on JenkinsRule", e);
        }
    }
}
