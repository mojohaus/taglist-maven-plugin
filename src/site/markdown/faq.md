title: Frequently Asked Questions

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Frequently Asked Questions

1. [Where should a tag be placed to be considered in the report?](#Where_should_a_tag_be_placed_to_be_considered_in_the_report.3F)
2. [When is a tag included in the report?](#When_is_a_tag_included_in_the_report.3F)
3. [Why do I get an error stating the Taglist plugin must be executed after the JXR plugin?](#Why_do_I_get_an_error_stating_the_Taglist_plugin_must_be_executed_after_the_JXR_plugin.3F)

## General

<a id="Where should a tag be placed to be considered in the report?"></a>

### Where should a tag be placed to be considered in the report?

A tag has to be preceded by either "\*" or "//", ignoring whitespace,
i.e. it has to be inside a single-line or multi-line JavaDoc comment.
Otherwise it will be ignored in the report.

For examples of this, see the
[counting tags document](examples/counting-tags.html).

<a id="When is a tag included in the report?"></a>

### When is a tag included in the report?

For examples of this, see the
[counting tags document](examples/counting-tags.html).

<a id="Why do I get an error stating the Taglist plugin must be executed after the JXR plugin?"></a>

### Why do I get an error stating the Taglist plugin must be executed after the JXR plugin?

The error message: "\[ERROR\] Taglist plugin MUST be executed after the JXR plugin. No links to xref were generated."
is logged when the Taglist plugin is executed before the JXR (source cross-reference generator) plugin.
The Taglist plugin creates links in its report to the xref or test-xref files when tags are found; however,
if the JXR plugin has not been executed, the Taglist plugin cannot create the links.

Some causes of this condition:

- *POM plugin order*: the JXR plugin must be listed before the Taglist plugin in the POM or inherited
  POM reporting section.
- *Maven bug*: in some versions of Maven, the report plugins execution order is erroneously
  changed from the order defined in the POM file.
  Please follow [the JIRA issue](http://jira.codehaus.org/browse/MNG-2258) for more information and updated status.

To resolve this error, check that the report plugins order is correct in the POM. If you cannot modify the POM order,
or if doing so does not resolve the problem, a workaround is to execute the "site" goal twice. The first time
you will receive the error and no links will be created, but the second pass will be successful because the xref files
already exist.
