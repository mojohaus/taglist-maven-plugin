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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.mojo.taglist.beans.FileReport;
import org.codehaus.mojo.taglist.beans.TagReport;
import org.codehaus.mojo.taglist.tags.TagClass;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Class that analyzes a file with a special comment tag. For instance:
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
     * Maximum length of a comment.
     */
    private static final int MAX_COMMENT_CHARACTERS = 99999;

    /**
     * The character encoding of the files to analyze.
     */
    private String encoding;
    
    /**
     * The Locale of the files to analyze.
     */
    private Locale locale;

    /**
     * The directories to analyze.
     */
    private Collection sourceDirs;

    /**
     * Log for debug output.
     */
    private Log log;

    /**
     * Set to true if the analyzer should look for multiple line comments.
     */
    private boolean multipleLineCommentsOn;

    /**
     * Set to true if the analyzer should look for tags without comments.
     */
    private boolean emptyCommentsOn;

    /**
     * String used to indicate that there is no comment after the tag.
     */
    private String noCommentString;

    /**
     * ArrayList of tag classes.
     */
    private ArrayList tagClasses = new ArrayList();

    /**
     * Constructor.
     * 
     * @param report the MOJO that is using this analyzer.
     * @param tagClasses the array of tag classes to use for searching
     */
    public FileAnalyser( TagListReport report, ArrayList tagClasses )
    {
        multipleLineCommentsOn = report.isMultipleLineComments();
        emptyCommentsOn = report.isEmptyComments();
        log = report.getLog();
        sourceDirs = report.constructSourceDirs();
        encoding = report.getEncoding();
        locale = report.getLocale();
        noCommentString = report.getBundle().getString( "report.taglist.nocomment" );      
        this.tagClasses = tagClasses;
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

        // Get the tag reports from each of the tag classes.
        Collection tagReports = new ArrayList();
        Iterator itr = tagClasses.iterator();      
        while ( itr.hasNext() )
        {
            TagClass tc = (TagClass) itr.next();
            tagReports.add( tc.getTagReport() );
        }

        return tagReports;
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
     * Access an input reader that uses the current file encoding.
     *
     * @param file the file to open in the reader.
     * @throws IOException the IO exception.
     * @return a reader with the current file encoding.
     */
    private Reader getReader( File file ) throws IOException
    {
        InputStream in = new FileInputStream( file );
        return ( encoding == null ) ? new InputStreamReader( in ) : new InputStreamReader( in, encoding ); 
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
            reader = new LineNumberReader( getReader( file ) );

            String currentLine = reader.readLine();
            while ( currentLine != null )
            {
                int index = -1;
                Iterator iter = tagClasses.iterator();
                // look for a tag on this line
                while ( iter.hasNext() )
                {
                    TagClass tagClass = (TagClass) iter.next();
                    index = tagClass.tagMatchContains( currentLine, locale );
                    if ( index != TagClass.NO_MATCH )
                    {
                        // there's a tag on this line
                        String commentType = null;
                        commentType = extractCommentType( currentLine, index );

                        if ( commentType == null )
                        {
                            // this is not a valid comment tag: skip other tag classes and
                            // go to the next line
                            break;
                        }

                        int tagLength = tagClass.getLastTagMatchStringLength();
                        int commentStartIndex = reader.getLineNumber();
                        StringBuffer comment = new StringBuffer();

                        String firstLine = StringUtils.strip( currentLine.substring( index + tagLength ) );
                        firstLine = StringUtils.removeEnd( firstLine, "*/" ); //MTAGLIST-35
                        if ( firstLine.length() == 0 || ":".equals( firstLine ) )
                        {
                            // this is not a valid comment tag: nothing is written there
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

                            if ( multipleLineCommentsOn )
                            {
                                // Mark the current position, set the read forward limit to
                                // a large number that should not be met.
                                reader.mark( MAX_COMMENT_CHARACTERS );
                                
                                // next line
                                String futureLine = reader.readLine();
                                
                                // we're looking for multiple line comments
                                while ( futureLine != null && futureLine.trim().startsWith( commentType )
                                    && futureLine.indexOf( tagClass.getLastTagMatchString() ) < 0 )
                                {
                                    String currentComment = futureLine.substring( futureLine.indexOf( commentType )
                                                                                   + commentType.length() ).trim();
                                    if ( currentComment.startsWith( "@" ) || "".equals( currentComment )
                                        || "/".equals( currentComment ) )
                                    {
                                        // the comment is finished
                                        break;
                                    }
                                    // try to look if the next line is not a new tag
                                    boolean newTagFound = false;
                                    Iterator moreTCiter = tagClasses.iterator();
                                    while ( moreTCiter.hasNext() )
                                    {
                                        TagClass tc = (TagClass) moreTCiter.next();
                                        if ( tc.tagMatchStartsWith( currentComment, locale ) )
                                        {
                                            newTagFound = true;
                                            break;
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
                                    futureLine = reader.readLine();
                                }
                                
                                // Reset the reader to the marked position before the multi
                                // line check was performed.
                                reader.reset();
                            }
                        }
                        TagReport tagReport = tagClass.getTagReport();
                        FileReport fileReport = tagReport.getFileReport( file, encoding );
                        fileReport.addComment( comment.toString(), commentStartIndex );
                    }
                }
                currentLine = reader.readLine();
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
     * @param currentLine the line to analyze.
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
