# Run Selector Plugin

This plugin contains Run Selector, which originally used to be in
[Copy Artifact Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Copy+Artifact+Plugin).

The **Run Selector** extension point has several implementations which can be used to select a specific build:
 - **Status Run Selector** - selects the run based on the last status. Possible filters are:
   - Stable
   - Stable or unstable
   - Unstable
   - Failed
   - Completed (any status)
   - Any (including not completed)
 - **Permalink Run Selector** - selects the run based on the permalink. Possible values:
   - 'lastBuild'
   - 'lastStableBuild'
   - 'lastSuccessfulBuild'
   - 'lastFailedBuild'
   - 'lastUnstableBuild'
   - 'lastUnsuccessfulBuild'
   - 'lastCompletedBuild'
 - **Triggering Run Selector** - selects the run that triggered this run (usually the upstream run)
 - **Specific Run Selector** - selects the run based on the given build number parameter
 - **Parameterized Run Selector** - selects the run based on a parameter
 - **Fallback Run Selector** - tries multiple selectors consequently

Moreover, you can specify a **Run Filter**, that can be used as an additional condition for the Run Selector.
The implementations for the Run Filter are the followings:
 - **Downstream Run Filter** - selects a run which is a downstream of a run build
*(Note: this is not applicable for Pipeline jobs)*
 - **Parameters Run Filter** - filter to find builds matching particular parameters
 - **Saved Run Filter** - selects the saved build (marked "keep forever")
 - **Parameterized Run Filter** - selects the run based on a parameter
 - **And Run filter** - accepts a build only when every underlying filters accepts it
 - **Or Run filter** - accepts a build when any of underlying filters accepts it
 - **Not Run filter** - accepts a build when the underlying filters don't accept it
