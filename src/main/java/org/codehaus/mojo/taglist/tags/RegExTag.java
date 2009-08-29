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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

/** The regular expression tag class.
 * 
 *  This class defines a regular express tag search.  This tag
 *  will match if the regular express string listed is found within
 *  the scanned lines.
 *  
 *  Example POM:
 *  <pre>
 *   <project >
 *       ...
 *       <matchString>fixme[0-9]</matchString>
 *      <matchType>regEx</matchType>
 *       ...
 *   </project>
 *  </pre>
 *  
 *  Example Java code with match:
 *  <pre>
 *    fixme1 this will match (fixme followed by one digit.)
 *  </pre>
 *  
 *  Example Java code without match:
 *  <pre>
 *     fixme this will NOT match (no digit after fixme)
 *  </pre>
 *       
 */
public class RegExTag extends AbsTag
{
    /** The regular expression pattern to pre-compile. */
    private Pattern pattern = null;
    
    /** The length of the last regEx comment tag match */
    private int lastMatchedCommentTagLength = 0;

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
            Matcher m = pattern.matcher( currentLine );
            if ( m.find() )
            {
                result = m.start();
                
                // Store the length of the comment tag.
                lastMatchedCommentTagLength = m.end() - m.start();
            }
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
            Matcher m = pattern.matcher( currentLine );
            if ( m.find() )
            {
                // Was the match at the first character?
                result = m.start() == 0;
            }
        }
        
        return ( result );
    }
    
    /** Return the length of the last matched tag.
     * 
     * A regular expression tag might be 10 characters; however, 
     * the matched string may only be 5.
     * 
     * Example:
     *    regEx tag:       a*b    (tag length = 3)
     *    
     *    matched comment: aaabbb (comment length = 6)
     *    
     * In the above example, this function will return 6.
     *  
     * @return the length of the last matched tag.
     */
    public int getLastTagMatchLength()
    {
        return ( lastMatchedCommentTagLength );
    }

    /** Constructor.
     * 
     * @param tagString the string to match against for this tag.
     */
    public RegExTag ( final String tagString )
    {
        super( tagString );
        
        // Pre-compile the regular expression
        pattern = Pattern.compile( tagString );

    }

}
