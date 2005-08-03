package org.apache.maven.plugin.taglist.beans;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Report of the scan for a specific tag
 * 
 * @author <a href="mailto:bellingard@gmail.com">Fabrice Bellingard </a>
 */
public class TagReport
    implements Comparable
{

    /**
     * Tag name
     */
    private String tagName;

    /**
     * Map containing File objects as keys, and FileReport object
     * as values.
     */
    private Map fileReportsMap;

    /**
     * Number of tags found in the code.
     */
    private int tagCount;

    /**
     * Constructor
     * @param tagName
     * @param fileReports
     */
    public TagReport( String tagName )
    {
        this.tagName = tagName;
        this.fileReportsMap = new HashMap();
        tagCount = -1;
    }

    /**
     * Returns the FileReport object corresponding to this file.
     * If it does not exist yet, it will be created.
     * @param file the file being analysed
     * @return a FileReport object for this file
     */
    public FileReport getFileReport( File file )
    {
        Object report = fileReportsMap.get( file );
        if ( report instanceof FileReport )
        {
            return (FileReport) report;
        }
        else
        {
            FileReport newFileReport = new FileReport( file );
            fileReportsMap.put( file, newFileReport );
            return newFileReport;
        }
    }

    /**
     * Returns the collection of file reports for the tag.
     * @return a Collection of FileReport objects.
     */
    public Collection getFileReports()
    {
        return fileReportsMap.values();
    }

    /**
     * Returns the name of the tag that was looked for.
     * @return the name of the tag.
     */
    public String getTagName()
    {
        return tagName;
    }

    /**
     * Gives the number of comments found for that tag.
     * @return the number of comments
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
     * Cf. overriden method documentation.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
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

}
