title: Usage
author:
- Fabrice Bellingard

Usage
=====

Available Configuration Options
-------------------------------

You can refer to the [goal documentation](./taglist-mojo.html) to discover
the different configuration options for this plugin.

How to Use
----------

In your `pom.xml`, insert this segment:

```xml
<project>
  ...
  <build>
    ...
  </build>
  ...
  <reporting>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>taglist-maven-plugin</artifactId>
        <version><!-- latest release --></version>
      </plugin>
    </plugins>
  </reporting>
  ...
</project>
```

NOTE:
If you like having cross references included you need to define the
taglist-maven-plugin after [maven-jxr-plugin](https://maven.apache.org/jxr/maven-jxr-plugin/)
otherwise you will get a warning (See also [FAQ](./faq.html)).

The report will be generated when executing the `site` life cycle phase,
like this:

```sh
mvn site
```

The TagList plugin allows tags to be found using three matching methods: exact match, ignore case match, and
regular expression match. The matched tags are grouped into tag classes. For instance:

```xml
<project>
  ...
  <build>
    ...
  </build>
  ...
  <reporting>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>taglist-maven-plugin</artifactId>
        <configuration>
          <tagListOptions>
            <tagClasses>
              <tagClass>
                <displayName>Todo Work</displayName>
                <tags>
                  <tag>
                    <matchString>todo</matchString>
                    <matchType>ignoreCase</matchType>
                  </tag>
                  <tag>
                    <matchString>FIXME</matchString>
                    <matchType>exact</matchType>
                  </tag>
                </tags>
              </tagClass>
            </tagClasses>
          </tagListOptions>
        </configuration>
      </plugin>
    </plugins>
  </reporting>
  ...
</project>
```

See also type description [taglistOptions](./taglistOptions.html).

Other Options
-------------

These parameters change what the report scans and how it presents the result; the
[goal documentation](./taglist-mojo.html) describes each one in full.

* `includes` and `excludes`: the source files to scan, as patterns relative to the source directories. Only
  `**/*.java` is scanned by default.
* `skipTestSources`: leave the test sources out of the report.
* `skipEmptyReport` (property `taglist.skipEmptyReport`): do not generate the report when no tags are found.
* `showEmptyDetails`: also list the tags that have no occurrences.
* `linkXRef` (property `taglists.linkXRef`), `xrefLocation` and `testXrefLocation`: link the tag line numbers to
  the source cross-reference of the JXR plugin.
* `aggregate` (property `taglists.aggregate`): build one report at the root of a multi-module project instead of one
  per module.
* `inputEncoding` (property `encoding`): the encoding of the source files. Without it, the platform encoding is
  used and the plugin logs a warning.

```xml
<configuration>
  <inputEncoding>UTF-8</inputEncoding>
  <includes>
    <include>**/*.java</include>
  </includes>
  <excludes>
    <exclude>**/generated/**</exclude>
  </excludes>
  <skipTestSources>true</skipTestSources>
</configuration>
```

The report is also written as XML, to `target/taglist/taglist.xml` by default (see
[taglistOutput](./taglistOutput.html)).
