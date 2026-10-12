# Agent guide for taglist-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module with one goal, `taglist` (`TagListReport`, a report mojo), in
  `org.codehaus.mojo.taglist`. `FileAnalyser` scans the sources, `tags` holds the match types
  (`GenericTag`, `IgnoreCaseTag`, `RegExTag`, `TagClass`) and `TaglistReportRenderer` writes the
  report.
- The configuration (`tagListOptions`) and the XML output are Modello models:
  `src/main/mdo/TagListOptions.mdo` and `src/main/mdo/TagListOutput.mdo`. Edit the `.mdo` files;
  the classes and XSDs are generated under `target/generated-sources/modello` and must not be
  edited. The `pre-site` phase also generates their xdoc.
- The report texts are in `src/main/resources/taglist-report*.properties` for `en`, `de` and `fr`.
- The site is built from `src/site` (`apt` pages and the FAQ in `fml`).

## Build gates

- On a local build Spotless applies the formatting; it also reformats Markdown, so run
  `mvn spotless:apply` after editing the README or this file.
- The parent, `mojo-parent`, sets `maven.compiler.release` to 8, so main code must use the Java 8
  API.
- The plugin compiles against Maven 3.9.16 (`mavenVersion`) and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite.

## Unit tests

- JUnit 5 with the Maven plugin testing harness: test classes extend
  `AbstractTaglistMojoTestCase`, which carries `@MojoTest`, and inject `TagListReport` from a test
  POM with `@InjectMojo`. `getTagListReport(mojo, dir)` points the project's source roots at
  `src/test/resources/unit/<dir>/java-sources`, so the report runs against that fixture and not the
  plugin's own sources.
- Each case is a POM in `src/test/resources/unit/<dir>/` plus the expected output next to it, such as
  `expected-taglist.xml`.
- The tests resolve the Fluido skin from Central through the local repository that surefire passes
  as `localRepository`, so a first run needs network access.
- Run a subset:

  ```shell
  mvn -B -Dtest=TaglistMojoSkipTest,TaglistMojoBasicConfigTest verify
  ```
- There are no integration tests.

## Dependencies

- Deliberate pins in `.github/dependabot.yml`: the Maven artifacts stay below 3.10.0 (the plugin
  baseline is the Maven 3.9.x line), Maven Resolver below 2.0 and `plexus-xml` below 4.0.0 (both
  target Maven 4), and SLF4J below 2.0 (it needs a service provider that the Maven 3 runtime does
  not offer).

## Continuous integration and pull requests

- `.github/workflows/maven.yml` calls the shared `maven-verify.yml@v5` workflow from
  `apache/maven-gh-actions-shared`, with `install` as the goal, because the site build uses the
  project version for reporting. Branches in this repository build under the push event, so look at
  the push run for a pull request from a same-repository branch.
- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

