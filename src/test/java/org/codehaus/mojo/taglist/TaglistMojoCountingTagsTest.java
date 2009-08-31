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
 * Test the Taglist mojo tags displayed on the website.
 *
 * @version $Id$
 */
public class TaglistMojoCountingTagsTest
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
     * Test the counting tags example from the TagList website.
     * 
     * The purpose of this test is to guarantee that the information
     * listed on the website matches what the taglist plugin will return.
     *
     * @throws Exception
     */
    public void testCountingTags()
        throws Exception
    {
    	File pluginXmlFile = new File( getBasedir(), "/src/test/resources/unit/counting-tags-test/counting-tags-pom.xml" );
    	TagListReport mojo = super.getTagListReport( pluginXmlFile );
        
        // Run the TagList mojo
        mojo.execute();
        String htmlString = super.getGeneratedOutput( mojo );
        
        // Check that NOT_YET_DOCUMENTED does not show up.
        String expected = "<td>NOT_YET_DOCUMENTED</td><td>0</td>";
        assertTrue("Incorrect NOT_YET_DOCUMENTED tag result.", htmlString.indexOf(expected) != -1);
        
        // Check that FIXME has one tag.
        expected = "\">FIXME</a></td><td>1</td>";
        assertTrue("Incorrect FIXME tag result.", htmlString.indexOf(expected) != -1);
        
        // Check that DOCUMENT_ME does not show up.
        expected = "<td>DOCUMENT_ME</td><td>0</td>";
        assertTrue("Incorrect NOT_YET_DOCUMENTED tag result.", htmlString.indexOf(expected) != -1);
        
        // Check that <todo has one tag.
        expected = "\">&lt;todo</a></td><td>1</td>";
        assertTrue("Incorrect <todo tag result.", htmlString.indexOf(expected) != -1);
        
        // Check that @todo does not show up anywhere in the output.
        String notExpected = "@todo";
        assertTrue("Incorrect @todo tag result.", htmlString.indexOf(notExpected) == -1);
    }
}
