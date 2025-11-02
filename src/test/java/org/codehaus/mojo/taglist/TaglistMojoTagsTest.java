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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the Taglist mojo tags.
 *
 * @version $Id$
 */
class TaglistMojoTagsTest extends AbstractTaglistMojoTestCase {

    /**
     * Test the default tags.
     *
     * @throws Exception
     */
    @Test
    void defaultTags() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-test/default-tags-pom.xml");

        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that @todo has one occurance.
        String expected = "\">@todo</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect default @todo tag result.");

        // Check to see that @todo has one occurance.
        expected = "\">TODO</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect default TODO tag result.");

        // Check to see that @FIXME has one occurance.
        expected = "\">FIXME</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect default FIXME tag result.");
    }

    /**
     * Test the C style tags.
     *
     *
     * @throws Exception
     */
    @Test
    void cTags() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-test/c-style-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that C++ style tag has one occurrence.
        String expected = "\">c_style_tag</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect C style tag result.");

        // Check to see that tag has the correct text.
        expected = "<td>This is a C style tag.</td>";
        assertTrue(htmlString.contains(expected), "Incorrect C style tag text.");
    }

    /**
     * Test the C++ style tags.
     *
     * @throws Exception
     */
    @Test
    void cPlusPlusTags() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-test/cplusplus-style-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that C++ style tag has one occurrence.
        String expected = "\">c++_style_tag</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect C++ style tag result.");

        // Check to see that tag has the correct text.
        expected = "<td>This is a C++ style tag.</td>";
        assertTrue(htmlString.contains(expected), "Incorrect C++ style tag text.");
    }

    /**
     * Test the JavaDoc single line style tags.
     * <p>
     * This tests java doc comments that contain the tag on one line only.
     *
     * @throws Exception
     */
    @Test
    void javaDocSingleTags() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/tag-test/javadoc-single-style-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that JavaDoc single style tag has one occurrence.
        String expected = "\">javadoc_single_style_tag</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect JavaDoc single style tag result.");

        // Check to see that tag has the correct text.
        expected = "<td>This is a JavaDoc single style tag.</td>";
        assertTrue(htmlString.contains(expected), "Incorrect JavaDoc single style tag text.");
    }

    /**
     * Test the JavaDoc multi line style tags.
     * <p>
     * This test java doc comments that contain the tag on the second line
     * or later of the java doc comment.
     *
     * @throws Exception
     */
    @Test
    void javaDocMultiTags() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/tag-test/javadoc-multi-style-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that JavaDoc style tag has one occurrence.
        String expected = "\">javadoc_multi_style_tag</a></td><td>1</td>";
        assertTrue(htmlString.contains(expected), "Incorrect JavaDoc multi style tag result.");

        // Check to see that tag has the correct text.
        expected = "<td>This is a JavaDoc multi style tag.</td>";
        assertTrue(htmlString.contains(expected), "Incorrect JavaDoc multi style tag text.");
    }

    /**
     * Test the tags not at start of line.
     * <p>
     * E.g. // This is a comment about @todo line.   &lt;-- @todo didn't start line
     *
     * @throws Exception
     */
    @Test
    void notAtStartOfLineTags() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-test/not-start-line-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        File outputDirectory = mojo.getReportOutputDirectory();
        assertNotNull(outputDirectory);
        assertFalse(outputDirectory.exists());
    }

    /**
     * Test the source code with variables that match tag name.
     * <p>
     * This test makes sure that source code with variable names
     * that match to "tag" do not show up in the results.
     *
     * @throws Exception
     */
    @Test
    void sourceCodeVariablesTags() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/tag-test/source-code-variable-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        File outputDirectory = mojo.getReportOutputDirectory();
        assertNotNull(outputDirectory);
        assertFalse(outputDirectory.exists());
    }
}
