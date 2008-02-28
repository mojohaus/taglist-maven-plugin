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

import org.codehaus.plexus.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Report for a file.
 * 
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard </a>
 */
public class FileReport
    implements Comparable
{

    /**
     * The file being analysed.
     */
    private File file;

    /**
     * The name of the class corresponding to this file.
     */
    private String className;

    /**
     * Pair values of a line number and a comment. Map of [Integer,String].
     */
    private Map tagListing;

    /**
     * The package identification string.
     */
    private static final String PACKAGE_STR = "package";

    /**
     * Constructor.
     * 
     * @param file The file to analyse.
     */
    public FileReport( File file )
    {
        this.file = file;
        this.tagListing = new HashMap();
    }

    /**
     * Adds a new entry to the list of tags found for this file report.
     * 
     * @param comment the comment string containing the 'todo'.
     * @param lineIndex the line number of the comment (or first line if multi-lined).
     */
    public void addComment( String comment, int lineIndex )
    {
        tagListing.put( new Integer( lineIndex ), comment );
    }

    /**
     * Returns the path corresponding to the analysed class, for instance:
     * org/apache/maven/plugins/taglist/beans/FileReport.
     * 
     * @return the file path.
     */
    public String getClassNameWithSlash()
    {
        return className.replace( '.', '/' );
    }

    /**
     * Returns the complete name of the analysed class, for instance: org.codehaus.mojo.taglist.beans.FileReport.
     * 
     * @return the full class name.
     */
    public String getClassName()
    {
        if ( className != null )
        {
            return className;
        }
        // need to compute it (only once)
        BufferedReader reader = null;
        String packageName = null;
        try
        {
            reader = new BufferedReader( new FileReader( file ) );
            String currentLine = reader.readLine();
            if ( currentLine != null )
            {
                currentLine = currentLine.trim();
            }
            while ( currentLine != null )
            {
                if ( currentLine.startsWith( PACKAGE_STR ) )
                {
                    packageName = currentLine.substring( PACKAGE_STR.length() ).trim().replaceAll( ";", "" ).trim();
                    break;
                }
                String nextLine = reader.readLine();
                if ( nextLine == null )
                {
                    currentLine = nextLine;
                }
                else
                {
                    currentLine = nextLine.trim();
                }
            }
        }
        catch ( IOException e )
        {
            packageName = "unknown";
        }
        finally
        {
            IOUtil.close( reader );
        }

        className = packageName + "." + file.getName().replaceAll( ".java", "" );

        return className;
    }

    /**
     * Returns the list of the comment line indexes.
     * 
     * @return Collection of Integer.
     */
    public Collection getLineIndexes()
    {
        ArrayList list = new ArrayList();
        list.addAll( tagListing.keySet() );
        Collections.sort( list );
        return list;
    }

    /**
     * Returns the comment for the corresponding line index.
     * 
     * @param lineIndex the index of the line.
     * @return the comment.
     */
    public String getComment( Integer lineIndex )
    {
        return (String) tagListing.get( lineIndex );
    }

    /**
     * Cf. overriden method documentation. {@inheritDoc}
     * 
     * @see Comparable#compareTo(Object)
     */
    public int compareTo( Object o )
    {
        if ( o instanceof FileReport )
        {
            FileReport fileReport = (FileReport) o;
            return this.getClassName().compareTo( fileReport.getClassName() );
        }
        else
        {
            return 0;
        }
    }

}
