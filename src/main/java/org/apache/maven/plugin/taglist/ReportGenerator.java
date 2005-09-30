package org.apache.maven.plugin.taglist;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.apache.maven.plugin.taglist.beans.FileReport;
import org.apache.maven.plugin.taglist.beans.TagReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

/**
 * Generates the taglist report using Doxia.
 * 
 * @author <a href="mailto:bellingard@gmail.com">Fabrice Bellingard </a>
 */

public class ReportGenerator
{

    /**
     * Generates the whole report using each tag reports made during the
     * analysis.
     * 
     * @param tagReports
     *            a Collection of TagReport objects
     * @param locale
     *            the user locale
     */
    public static void generateReport( Collection tagReports, ResourceBundle bundle, Sink sink )
        throws MavenReportException
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

        // sort the tag reports by name
        ArrayList sortedTagReports = new ArrayList( tagReports );
        Collections.sort( sortedTagReports );

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
        sink.text( bundle.getString( "report.taglist.summary.occurences" ) );
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
        sink.text( tagReport.getTagCount() + "" );
        sink.tableCell_();
        sink.tableRow_();
    }

    /**
     * @param tagReports
     * @param sink
     */
    private static void doDetailSection( Collection tagReports, ResourceBundle bundle, Sink sink )
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
    private static void doTagDetailedPart( Sink sink, TagReport tagReport, ResourceBundle bundle )
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
        ArrayList sortedFileReports = new ArrayList( fileReports );
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
    private static void doFileDetailedPart( Sink sink, FileReport fileReport, ResourceBundle bundle )
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
    private static void doCommentLine( Sink sink, FileReport fileReport, Integer lineNumber )
    {
        sink.tableRow();
        sink.tableCell();
        sink.text( fileReport.getComment( lineNumber ) );
        sink.tableCell_();
        sink.tableCell();
        sink.link( "xref/" + fileReport.getClassNameWithSlash() + ".html#" + lineNumber );
        sink.text( lineNumber + "" );
        sink.link_();
        sink.tableCell_();
        sink.tableRow_();
    }

}