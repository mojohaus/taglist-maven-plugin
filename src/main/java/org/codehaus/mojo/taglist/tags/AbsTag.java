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

import java.util.Locale;

/** The abstract base class for tags.
 * 
 *  This class defines the required functions that each type of 
 *  tag must implement.  The goal here is to allow different types
 *  of tags to be created by the user, but the call processing 
 *  for each tag can be generic. 
 *  
 *  For example, a generic tag might search files for an exact string
 *  match of the tag, but a regex tag will use regular expressions in
 *  its searching.  Either way, the processing classes call each tag
 *  with the same function and get the same results. 
 * 
 */
public abstract class AbsTag
{
    /** The int value for no tag match found.
     *  This value matches what is returned from Java String class 
     *  indexOf(..string..) function. */
    static final int NO_MATCH = -1;
    
    /** The tag string for this tag. The string here is generic, and its
     *  use is defined by the derived tag objects.
     */
    protected String tagString = null;
    
    
    /** Check to see if the string contains this tag.  
     *  If there is a match, return the index within the string; otherwise,
     *  return NO_MATCH.
     *  
     *  @param currentLine the string for the current line being scanned.
     *  @param locale the Locale of the currentLine.
     *  @return the index within the string of the matched tag, or TagClass.NO_MATCH
     *  if not match was found.
     */
    public abstract int contains( final String currentLine, final Locale locale );
    
    /** Check to see if the string starts with this tag.  
     *  If there is a match, return true.
     *  
     *  @param currentLine the string for the current line being scanned.
     *  @param locale the Locale of the currentLine.
     *  @return true if the string starts with this tag.
     */
    public abstract boolean startsWith( final String currentLine, final Locale locale );
    
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
    public abstract int getLastTagMatchLength();

    /** Constructor.
     * 
     * @param tag the tag string to be used for this tag.
     */
    protected AbsTag ( final String tag )
    {
        tagString = tag;
    }

}
