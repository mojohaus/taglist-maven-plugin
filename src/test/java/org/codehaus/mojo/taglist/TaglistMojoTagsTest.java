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

/**
 * Test the Taglist mojo tags.
 *
 * @version $Id$
 */
public class TaglistMojoTagsTest extends AbstractTaglistMojoTestCase {

    /**
     * Test the default tags.
     *
     * @throws Exception
     */
    public void testDefaultTags() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-test/default-tags-pom.xml");

        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that @todo has one occurance.
        String expected = "\">@todo</a></td><td>1</td>";
        assertTrue("Incorrect default @todo tag result.", htmlString.contains(expected));

        // Check to see that @todo has one occurance.
        expected = "\">TODO</a></td><td>1</td>";
        assertTrue("Incorrect default TODO tag result.", htmlString.contains(expected));

        // Check to see that @FIXME has one occurance.
        expected = "\">FIXME</a></td><td>1</td>";
        assertTrue("Incorrect default FIXME tag result.", htmlString.contains(expected));
    }

    /**
     * Test the C style tags.
     *
     *
     * @throws Exception
     */
    public void testCTags() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-test/c-style-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that C++ style tag has one occurrence.
        String expected = "\">c_style_tag</a></td><td>1</td>";
        assertTrue("Incorrect C style tag result.", htmlString.contains(expected));

        // Check to see that tag has the correct text.
        expected = "<td>This is a C style tag.</td>";
        assertTrue("Incorrect C style tag text.", htmlString.contains(expected));
    }

    /**
     * Test the C++ style tags.
     *
     * @throws Exception
     */
    public void testCPlusPlusTags() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-test/cplusplus-style-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that C++ style tag has one occurrence.
        String expected = "\">c++_style_tag</a></td><td>1</td>";
        assertTrue("Incorrect C++ style tag result.", htmlString.contains(expected));

        // Check to see that tag has the correct text.
        expected = "<td>This is a C++ style tag.</td>";
        assertTrue("Incorrect C++ style tag text.", htmlString.contains(expected));
    }

    /**
     * Test the JavaDoc single line style tags.
     * <p>
     * This tests java doc comments that contain the tag on one line only.
     *
     * @throws Exception
     */
    public void testJavaDocSingleTags() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/tag-test/javadoc-single-style-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that JavaDoc single style tag has one occurrence.
        String expected = "\">javadoc_single_style_tag</a></td><td>1</td>";
        assertTrue("Incorrect JavaDoc single style tag result.", htmlString.contains(expected));

        // Check to see that tag has the correct text.
        expected = "<td>This is a JavaDoc single style tag.</td>";
        assertTrue("Incorrect JavaDoc single style tag text.", htmlString.contains(expected));
    }

    /**
     * Test the JavaDoc multi line style tags.
     * <p>
     * This test java doc comments that contain the tag on the second line
     * or later of the java doc comment.
     *
     * @throws Exception
     */
    public void testJavaDocMultiTags() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/tag-test/javadoc-multi-style-tags-pom.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);

        // Check to see that JavaDoc style tag has one occurrence.
        String expected = "\">javadoc_multi_style_tag</a></td><td>1</td>";
        assertTrue("Incorrect JavaDoc multi style tag result.", htmlString.contains(expected));

        // Check to see that tag has the correct text.
        expected = "<td>This is a JavaDoc multi style tag.</td>";
        assertTrue("Incorrect JavaDoc multi style tag text.", htmlString.contains(expected));
    }

    /**
     * Test the tags not at start of line.
     * <p>
     * E.g. // This is a comment about @todo line.   &lt;-- @todo didn't start line
     *
     * @throws Exception
     */
    public void testNotAtStartOfLineTags() throws Exception {
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
    public void testSourceCodeVariablesTags() throws Exception {
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
