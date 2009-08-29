package org.codehaus.mojo.taglist.tags;

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

/** The tag factory is used to create a tag based on the type of tag
 *  defined in the pom.xml.
 * 
 */
public class TagFactory 
{
    /** The XML name of the generic tag (case sensitive). */
    private static String genericTag = "exact";
    /** The XML name of the ignore case tag (case insensitive). */
    private static String ignorecaseTag = "ignoreCase";
    /** The XML name of the regular expression tag. */
    private static String regexTag = "regEx";
    
    
    /** Create a tag based on a tag type string.
     * 
     * @param tagType the XML string for the tag to create.
     * @param rule the tag string to use in matching tags.
     * @return the new tag. NULL if the tagType is unknown.
     * @throws InvalidTagException if the tagType is unknown
     */
    public static AbsTag createTag ( final String tagType, final String rule )
    throws InvalidTagException
    {
        AbsTag tag = null;
        
        if ( genericTag.equals( tagType ) )
        {
            tag = new GenericTag( rule );
        }
        else if ( ignorecaseTag.equals( tagType ) )
        {
            tag = new IgnoreCaseTag( rule );
        }
        else if ( regexTag.equals( tagType ) )
        {
            tag = new RegExTag( rule );
        }
        else 
        {
            throw new InvalidTagException( tagType );
        }
        
        return ( tag );
    }
    
    /** Returns the default tag type if one is not specified.
     * 
     * @return the default tag type string
     */
    public static final String getDefaultTagType()
    {
        return ( genericTag );
    }
    
    /** Private constructor.  This is a utility class.
     * 
     */
    private TagFactory ()
    {
        // Do nothing
    }
}
