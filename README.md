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
   - `lastBuild`
   - `lastStableBuild`
   - `lastSuccessfulBuild`
   - `lastFailedBuild`
   - `lastUnstableBuild`
   - `lastUnsuccessfulBuild`
   - `lastCompletedBuild`
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

## Pipeline examples

This plugin may also be used in Pipeline code. 
It provides the `selectRun` step that selects a specific build based on the input parameters.
The step returns a `RunWrapper` object which can be used as input parameter for other steps.

### Select the build based on its status

By default, if no selector parameter is provided, the `selectRun` step selects the last stable build from the 
upstream job.

```groovy
def runWrapper = selectRun 'upstream-project-name'
```

Alternatively, you can specify the `StatusRunSelector`. 
For example, if you'd like to select the last successful build (stable or unstable), the value of the 
`buildStatus` parameter has to be *Successful*:
 
```groovy
def runWrapper = selectRun job: 'upstream-project-name', 
 selector: [$class: 'StatusRunSelector', buildStatus: 'Successful'] 
```
The complete list of `buildStatus` values may be found in the Pipeline *Snippet Generator*.

Or, if you'd like to make use of *Permalink*, you can use the `PermalinkRunSelector`.
For example, the permalink for selecting the last unstable build is *lastUnstableBuild*: 

```groovy
def runWrapper = selectRun job: 'upstream-project-name', 
 selector: [$class: 'PermalinkRunSelector', id: 'lastUnstableBuild'] 
```

The complete list of *Permalink* `id` values may be found in the Pipeline *Snippet Generator*. 

### Select a specific build number

You can select a specific build number from the upstream job. 
In the following example, the *UPSTREAM_BUILD_NUMBER* is a build parameter.

```groovy
def runWrapper = selectRun job: 'upstream-project-name', 
 selector: [$class: 'SpecificRunSelector', buildNumber: UPSTREAM_BUILD_NUMBER] 
```

### Select the triggering build

You may have an upstream job that triggers a specific downstream job by using the `build` step:

```groovy
build 'downstream-project-name'
```

A possible solution to select the run that triggered your downstream job is by using the `TriggeringRunSelector`:

```groovy
def runWrapper = selectRun job: 'upstream-project-name', 
 selector: [$class: 'TriggeringRunSelector'] 
```

Of course you could instead (and more explicitly) have the upstream build pass `currentBuild.number` as a build parameter.
