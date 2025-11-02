package org.codehaus.mojo.taglist;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the Taglist mojo basic configurations.
 *
 * @version $Id$
 */
class TaglistMojoBasicConfigTest extends AbstractTaglistMojoTestCase {

    /**
     * Test that all of the default settings are correct.
     *
     * @throws Exception
     */
    @Test
    void createTagListOutput() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/basic-config-test/create-output-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see is the com.Basic file was processed.
        String expected = "<tr class=\"a\"><th>com.BasicConfig</th>";
        assertTrue(htmlString.contains(expected), "Missing tag result.");
    }

    /**
     * Test support of multiple line comments enabled.
     *
     * @throws Exception
     */
    @Test
    void multipleLineCommentsEnabled() throws Exception {
        File pluginXmlFile = new File(
                getBasedir(), "/src/test/resources/unit/basic-config-test/multiple-line-comments-enabled-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that all three lines of the comment are captured.
        String expected = "<td>This is line one, this is line two, and this is line three.</td>";
        assertTrue(htmlString.contains(expected), "Missing tag result.");
    }

    /**
     * Test support of multiple line comments disabled.
     *
     * @throws Exception
     */
    @Test
    void multipleLineCommentsDisabled() throws Exception {
        File pluginXmlFile = new File(
                getBasedir(), "/src/test/resources/unit/basic-config-test/multiple-line-comments-disabled-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();
        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that only the first line of the comment is captured.
        String expected = "<td>This is line one,</td>";
        assertTrue(htmlString.contains(expected), "Missing tag result.");
    }

    /**
     * Test support of empty comments enabled.
     *
     * @throws Exception
     */
    @Test
    void emptyCommentsEnabled() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/basic-config-test/empty-comments-enabled-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        mojo.execute();
        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that there was only one occurrence.
        String expected = "Number of occurrences found in the code: 1";
        assertTrue(htmlString.contains(expected), "Missing tag result.");

        // Use the resource bundle to determine what the no comment string
        // is for the current locale.
        ResourceBundle bundle = ResourceBundle.getBundle("taglist-report");
        String noComment = bundle.getString("report.taglist.nocomment");

        // Check to see that the empty comment message was entered
        expected = "<td>--" + noComment + "--</td>";
        assertTrue(htmlString.contains(expected), "Incorrect no comment message.");
    }

    /**
     * Test support of empty comments disabled.
     *
     * @throws Exception
     */
    @Test
    void emptyCommentsDisabled() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/basic-config-test/empty-comments-disabled-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        mojo.execute();

        File outputDirectory = mojo.getReportOutputDirectory();
        assertNotNull(outputDirectory);
        assertFalse(outputDirectory.exists());
    }

    /**
     * Test support "tag" and "tag:" being the same tag.
     *
     * @throws Exception
     */
    @Test
    void colonInComment() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/basic-config-test/colons-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);
        String xmlString = super.getGeneratedXMLOutput(mojo);

        // Check to see that there were two tags found
        String expected = "<tag name=\"@colons\" count=\"2\">";
        assertTrue(xmlString.contains(expected), "Incorrect number of colon matches.");

        // Check for the tag without the colon
        expected = "<td>This is without colon.</td>";
        assertTrue(htmlString.contains(expected), "Missing without colon tag result.");

        // Check for the tag with the colon
        expected = "<td>This is with colon.</td>";
        assertTrue(htmlString.contains(expected), "Missing with tag result.");
    }

    /**
     * Test support empty "tag" and "tag:".
     *
     * @throws Exception
     */
    @Test
    void colonInEmptyComment() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/basic-config-test/empty-colons-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Use the resource bundle to determine what the no comment string
        // is for the current locale.
        ResourceBundle bundle = ResourceBundle.getBundle("taglist-report", Locale.ENGLISH);
        String noComment = bundle.getString("report.taglist.nocomment");

        // Check to see that there is a comment for line #40 (empty_no_colons)
        // Since there is no text following the tag, the "No Comment" string should
        // be inserted by the TagList Plugin
        String expected = "<td>--" + noComment + "--</td><td>40</td>";
        assertTrue(htmlString.contains(expected), "Incorrect empty tag without colon text.");

        // Check to see that there is a comment for line #43 (empty_colons)
        // Since there is no text following the tag, the "No Comment" string should
        // be inserted by the TagList Plugin
        expected = "<td>--" + noComment + "--</td><td>43</td>";
        assertTrue(htmlString.contains(expected), "Incorrect empty tag with colon text.");
    }

    /**
     * Test support of show empty details enabled.
     *
     * @throws Exception
     */
    @Test
    void showEmptyDetailsEnabled() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/basic-config-test/show-empty-details-enabled-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see show empty tags in code flag has a count of 1.
        String expected = "@show_empty_details_tag_in_code</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect count for the in code tag.");

        // Check to see show empty tags in not code flag has a count of 0.
        expected = "@show_empty_details_tag_not_in_code</a></td><td>0</td>";
        assertTrue(htmlString.contains(expected), "Incorrect count for the not in code tag.");

        // Check to see show empty tags in code section details exist (they should).
        expected = "<h2>@show_empty_details_tag_not_in_code</h2>";
        assertTrue(htmlString.contains(expected), "Missing tag details for the in code tag.");

        // Check to see show empty tags not in code section details exist (they should).
        expected = "<h2>@show_empty_details_tag_not_in_code</h2>";
        assertTrue(htmlString.contains(expected), "Missing tag details for the not in code tag.");
    }

    /**
     * Test support of show empty details disabled.
     *
     * @throws Exception
     */
    @Test
    void showEmptyDetailsDisabled() throws Exception {
        File pluginXmlFile = new File(
                getBasedir(), "/src/test/resources/unit/basic-config-test/show-empty-details-disabled-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see show empty tags in code flag has a count of 1.
        String expected = "@show_empty_details_tag_in_code</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect count for the in code tag.");

        // Check to see show empty tags in not code flag has a count of 0.
        // FYI: Since there are no details, there is no ending hyperlink tag (</a>).
        expected = "@show_empty_details_tag_not_in_code</td><td>0</td>";
        assertTrue(htmlString.contains(expected), "Incorrect count for the not in code tag.");

        // Check to see show empty tags in code section details exist (they should).
        expected = "<h2>@show_empty_details_tag_in_code</h2>";
        assertTrue(htmlString.contains(expected), "Missing tag details for the in code tag.");

        // Check to see show empty tags not in code section details do NOT exist (they should not).
        expected = "<h2>@show_empty_details_tag_not_in_code</h2>";
        assertFalse(htmlString.contains(expected), "Unexpected tag details for the not in code tag.");
    }

    @Test
    void xmlFile() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/basic-config-test/xml-output-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);
        mojo.execute();

        String actualXml = super.getGeneratedXMLOutput(mojo);

        File expectedFile = new File(getBasedir(), "/target/test-classes/unit/basic-config-test/expected-taglist.xml");
        String expectedXml = readFileContentWithoutNewLine(expectedFile);

        assertEquals(expectedXml, actualXml, "unexpected contents");
    }
}
