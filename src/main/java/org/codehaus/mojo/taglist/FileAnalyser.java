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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.mojo.taglist.beans.FileReport;
import org.codehaus.mojo.taglist.beans.TagReport;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Class that analyses a file with a special comment tag. For instance:
 * 
 * <pre>
 * // TODO: Example of an Eclipse/IntelliJ-like "todo" tag
 * </pre>
 * 
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard </a>
 * @todo : This is another example of "todo" tag
 */
public class FileAnalyser
{
    /**
     * String that is used for beginning a comment line.
     */
    private static final String STAR_COMMENT = "*";

    /**
     * String that is used for beginning a comment line.
     */
    private static final String SLASH_COMMENT = "//";

    /**
     * The directories to analyse.
     */
    private Collection sourceDirs;

    /**
     * Log for debug output.
     */
    private Log log;

    /**
     * Map containing tag names as keys ("TODO" or "@todo" for instance), and a TagReport object as value.
     */
    private Map tagReportsMap;

    /**
     * Set to true if the analyser should look for multiple line comments.
     */
    private boolean multipleLineCommentsOn;

    /**
     * Set to true if the analyser should look for tags without comments.
     */
    private boolean emptyCommentsOn;

    /**
     * String used to indicate that there is no comment after the tag.
     */
    private String noCommentString;

    /**
     * Constructor.
     * 
     * @param report the MOJO that is using this analyser.
     */
    public FileAnalyser( TagListReport report )
    {
        multipleLineCommentsOn = report.isMultipleLineComments();
        emptyCommentsOn = report.isEmptyComments();
        log = report.getLog();
        sourceDirs = report.constructSourceDirs();
        noCommentString = report.getBundle().getString( "report.taglist.nocomment" );
        // init the map of tag reports
        String[] tags = report.getTags();
        tagReportsMap = new HashMap( tags.length );
        for ( int i = 0; i < tags.length; i++ )
        {
            String tagName = tags[i];
            TagReport tagReport = new TagReport( tagName );
            tagReportsMap.put( tagName, tagReport );
        }
    }

    /**
     * Execute the analysis for the configuration given by the TagListReport.
     * 
     * @return a collection of TagReport objects.
     * @throws MavenReportException the Maven report exception.
     */
    public Collection execute()
        throws MavenReportException
    {
        List fileList = findFilesToScan();

        for ( Iterator iter = fileList.iterator(); iter.hasNext(); )
        {
            File file = (File) iter.next();
            if ( file.exists() )
            {
                scanFile( file );
            }
        }

        return tagReportsMap.values();
    }

    /**
     * Gives the list of files to scan.
     * 
     * @return a List of File objects.
     * @throws MavenReportException the Maven report exception.
     */
    private List findFilesToScan()
        throws MavenReportException
    {
        List filesList = new ArrayList();
        try
        {
            for ( Iterator iter = sourceDirs.iterator(); iter.hasNext(); )
            {
                filesList.addAll( FileUtils.getFiles( new File( (String) iter.next() ), "**/*.java", null ) );
            }
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Error while trying to find the files to scan.", e );
        }
        return filesList;
    }

    /**
     * Scans a file to look for task tags.
     * 
     * @param file the file to scan.
     */
    public void scanFile( File file )
    {
        LineNumberReader reader = null;

        try
        {
            reader = new LineNumberReader( new FileReader( file ) );

            String currentLine = reader.readLine();
            while ( currentLine != null )
            {
                int index = -1;
                String tagName = null;

                int[] indices = new int[tagReportsMap.keySet().size()];
                String[] tagNames = new String[tagReportsMap.keySet().size()];
                int counter = 0;
                boolean found = false;
                // look for a tag on this line
                for ( Iterator iter = tagReportsMap.keySet().iterator(); iter.hasNext(); )
                {
                    tagName = (String) iter.next();
                    index = currentLine.indexOf( tagName );
                    tagNames[counter] = tagName;
                    indices[counter] = index;
                    if ( index >= 0 )
                    {
                        found = true;
                    }

                    counter++;
                }

                if ( !found || tagName == null )
                {
                    // no tag on this line: just go on next line
                    currentLine = reader.readLine();
                    continue;
                }

                // there's a tag on this line
                String commentType = null;
                for ( int i = 0; i < indices.length; i++ )
                {
                    if ( indices[i] >= 0 )
                    {
                        commentType = extractCommentType( currentLine, indices[i] );
                    }
                    if ( commentType != null )
                    {
                        index = indices[i];
                        tagName = tagNames[i];
                        break;
                    }
                }

                if ( commentType == null )
                {
                    // this is not a valid comment tag: go to the next line
                    currentLine = reader.readLine();
                    continue;
                }

                int tagLength = tagName.length();
                int commentStartIndex = reader.getLineNumber();
                StringBuffer comment = new StringBuffer();

                String firstLine = currentLine.substring( index + tagLength ).trim();
                if ( firstLine.length() == 0 )
                {
                    // this is not a valid comment tag: nothing is written there
                    currentLine = reader.readLine();
                    if ( emptyCommentsOn )
                    {
                        comment.append( "--" );
                        comment.append( noCommentString );
                        comment.append( "--" );
                    }
                    else
                    {
                        continue;
                    }
                }
                else
                {
                    // this tag has a comment
                    if ( firstLine.charAt( 0 ) == ':' )
                    {
                        comment.append( firstLine.substring( 1 ).trim() );
                    }
                    else
                    {
                        comment.append( firstLine );
                    }

                    // next line
                    currentLine = reader.readLine();

                    if ( multipleLineCommentsOn )
                    {
                        // we're looking for multiple line comments
                        while ( currentLine != null && currentLine.trim().startsWith( commentType )
                            && currentLine.indexOf( tagName ) < 0 )
                        {
                            String currentComment = currentLine.substring( currentLine.indexOf( commentType )
                                                                           + commentType.length() ).trim();
                            if ( currentComment.startsWith( "@" ) || "".equals( currentComment )
                                || "/".equals( currentComment ) )
                            {
                                // the comment is finished
                                break;
                            }
                            // try to look if the next line is not a new tag
                            boolean newTagFound = false;
                            for ( Iterator iter = tagReportsMap.keySet().iterator(); iter.hasNext(); )
                            {
                                String currentTagName = (String) iter.next();
                                if ( currentComment.startsWith( currentTagName ) )
                                {
                                    newTagFound = true;
                                }
                            }
                            if ( newTagFound )
                            {
                                // this is a new comment: stop here the current comment
                                break;
                            }
                            // nothing was found: this means the comment is going on this line
                            comment.append( " " );
                            comment.append( currentComment );
                            currentLine = reader.readLine();
                        }
                    }
                }

                TagReport tagReport = (TagReport) tagReportsMap.get( tagName );
                FileReport fileReport = tagReport.getFileReport( file );
                fileReport.addComment( comment.toString(), commentStartIndex );
            }
        }
        catch ( IOException e )
        {
            log.error( "Error while scanning the file " + file.getPath(), e );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    /**
     * Finds the type of comment the tag is in.
     * 
     * @param currentLine the line to analyse.
     * @param index the index of the tag in the line.
     * @return "*" or "//" or null.
     */
    private String extractCommentType( String currentLine, int index )
    {
        String commentType = null;
        String beforeTag = currentLine.substring( 0, index ).trim();
        if ( beforeTag.endsWith( SLASH_COMMENT ) )
        {
            commentType = SLASH_COMMENT;
        }
        else if ( beforeTag.endsWith( STAR_COMMENT ) )
        {
            commentType = STAR_COMMENT;
        }
        return commentType;
    }

}
