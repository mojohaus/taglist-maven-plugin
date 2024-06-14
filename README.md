# MojoHaus TagList Maven Plugin

This is the http://www.mojohaus.org/taglist-maven-plugin/[taglist-maven-plugin.

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](https://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/taglist-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.mojo/taglist-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/taglist-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/taglist-maven-plugin/actions/workflows/maven.yml)

## Contributing

### Creating Issues

If you find a problem please create an
https://github.com/mojohaus/taglist-maven-plugin/issues[issue in the ticket]
and describe what is going wrong or what you expect to happen.
If you have a full working example or a log file this is also helpful.
You should of course describe only a single issue in a single ticket and not
mixing up several different things into a single issue.

### Creating a Pull Request

Before you create a pull request it is necessary to create an issue in
the https://github.com/mojohaus/taglist-maven-plugin/issues[ticket system]
and describe what the problem is or what kind of feature you would like
to add. Afterwards you can create an appropriate pull request.

It is required if you want to get a Pull request to be integrated into please
squash your commits into a single commit which references the issue in the
commit message which looks like this:

```
Fixed #Issue

 o Description.

```

This makes it simpler to merge it and this will also close the
appropriate issue automatically in one go. This make the life as
maintainer a little bit easier.

A pull request has to fulfill only a single ticket and should never
create/add/fix several issues in one, cause otherwise the history is hard to
read and to understand and makes the maintenance of the issues and pull request
hard or to be honest impossible.

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```

