package org.codehaus.mojo.taglist.beans;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Report of the scan for a specific tag.
 * 
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard </a>
 */
public class TagReport
    implements Comparable
{

    /**
     * Tag Class display name.
     */
    private String displayName;
    
    /**
     * Tag Class HTML safe link name.
     */
    private String linkName;
    
    /**
     * An array containing the tag string that make the tag class.
     */
    private ArrayList tagStrings = new ArrayList();

    /**
     * Map containing File objects as keys, and FileReport object as values.
     */
    private Map fileReportsMap;

    /**
     * Number of tags found in the code.
     */
    private int tagCount;

    /**
     * Constructor.
     * 
     * @param displayName the tag class's name.
     * @param linkName a HTML safe link name for this report.
     */
    public TagReport( final String displayName, final String linkName )
    {
        this.displayName = displayName;
        this.fileReportsMap = new HashMap();
        this.linkName = linkName;
        tagCount = -1;
    }

    /**
     * Returns the FileReport object corresponding to this file. If it does not exist yet, it will be created.
     * 
     * @param file the file being analyzed.
     * @param encoding the character encoding of the file
     * @return a FileReport object for this file.
     */
    public FileReport getFileReport( File file, String encoding )
    {
        Object report = fileReportsMap.get( file );
        if ( report instanceof FileReport )
        {
            return (FileReport) report;
        }
        else
        {
            FileReport newFileReport = new FileReport( file, encoding );
            fileReportsMap.put( file, newFileReport );
            return newFileReport;
        }
    }

    /**
     * Returns the collection of file reports for the tag.
     * 
     * @return a Collection of FileReport objects.
     */
    public Collection getFileReports()
    {
        return fileReportsMap.values();
    }

    /**
     * Returns the name of the tag class that was looked for.
     * 
     * @return the name of the tag class.
     */
    public String getTagName()
    {
        return displayName;
    }
    
    /**
     * Returns a HTML safe link name for this tag report.
     * 
     * @return a HTML safe link name.
     */
    public String getHTMLSafeLinkName()
    {
        return linkName;
    }

    /**
     * Gives the number of comments found for that tag.
     * 
     * @return the number of comments.
     */
    public int getTagCount()
    {
        if ( tagCount > -1 )
        {
            return tagCount;
        }
        // tagCount was not computed yet
        tagCount = 0;
        for ( Iterator iter = fileReportsMap.values().iterator(); iter.hasNext(); )
        {
            FileReport fileReport = (FileReport) iter.next();
            tagCount += fileReport.getLineIndexes().size();
        }
        return tagCount;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Comparable#compareTo(Object)
     */
    public int compareTo( Object o )
    {
        if ( o instanceof TagReport )
        {
            TagReport tagReport = (TagReport) o;
            return this.getTagName().compareTo( tagReport.getTagName() );
        }
        else
        {
            return 0;
        }
    }
    
    /** Add a tag string to this tag class. 
     *  Each tag class contains 1 or more tag strings that are used
     *  for matching 'todo' strings in the scanned code. 
     *  
     * @param tagString the tag string to add.
     */
    public void addTagString ( final String tagString )
    {
        if ( tagString != null )
        {
            tagStrings.add( tagString );
        }
    }
    
    /** Get a list of tag strings used by this tag report.
     * 
     * @return a list of tag strings.
     */
    public String [] getTagStrings ()
    {
        
        String [] strings = null;
        
        if ( tagStrings.size() > 0 )
        {
            strings = new String [tagStrings.size()];

            for ( int i = 0; i < tagStrings.size(); ++i )
            {
                strings[i] = (String) tagStrings.get( i );
            }
        }
        
        return ( strings );
        
    }
    
    /**
     * {@inheritDoc}
     *
     * @see Object#equals(Object)
     */
    public boolean equals( Object r )
    {
        // In Java 5 the PriorityQueue.remove method uses the 
        // compareTo method, while in Java 6 it uses the equals method.
        return ( this.compareTo( r ) == 0 );
    }
    
    /**
     * {@inheritDoc}
     *
     * @see Object#hashCode()
     */
    public int hashCode() 
    {
        assert false : "hashCode not designed";
        return 1; // any arbitrary constant will do 
    }

}
