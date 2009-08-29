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
import java.util.ResourceBundle;

/**
 * Test the Taglist mojo locale support
 *
 * @version $Id$
 */
public class TaglistMojoLocalesTest
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
     * Test when locale is set to English.
     *
     * @throws Exception
     */
    public void testEnglishLocale()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/locale-test/english-locale.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );
        String xmlString = super.getGeneratedXMLOutput( mojo );


        // Check to see that all three lines of the comment are captured.
        String expected1 = "<td>Should match under Locale en  1 of 3.</td>";
        assertTrue("Missing tag result #1.", htmlString.indexOf(expected1) != -1);
        String expected2 = "<td>Should match under Locale en  2 of 3.</td>";
        assertTrue("Missing tag result #2.", htmlString.indexOf(expected2) != -1);
        String expected3 = "<td>Should match under Locale en  3 of 3.</td>";
        assertTrue("Missing tag result #3.", htmlString.indexOf(expected3) != -1);
        
        // Check to see that all three lines of the comment are captured.
        String expectedCount = "<tag name=\"EnglishLocale\" count=\"3\">";
        assertTrue("Incorrect tag count.", xmlString.indexOf(expectedCount) != -1);
    }
    
    /**
     * Test when locale is set to Turkish.
     *
     * @throws Exception
     */
    public void testTurkishLocale()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/locale-test/turkish-locale.xml" );
        TagListReport mojo = super.getTagListReport( pluginXmlFile );

        // Run the TagList mojo
        mojo.execute();

        String htmlString = super.getGeneratedOutput( mojo );
        String xmlString = super.getGeneratedXMLOutput( mojo );


        // Check to see that all three lines of the comment are captured.
        String expected1 = "<td>Should match under Locale tr  1 of 2.</td>";
        assertTrue("Missing tag result #1.", htmlString.indexOf(expected1) != -1);
        String expected2 = "<td>Should match under Locale tr  2 of 2.</td>";
        assertTrue("Missing tag result #2.", htmlString.indexOf(expected2) != -1);
        
        // Check to see that all three lines of the comment are captured.
        String expectedCount = "<tag name=\"Turkish Locale\" count=\"2\">";
        assertTrue("Incorrect tag count.", xmlString.indexOf(expectedCount) != -1);
    }
    
}
