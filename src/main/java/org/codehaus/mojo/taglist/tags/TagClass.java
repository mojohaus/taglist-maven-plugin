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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.codehaus.mojo.taglist.beans.TagReport;


/**
 * Class that define a classification of tags.
 * 
 * Each tag class contains 1 or more tags.  This allows a user to define
 * one tag "name" for display purposes, while still checking the files for
 * multiple tag rules.
 * 
 *  Example
 *  <pre>
 *   <tagClass>
 *    <displayName>Action Items</displayName>
 *    <tags>
 *     <tag>
 *      <matchString>todo</matchString>
 *      <matchType>ignoreCase</matchType>
 *     </tag>
 *    </tags>
 *   </tagClass>
 *  </pre>
 * 
 */
public class TagClass
{
    /** The tag class's name. */
    private String classDisplayName = null;
    
    /** The tag report for this tag class. */
    private TagReport classTagReport = null;
    
    /** The container of tags that make up this tag class. */
    private ArrayList tags = new ArrayList();
    
    /** The int value for no tag match found. */
    public static final int NO_MATCH = AbsTag.NO_MATCH;
    
    /** The last tag to successfully match. */
    private AbsTag lastSuccessfulTagMatch = null;
    
    /** A unique ID counter for the tag classes. */
    private static int uniqueTcCounter = 1;
    
    /** The unique id for this tag class. */
    private int uniqueId = 0;
   
    /**
     * Constructor.
     * 
     * @param displayName the string to display as the name for this tag class.
     */
    public TagClass( final String displayName )
    {
        classDisplayName = displayName;
        
        // Assign a unique ID for this tag class and update the global counter.
        uniqueId = uniqueTcCounter++;
        
        classTagReport = new TagReport( displayName, "tag_class_" + String.valueOf( uniqueId ) );
    }

    /** Access the tag report for this tag class.
     * 
     * @return the tag class's tag report.
     */
    public TagReport getTagReport ()
    {
        return ( classTagReport );
    }
    
    /** Add a tag to this tag class.
     *  
     * @param tag the tag to add to the tag class.
     */
    public void addTag ( AbsTag tag )
    {
        if ( tag != null )
        {
            tags.add( tag );
        
            classTagReport.addTagString( tag.tagString );
        }            
    }
    
    /** Get the index of the first tag contained from within a string. 
     *  
     *  The tag class will check each for its tags until a match is found
     *  within the specified string.  If no match is found, this function will
     *  return TagClass.NO_MATCH for the index. 
     *  
     *  @param currentLine the string for the current line being scanned.
     *  @param locale the Locale of the currentLine.
     *  @return the index within the string of the matched tag, or TagClass.NO_MATCH
     *  if not match was found.
     */
    public int tagMatchContains ( final String currentLine, final Locale locale )
    {
        int index = NO_MATCH;
        
        // Reset the last tag match
        lastSuccessfulTagMatch = null;
        
        Iterator itr = tags.iterator();      
        while ( itr.hasNext() )
        {
            AbsTag tag = (AbsTag) itr.next();
            
            // Check if the string contain this tag
            index = tag.contains( currentLine, locale );
            
            if ( index != NO_MATCH )
            {
                // Store the last match
                lastSuccessfulTagMatch = tag;
                
                // Stop checking
                break;
            }
        }
        
        return index;
    }
    
    /** Check if a string starts with a tag from this tag class.
     *  
     *  The tag class will check each of its tags until the start of the string
     *  matched one of the tags.  If not match if found, false is returned.
     *  
     *  @param currentLine the string for the current line being scanned.
     *  @param locale the Locale of the currentLine.
     *  @return true if the string starts with a tag within this tag class.
     *  Otherwise false is returned.
     */
    public boolean tagMatchStartsWith ( final String currentLine, final Locale locale )
    {
        boolean match = false;
        
        Iterator itr = tags.iterator();
        
        // Loop while there are more tags and there has not been a match.
        while ( itr.hasNext() && !match )
        {
            AbsTag tag = (AbsTag) itr.next();
            
            // Check if the string starts with this tag
            match = tag.startsWith( currentLine, locale );
        }
        
        return match;
    }

   /** Return the tag string for the last successfully matched tag.
    * 
    * @return string of the last matched tag.
    */
   public String getLastTagMatchString()
   {
       if ( lastSuccessfulTagMatch == null )
       {
           return ( "" );
       }
       else
       {
           return ( lastSuccessfulTagMatch.tagString );
       }
   }
   
   /** Return the length of the last matched tag.
    * 
    * Normally this is the length of the tag; however, some tags
    * are dynamic.  For example a regular expression tag might be
    * 10 characters; however, the matched string may only be 5.
    * 
    * Calling this function allows the tag object to return the
    * correct length for the last matched tag.
    *  
    * @return the length of the last matched tag.
    */
   public int getLastTagMatchStringLength()
   {
       if ( lastSuccessfulTagMatch == null )
       {
           return ( 0 );
       }
       else
       {
           return ( lastSuccessfulTagMatch.getLastTagMatchLength() );
       }
   }
   
   /** Get the display name of this tag class.
    * 
    * @return the tag class display name.
    */
   public String getDisplayName ()
   {
       return ( classDisplayName );
   }

}
