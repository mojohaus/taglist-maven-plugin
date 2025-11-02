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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the Taglist mojo new tag class configurations.
 *
 * @version $Id$
 */
class TaglistMojoTagClassesTest extends AbstractTaglistMojoTestCase {

    /**
     * Test that the default match type is exact.
     *
     * @throws Exception
     */
    @Test
    void defaultExactMatch() throws Exception {
        File pluginXmlFile =
                new File(getBasedir(), "/src/test/resources/unit/tag-classes-test/default-exact-match.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);
        String xmlString = super.getGeneratedXMLOutput(mojo);

        // Check to see that all three lines of the comment are captured.
        String expected = "<td>This is the tag for the exact match default 1 of 1.</td>";
        assertTrue(htmlString.contains(expected), "Missing tag result.");

        // Check to see that all three lines of the comment are captured.
        String expectedCount = "<tag name=\"Test Exact Matches (default)\" count=\"1\">";
        assertTrue(xmlString.contains(expectedCount), "Incorrect tag count.");
    }

    /**
     * Test the exact match.
     *
     * @throws Exception
     */
    @Test
    void exactMatch() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-classes-test/exact-match.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);
        String xmlString = super.getGeneratedXMLOutput(mojo);

        // Check to see that all three lines of the comment are captured.
        String expected = "<td>This is hte tag for the exact match 1 of 1.</td>";
        assertTrue(htmlString.contains(expected), "Missing tag result.");

        // Check to see that all three lines of the comment are captured.
        String expectedCount = "<tag name=\"Test Exact Matches (configured)\" count=\"1\">";
        assertTrue(xmlString.contains(expectedCount), "Incorrect tag count.");
    }

    /**
     * Test the ignorecase match.
     *
     * @throws Exception
     */
    @Test
    void ignoreCaseMatch() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-classes-test/ignorecase-match.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);
        String xmlString = super.getGeneratedXMLOutput(mojo);

        // Check to see that all three lines of the comment are captured.
        String expected1 = "<td>ignore case 1 of 3.</td>";
        assertTrue(htmlString.contains(expected1), "Missing tag result #1.");
        String expected2 = "<td>ignore case 2 of 3.</td>";
        assertTrue(htmlString.contains(expected2), "Missing tag result #2.");
        String expected3 = "<td>ignore case 3 of 3.</td>";
        assertTrue(htmlString.contains(expected3), "Missing tag result #3.");

        // Check to see that all three lines of the comment are captured.
        String expectedCount = "<tag name=\"Test IgnoreCase Matches (configured)\" count=\"3\">";
        assertTrue(xmlString.contains(expectedCount), "Incorrect tag count.");
    }

    /**
     * Test the regular expression match.
     *
     * @throws Exception
     */
    @Test
    void regExMatch() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "/src/test/resources/unit/tag-classes-test/regex-match.xml");
        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput(mojo);
        String xmlString = super.getGeneratedXMLOutput(mojo);

        // Check to see that all three lines of the comment are captured.
        String expected1 = "<td>reg ex match 1 of 3.</td>";
        assertTrue(htmlString.contains(expected1), "Missing tag result #1.");
        String expected2 = "<td>reg ex match 2 of 3.</td>";
        assertTrue(htmlString.contains(expected2), "Missing tag result #2.");
        String expected3 = "<td>reg ex match 3 of 3.</td>";
        assertTrue(htmlString.contains(expected3), "Missing tag result #3.");

        // Check to see that all three lines of the comment are captured.
        String expectedCount = "<tag name=\"Test RegEx Matches (configured)\" count=\"3\">";
        assertTrue(xmlString.contains(expectedCount), "Incorrect tag count.");
    }
}
