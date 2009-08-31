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

import org.codehaus.plexus.util.FileUtils;

/**
 * Test the Taglist mojo basic configurations.
 *
 * @version $Id$
 */
public class TaglistMojoBasicConfigTest
    extends AbstractTaglistMojoTestCase
{

    /** {@inheritDoc} */
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    /** {@inheritDoc} */
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    /**
     * Test that all of the default settings are correct.
     *
     * @throws Exception
     */
    public void testCreateTagListOutput()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/create-output-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );

        // Check to see is the com.Basic file was processed.
        String expected = "<tr class=\"a\"><th>com.BasicConfig</th>";
        assertTrue("Missing tag result.", htmlString.indexOf(expected) != -1);
    }

    /**
     * Test support of multiple line comments enabled.
     *
     * @throws Exception
     */
    public void testMultipleLineCommentsEnabled()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/multiple-line-comments-enabled-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );

        // Check to see that all three lines of the comment are captured.
        String expected = "<td>This is line one, this is line two, and this is line three.</td>";
        assertTrue("Missing tag result.", htmlString.indexOf(expected) != -1);
    }

    /**
     * Test support of multiple line comments disabled.
     *
     * @throws Exception
     */
    public void testMultipleLineCommentsDisabled()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/multiple-line-comments-disabled-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        // Run the TagList mojo
        mojo.execute();
        String htmlString = super.getGeneratedOutput( mojo );

        // Check to see that only the first line of the comment is captured.
        String expected = "<td>This is line one,</td>";
        assertTrue("Missing tag result.", htmlString.indexOf(expected) != -1);
    }

    /**
     * Test support of empty comments enabled.
     *
     * @throws Exception
     */
    public void testEmptyCommentsEnabled()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/empty-comments-enabled-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        mojo.execute();
        String htmlString = super.getGeneratedOutput( mojo );

        // Check to see that there was only one occurrence.
        String expected = "<b>Number of occurrences found in the code: 1</b>";
        assertTrue("Missing tag result.", htmlString.indexOf(expected) != -1);

        // Use the resource bundle to determine what the no comment string
        // is for the current locale.
        ResourceBundle bundle = ResourceBundle.getBundle( "taglist-report" );
        String noComment = bundle.getString("report.taglist.nocomment");

        // Check to see that the empty comment message was entered
        expected = "<td>--" + noComment + "--</td>";
        assertTrue("Incorrect no comment message.", htmlString.indexOf(expected) != -1);
    }

    /**
     * Test support of empty comments disabled.
     *
     * @throws Exception
     */
    public void testEmptyCommentsDisabled()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/empty-comments-disabled-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );

        // Check to see that there was zero tags found
        String expected = "<td>@empty_comment</td><td>0</td>";
        assertTrue("Missing tag result.", htmlString.indexOf(expected) != -1);
    }

    /**
     * Test support "tag" and "tag:" being the same tag.
     *
     * @throws Exception
     */
    public void testColonInComment()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/colons-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );
        String xmlString = super.getGeneratedXMLOutput( mojo );

        // Check to see that there were two tags found
        String expected = "<tag name=\"@colons\" count=\"2\">";
        assertTrue("Incorrect number of colon matches.", xmlString.indexOf(expected) != -1);

        // Check for the tag without the colon
        expected = "<td>This is without colon.</td>";
        assertTrue("Missing without colon tag result.", htmlString.indexOf(expected) != -1);

        // Check for the tag with the colon
        expected = "<td>This is with colon.</td>";
        assertTrue("Missing with tag result.", htmlString.indexOf(expected) != -1);
    }

    /**
     * Test support empty "tag" and "tag:".
     *
     * @throws Exception
     */
    public void testColonInEmptyComment()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/empty-colons-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );

        // Use the resource bundle to determine what the no comment string
        // is for the current locale.
        ResourceBundle bundle = ResourceBundle.getBundle( "taglist-report", Locale.ENGLISH );
        String noComment = bundle.getString("report.taglist.nocomment");

        // Check to see that there is a comment for line #40 (empty_no_colons)
        // Since there is no text following the tag, the "No Comment" string should
        // be inserted by the TagList Plugin
        String expected = "<td>--" + noComment + "--</td><td>40</td>";
        assertTrue("Incorrect empty tag without colon text.", htmlString.indexOf(expected) != -1);

        // Check to see that there is a comment for line #43 (empty_colons)
        // Since there is no text following the tag, the "No Comment" string should
        // be inserted by the TagList Plugin
        expected = "<td>--" + noComment + "--</td><td>43</td>";
        assertTrue("Incorrect empty tag with colon text.", htmlString.indexOf(expected) != -1);
    }

    /**
     * Test support of show empty details enabled.
     *
     * @throws Exception
     */
    public void testShowEmptyDetailsEnabled()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/show-empty-details-enabled-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );

        // Check to see show empty tags in code flag has a count of 1.
        String expected = "@show_empty_details_tag_in_code</a></td><td>1</td>";
        assertTrue("Incorrect count for the in code tag.", htmlString.indexOf(expected) != -1);

        // Check to see show empty tags in not code flag has a count of 0.
        expected = "@show_empty_details_tag_not_in_code</a></td><td>0</td>";
        assertTrue("Incorrect count for the not in code tag.", htmlString.indexOf(expected) != -1);

        // Check to see show empty tags in code section details exist (they should).
        expected = "\">@show_empty_details_tag_in_code</a></h3>";
        assertTrue("Missing tag details for the in code tag.", htmlString.indexOf(expected) != -1);

        // Check to see show empty tags not in code section details exist (they should).
        expected = "\">@show_empty_details_tag_not_in_code</a></h3>";
        assertTrue("Missing tag details for the not in code tag.", htmlString.indexOf(expected) != -1);
    }

    /**
     * Test support of show empty details disabled.
     *
     * @throws Exception
     */
    public void testShowEmptyDetailsDisabled()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/show-empty-details-disabled-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );

        // Check to see show empty tags in code flag has a count of 1.
        String expected = "@show_empty_details_tag_in_code</a></td><td>1</td>";
        assertTrue("Incorrect count for the in code tag.", htmlString.indexOf(expected) != -1);

        // Check to see show empty tags in not code flag has a count of 0.
        // FYI: Since there are no details, there is no ending hyperlink tag (</a>).
        expected = "@show_empty_details_tag_not_in_code</td><td>0</td>";
        assertTrue("Incorrect count for the not in code tag.", htmlString.indexOf(expected) != -1);

        // Check to see show empty tags in code section details exist (they should).
        expected = "\">@show_empty_details_tag_in_code</a></h3>";
        assertTrue("Missing tag details for the in code tag.", htmlString.indexOf(expected) != -1);

        // Check to see show empty tags not in code section details do NOT exist (they should not).
        expected = "\">@show_empty_details_tag_not_in_code</a></h3>";
        assertFalse("Unexpected tag details for the not in code tag.", htmlString.indexOf(expected) != -1);
    }

    public void testXmlFile()
        throws Exception
    {
    	File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/basic-config-test/xml-output-pom.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );
        mojo.execute();
        
        String actualXml = super.getGeneratedXMLOutput( mojo );
        actualXml = actualXml.replaceAll( "(\r\n)|(\r)", "\n" );
        
        File expectedFile = new File( getBasedir(), "/target/test-classes/unit/basic-config-test/expected-taglist.xml" );       
        String expectedXml = FileUtils.fileRead( expectedFile, TEST_ENCODING );
        expectedXml = expectedXml.replaceAll( "(\r\n)|(\r)", "\n" );

        assertEquals( "unexpected contents", expectedXml, actualXml );
    }
}
