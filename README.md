# MojoHaus Taglist Maven Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/taglist-maven-plugin.svg?label=License)](https://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/taglist-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/taglist-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/taglist-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/taglist-maven-plugin/actions/workflows/maven.yml)

The Taglist Maven Plugin generates a report on the tags found in the source code, such as `@todo` or
`// TODO`, grouped into tag classes of your choice. It requires Maven 3.9.0 or later and Java 8 or later.

Add it to the `reporting` section of your `pom.xml` and run `mvn site`:

```xml
<reporting>
  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>taglist-maven-plugin</artifactId>
      <version><!-- latest release --></version>
    </plugin>
  </plugins>
</reporting>
```

The [plugin site](https://www.mojohaus.org/taglist-maven-plugin/) has the
[usage guide](https://www.mojohaus.org/taglist-maven-plugin/usage.html), the
[goal parameters](https://www.mojohaus.org/taglist-maven-plugin/taglist-mojo.html) and the
[FAQ](https://www.mojohaus.org/taglist-maven-plugin/faq.html).

## Contributing

Report problems in the [issue tracker](https://github.com/mojohaus/taglist-maven-plugin/issues). Build,
test and pull request conventions are in [AGENTS.md](AGENTS.md).

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```

