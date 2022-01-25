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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Report for a file.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard </a>
 */
public class FileReport
        implements Comparable<FileReport>
{

    /**
     * The file being analyzed.
     */
    private final File file;

    /**
     * The character encoding of the source file
     */
    private final String encoding;

    /**
     * The name of the class corresponding to this file.
     */
    private String className;

    /**
     * Pair values of a line number and a comment. Map of [Integer,String].
     */
    private final Map<Integer, String> tagListing;

    /**
     * The package identification string.
     */
    private static final String PACKAGE_STR = "package";

    /**
     * Constructor.
     *
     * @param file The file to analyze.
     * @param encoding the file encoding to use for the report.
     */
    public FileReport( File file, String encoding )
    {
        this.file = file;
        this.encoding = encoding;
        this.tagListing = new HashMap<>();
    }

    /**
     * Adds a new entry to the list of tags found for this file report.
     *
     * @param comment the comment string containing the 'todo'.
     * @param lineIndex the line number of the comment (or first line if multi-lined).
     */
    public void addComment( String comment, int lineIndex )
    {
        tagListing.put( lineIndex, comment );
    }

    /**
     * Returns the path corresponding to the analyzed class, for instance:
     * org/apache/maven/plugins/taglist/beans/FileReport.
     *
     * @return the file path.
     */
    public String getClassNameWithSlash()
    {
        return className.replace( '.', '/' );
    }

    /**
     * Access an input reader that uses the current file encoding.
     *
     * @param fileToRead the file to open in the reader.
     * @throws IOException the IO exception.
     * @return a reader with the current file encoding.
     */
    private Reader getReader( File fileToRead ) throws IOException
    {
        InputStream in = new FileInputStream( fileToRead );
        return ( encoding == null ) ? new InputStreamReader( in ) : new InputStreamReader( in, encoding );
    }

    /**
     * Returns the complete name of the analyzed class, for instance: org.codehaus.mojo.taglist.beans.FileReport.
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
        String packageName = null;
        try ( BufferedReader reader = new BufferedReader( getReader( file ) ) )
        {
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

        className = packageName + "." + file.getName().replaceAll( "\\.java$", "" );

        return className;
    }

    /**
     * Returns the list of the comment line indexes.
     *
     * @return Collection of Integer.
     */
    public Collection<Integer> getLineIndexes()
    {
        SortedSet<Integer> lineIndexes = new TreeSet<>();
        lineIndexes.addAll( tagListing.keySet() );
        return lineIndexes;
    }

    /**
     * Returns the comment for the corresponding line index.
     *
     * @param lineIndex the index of the line.
     * @return the comment.
     */
    public String getComment( Integer lineIndex )
    {
        return tagListing.get( lineIndex );
    }

    /**
     * {@inheritDoc}
     *
     * @see Comparable#compareTo(Object)
     */
    public int compareTo( FileReport o )
    {
        if ( o != null )
        {
            return this.getClassName().compareTo( o.getClassName() );
        }
        else
        {
            return 0;
        }
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
        if ( !( r instanceof FileReport ) )
        {
            return false;
        }
        return ( this.compareTo( (FileReport) r ) == 0 );
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
