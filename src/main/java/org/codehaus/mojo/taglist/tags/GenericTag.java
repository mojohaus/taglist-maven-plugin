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

/** The generic tag class.
 * 
 *  This class defines a generic "case sensitive" exact match tag.  This tag
 *  will only match if the exact string listed is found within
 *  the scanned lines.
 *  
 *  Example POM:
 *  <pre>
 *   <project >
 *       ...
 *       <matchString>fixme</matchString>
 *       ...
 *   </project>
 *  </pre>
 *  
 *  Example Java code with match:
 *  <pre>
 *     * fixme this will NOT match.
 *  </pre>
 *  
 *  Example Java code without match:
 *  <pre>
 *     * Fixme this will NOT match.
 *  </pre>
 *       
 */
public class GenericTag extends AbsTag
{
    /** Check to see if the string contains this tag.  
     *  If there is a match, return the index within the string; otherwise,
     *  return NO_MATCH.
     *  
     *  @param currentLine the string for the current line being scanned.
     *  @param locale the Locale of the currentLine.
     *  @return the index within the string of the matched tag, or TagClass.NO_MATCH
     *  if not match was found.
     */
    public int contains( final String currentLine, final Locale locale )
    {
        int result = AbsTag.NO_MATCH;
        
        if ( currentLine != null )
        {
            // Get index match or -1 if no match
            result = currentLine.indexOf( tagString );
        }
        
        return ( result );
    }
    
    /** Check to see if the string starts with this tag.  
     *  
     *  @param currentLine the string for the current line being scanned.
     *  @param locale the Locale of the currentLine.
     *  @return true if the string starts with this tag.
     */
    public boolean startsWith( final String currentLine, final Locale locale )
    {
        boolean result = false;
        
        if ( currentLine != null )
        {
            result = currentLine.startsWith( tagString );
        }
        
        return ( result );
    }
    
    /** Return the length of the last matched tag.
     * 
     * In the case of a generic tag, this is always the length of the tag.
     *  
     * @return the length of the last matched tag.
     */
    public int getLastTagMatchLength()
    {
        return ( tagString.length() );
    }

    /** Constructor.
     * 
     * @param exactTagString the string to match against for this tag.
     */
    public GenericTag ( final String exactTagString )
    {
        super( exactTagString );
    }

}
