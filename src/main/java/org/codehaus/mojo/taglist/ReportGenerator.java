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
    private String xrefLocation;

    private String testXrefLocation;

    private Sink sink;

    private ResourceBundle bundle;

    private String siteOutputDirectory;

    private List sortedTagReports;

    public ReportGenerator( TagListReport report, Collection tagReports )
    {
        sortedTagReports = new ArrayList( tagReports );
        Collections.sort( sortedTagReports );
        this.bundle = report.getBundle();
        this.sink = report.getSink();
        // TODO Do not hardcode this, retrieve it from the site plugin config
        this.siteOutputDirectory = report.getProject().getBuild().getDirectory() + File.separator + "site";
    }

    /**
     * Generates the whole report using each tag reports made during the
     * analysis.
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
        doSummarySection( sortedTagReports, bundle, sink );

        // Detail section
        doDetailSection( sortedTagReports, bundle, sink );

        sink.section1_();
        sink.body_();
        sink.flush();
        sink.close();
    }

    /**
     * @param tagReports
     * @param sink
     */
    private static void doSummarySection( Collection tagReports, ResourceBundle bundle, Sink sink )
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
        sink.tableRow_();
        for ( Iterator iter = tagReports.iterator(); iter.hasNext(); )
        {
            doTagSummary( sink, (TagReport) iter.next() );
        }
        sink.table_();
    }

    /**
     * @param sink
     * @param tagReport
     */
    private static void doTagSummary( Sink sink, TagReport tagReport )
    {
        sink.tableRow();
        sink.tableCell();
        sink.link( "#" + tagReport.getTagName() );
        sink.text( tagReport.getTagName() );
        sink.link_();
        sink.tableCell_();
        sink.tableCell();
        sink.text( String.valueOf( tagReport.getTagCount() ) );
        sink.tableCell_();
        sink.tableRow_();
    }

    /**
     * @param tagReports
     * @param sink
     */
    private void doDetailSection( Collection tagReports, ResourceBundle bundle, Sink sink )
    {
        sink.paragraph();
        sink.text( bundle.getString( "report.taglist.detail.description" ) );
        sink.paragraph_();

        for ( Iterator iter = tagReports.iterator(); iter.hasNext(); )
        {
            doTagDetailedPart( sink, (TagReport) iter.next(), bundle );
        }
    }

    /**
     * @param sink
     * @param tagReport
     */
    private void doTagDetailedPart( Sink sink, TagReport tagReport, ResourceBundle bundle )
    {
        sink.section2();
        sink.sectionTitle2();
        sink.anchor( tagReport.getTagName() );
        sink.text( tagReport.getTagName() );
        sink.anchor_();
        sink.sectionTitle2_();
        sink.paragraph();
        sink.bold();
        sink.text( bundle.getString( "report.taglist.detail.numberOfOccurrences" ) + tagReport.getTagCount() );
        sink.bold_();
        sink.paragraph_();

        Collection fileReports = tagReport.getFileReports();
        List sortedFileReports = new ArrayList( fileReports );
        Collections.sort( sortedFileReports );

        for ( Iterator iter = sortedFileReports.iterator(); iter.hasNext(); )
        {
            doFileDetailedPart( sink, (FileReport) iter.next(), bundle );
        }

        sink.section2_();
    }

    /**
     * @param sink
     * @param fileReport
     */
    private void doFileDetailedPart( Sink sink, FileReport fileReport, ResourceBundle bundle )
    {
        sink.table();
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
            doCommentLine( sink, fileReport, (Integer) iter.next() );
        }
        sink.table_();
    }

    /**
     * @param sink
     * @param fileReport
     * @param lineNumber
     */
    private void doCommentLine( Sink sink, FileReport fileReport, Integer lineNumber )
    {
        sink.tableRow();
        sink.tableCell();
        sink.text( fileReport.getComment( lineNumber ) );
        sink.tableCell_();
        sink.tableCell();
        if ( xrefLocation != null )
        {
            String fileLink = xrefLocation + "/" + fileReport.getClassNameWithSlash() + ".html";
            File xrefFile = new File( siteOutputDirectory, fileLink.substring( 2 ) );
            if ( xrefFile.exists() )
            {
                sink.link( fileLink + "#" + lineNumber );
            }
            else
            {
                // this is test-xref
                sink.link( fileLink.replaceFirst( xrefLocation, testXrefLocation ) + "#" + lineNumber );
            }
        }
        sink.text( String.valueOf( lineNumber ) );
        sink.link_();
        sink.tableCell_();
        sink.tableRow_();
    }

    public void setXrefLocation( String xrefLocation )
    {
        this.xrefLocation = xrefLocation;
    }

    public String getXrefLocation()
    {
        return xrefLocation;
    }

    public String getTestXrefLocation()
    {
        return testXrefLocation;
    }

    public void setTestXrefLocation( String testXrefLocation )
    {
        this.testXrefLocation = testXrefLocation;
    }
}