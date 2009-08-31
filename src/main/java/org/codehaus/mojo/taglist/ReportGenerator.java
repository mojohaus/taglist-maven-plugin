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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.codehaus.doxia.sink.Sink;
import org.codehaus.mojo.taglist.beans.FileReport;
import org.codehaus.mojo.taglist.beans.TagReport;

/**
 * Generates the taglist report using Doxia.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard </a>
 */

public class ReportGenerator
{
    /**
     * The source code cross reference path.
     */
    private String xrefLocation;

    /**
     * The test code cross reference path.
     */
    private String testXrefLocation;

    /**
     * The sink used in this Maven build to generated the tag list page.
     */
    private Sink sink;

    /**
     * The resource bundle used in this Maven build.
     */
    private ResourceBundle bundle;

    /**
     * The output path of the site.
     */
    private File siteOutputDirectory;

    /**
     * A list of sorted tag reports.
     */
    private List sortedTagReports;

    /**
     * Display details for tags that contain zero occurrences.
     */
    private boolean showEmptyDetails;

    /**
     * Constructor.
     *
     * @param report the TagListReport object used in this build.
     * @param tagReports a collection of tagReports to output.
     */
    public ReportGenerator( TagListReport report, Collection tagReports )
    {
        sortedTagReports = new ArrayList( tagReports );
        Collections.sort( sortedTagReports );
        this.bundle = report.getBundle();
        this.sink = report.getSink();
        this.siteOutputDirectory = report.getReportOutputDirectory();
        this.showEmptyDetails = report.isShowEmptyDetails();
    }

    /**
     * Generates the whole report using each tag reports made during the analysis.
     */
    public void generateReport()
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "report.taglist.header" ) );
        sink.title_();
        sink.head_();

        sink.body();
        sink.section1();

        sink.sectionTitle1();
        sink.text( bundle.getString( "report.taglist.mainTitle" ) );
        sink.sectionTitle1_();

        // Summary section
        doSummarySection( sortedTagReports );

        // Detail section
        doDetailSection( sortedTagReports );

        sink.section1_();
        sink.body_();
        sink.flush();
        sink.close();
    }

    /**
     * @param tagReports a collection of tagReports to summarize.
     */
    private void doSummarySection( Collection tagReports )
    {
        sink.paragraph();
        sink.text( bundle.getString( "report.taglist.summary.description" ) );
        sink.paragraph_();

        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.taglist.summary.tag" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.taglist.summary.occurrences" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.taglist.summary.tagstrings" ) );
        sink.tableHeaderCell_();
        sink.tableRow_();
        for ( Iterator iter = tagReports.iterator(); iter.hasNext(); )
        {
            doTagSummary( (TagReport) iter.next() );
        }
        sink.table_();
    }

    /**
     * @param tagReport the tagReport to summarize.
     */
    private void doTagSummary( TagReport tagReport )
    {
        sink.tableRow();
        sink.tableCell();
        // Create a hyperlink if the "showEmptyTags" flag is set or the tag contains 1 or more occurrences.
        if ( showEmptyDetails || tagReport.getTagCount() > 0 )
        {
            sink.link( "#" + tagReport.getHTMLSafeLinkName() );
            sink.text( tagReport.getTagName() );
            sink.link_();
        }
        else
        {
            sink.text( tagReport.getTagName() );
        }
        sink.tableCell_();
        sink.tableCell();
        sink.text( String.valueOf( tagReport.getTagCount() ) );
        sink.tableCell_();
        sink.tableCell();
        String [] tags = tagReport.getTagStrings();
        if ( tags != null )
        {
            // Output each tag string
            for ( int i = 0; i < tags.length; ++i )
            {
                if ( i > 0 )
                {
                    // Insert comma before each tag except for the first one.
                    sink.text( ", " );
                }
                sink.text( tags[i] );
            }
        }
        sink.tableCell_();
        sink.tableRow_();
    }

    /**
     * @param tagReports a collection of tagReports to be detailed in this section.
     */
    private void doDetailSection( Collection tagReports )
    {
        sink.paragraph();
        sink.text( bundle.getString( "report.taglist.detail.description" ) );
        sink.paragraph_();

        for ( Iterator iter = tagReports.iterator(); iter.hasNext(); )
        {
            doTagDetailedPart( (TagReport) iter.next() );
        }
    }

    /**
     * @param tagReport to tagReport to detail.
     */
    private void doTagDetailedPart( TagReport tagReport )
    {
        // Create detailed section only if the "showEmptyTags" flag is set or the tag contains 1 or more occurrences.
        if ( !showEmptyDetails && tagReport.getTagCount() <= 0 )
        {
            return;
        }

        sink.section2();
        sink.sectionTitle2();
        sink.anchor( tagReport.getHTMLSafeLinkName() );
        sink.text( tagReport.getTagName() );
        sink.anchor_();
        sink.sectionTitle2_();
        sink.paragraph();
        sink.bold();
        sink.text( bundle.getString( "report.taglist.detail.numberOfOccurrences" ) + ' ' + tagReport.getTagCount() );
        sink.bold_();
        sink.paragraph_();

        Collection fileReports = tagReport.getFileReports();
        List sortedFileReports = new ArrayList( fileReports );
        Collections.sort( sortedFileReports );

        // MTAGLIST-38 - sink table before generating each file report in order
        //               to align the columns correctly.
        sink.table();

        for ( Iterator iter = sortedFileReports.iterator(); iter.hasNext(); )
        {
            doFileDetailedPart( (FileReport) iter.next() );
        }
        sink.table_();

        sink.section2_();
    }

    /**
     * @param fileReport the FileReport to output for this detailed tag report.
     */
    private void doFileDetailedPart( FileReport fileReport )
    {
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( fileReport.getClassName() );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.taglist.detail.line" ) );
        sink.tableHeaderCell_();
        sink.tableRow_();
        for ( Iterator iter = fileReport.getLineIndexes().iterator(); iter.hasNext(); )
        {
            doCommentLine( fileReport, (Integer) iter.next() );
        }
    }

    /**
     * @param fileReport the FileReport for the current tag's comment.
     * @param lineNumber the line number of the current tag's comment.
     */
    private void doCommentLine( FileReport fileReport, Integer lineNumber )
    {
        boolean linked = false;
        
        sink.tableRow();
        sink.tableCell();
        sink.text( fileReport.getComment( lineNumber ) );
        sink.tableCell_();
        sink.tableCell();
        if ( xrefLocation != null )
        {
            String fileLink = xrefLocation + "/" + fileReport.getClassNameWithSlash() + ".html";
            File xrefFile = new File( siteOutputDirectory, fileLink.substring( 2 ) );

            // Link only if file exists in xref
            if ( xrefFile.exists() )
            {
                sink.link( fileLink + "#" + lineNumber );
                linked = true;
            }
        }
        // If the file was not linked to xref and there is a test xref location check it
        if ( !linked && testXrefLocation != null )
        {
            String testFileLink = testXrefLocation + "/" + fileReport.getClassNameWithSlash() + ".html";
            File testXrefFile = new File( siteOutputDirectory, testFileLink.substring( 2 ) );
            
            // Link only if file exists in test xref
            if ( testXrefFile.exists() )
            {
                sink.link( testFileLink + "#" + lineNumber );
                linked = true;
            }
        }

        sink.text( String.valueOf( lineNumber ) );
        
        // Was a xref or test-xref link created?
        if ( linked )
        {
            sink.link_();
        }
        sink.tableCell_();
        sink.tableRow_();
    }

    /**
     * Set the source code cross reference location.
     *
     * @param xrefLocation the location of the source code cross reference.
     */
    public void setXrefLocation( String xrefLocation )
    {
        this.xrefLocation = xrefLocation;
    }

    /**
     * Get the source code cross reference location.
     *
     * @return the source code cross reference location.
     */
    public String getXrefLocation()
    {
        return xrefLocation;
    }

    /**
     * Get the test code cross reference location.
     *
     * @return the test code cross reference location.
     */
    public String getTestXrefLocation()
    {
        return testXrefLocation;
    }

    /**
     * Set the test code cross reference location.
     *
     * @param testXrefLocation the location of the test code cross reference.
     */
    public void setTestXrefLocation( String testXrefLocation )
    {
        this.testXrefLocation = testXrefLocation;
    }
}
