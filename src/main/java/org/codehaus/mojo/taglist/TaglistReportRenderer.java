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
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.reporting.AbstractMavenReportRenderer;
import org.codehaus.mojo.taglist.beans.FileReport;
import org.codehaus.mojo.taglist.beans.TagReport;

/**
 * Generates the taglist report using Doxia.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard </a>
 */
public class TaglistReportRenderer extends AbstractMavenReportRenderer {
    /**
     * The source code cross reference path.
     */
    private String xrefLocation;

    /**
     * The test code cross reference path.
     */
    private String testXrefLocation;

    /**
     * The resource bundle used in this Maven build.
     */
    private ResourceBundle bundle;

    /**
     * The output path of the site.
     */
    private final File siteOutputDirectory;

    /**
     * A list of sorted tag reports.
     */
    private final SortedSet<TagReport> sortedTagReports;

    /**
     * Display details for tags that contain zero occurrences.
     */
    private final boolean showEmptyDetails;

    /**
     * Constructor.
     *
     * @param report     the TagListReport object used in this build.
     * @param tagReports a collection of tagReports to output.
     */
    public TaglistReportRenderer(TagListReport report, Collection<TagReport> tagReports) {
        super(report.getSink());
        this.sortedTagReports = new TreeSet<>(tagReports);
        this.siteOutputDirectory = report.getReportOutputDirectory();
        this.showEmptyDetails = report.isShowEmptyDetails();
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public String getTitle() {
        return bundle.getString("report.taglist.header");
    }

    @Override
    protected void renderBody() {
        startSection(bundle.getString("report.taglist.mainTitle"));

        // Summary section
        doSummarySection(sortedTagReports);

        // Detail section
        doDetailSection(sortedTagReports);

        endSection();
    }

    /**
     * @param tagReports a collection of tagReports to summarize.
     */
    private void doSummarySection(Collection<TagReport> tagReports) {

        paragraph(bundle.getString("report.taglist.summary.description"));

        startTable();

        tableHeader(new String[] {
            bundle.getString("report.taglist.summary.tag"),
            bundle.getString("report.taglist.summary.occurrences"),
            bundle.getString("report.taglist.summary.tagstrings")
        });

        for (TagReport tagReport : tagReports) {
            doTagSummary(tagReport);
        }

        endTable();
    }

    /**
     * @param tagReport the tagReport to summarize.
     */
    private void doTagSummary(TagReport tagReport) {

        String[] tags = tagReport.getTagStrings();
        String tagsAsString = null;
        if (tags != null) {
            tagsAsString = String.join(", ", tags);
        }

        tableRow(new String[] {
            createLinkPatternedText(
                    tagReport.getTagName(),
                    showEmptyDetails || tagReport.getTagCount() > 0 ? "#" + tagReport.getHTMLSafeLinkName() : null),
            String.valueOf(tagReport.getTagCount()),
            tagsAsString
        });
    }

    /**
     * @param tagReports a collection of tagReports to be detailed in this section.
     */
    private void doDetailSection(Collection<TagReport> tagReports) {
        paragraph(bundle.getString("report.taglist.detail.description"));

        for (TagReport tagReport : tagReports) {
            doTagDetailedPart(tagReport);
        }
    }

    /**
     * @param tagReport to tagReport to detail.
     */
    private void doTagDetailedPart(TagReport tagReport) {
        // Create detailed section only if the "showEmptyTags" flag is set or the tag contains 1 or more occurrences.
        if (!showEmptyDetails && tagReport.getTagCount() <= 0) {
            return;
        }

        startSection(tagReport.getTagName(), tagReport.getHTMLSafeLinkName());

        paragraph(bundle.getString("report.taglist.detail.numberOfOccurrences") + ' ' + tagReport.getTagCount());

        Collection<FileReport> fileReports = tagReport.getFileReports();
        SortedSet<FileReport> sortedFileReports = new TreeSet<>(fileReports);

        // MTAGLIST-38 - sink table before generating each file report in order
        //               to align the columns correctly.
        startTable();

        for (FileReport sortedFileReport : sortedFileReports) {
            doFileDetailedPart(sortedFileReport);
        }

        endTable();

        endSection();
    }

    /**
     * @param fileReport the FileReport to output for this detailed tag report.
     */
    private void doFileDetailedPart(FileReport fileReport) {

        tableHeader(new String[] {fileReport.getClassName(), bundle.getString("report.taglist.detail.line")});

        for (Integer integer : fileReport.getLineIndexes()) {
            doCommentLine(fileReport, integer);
        }
    }

    /**
     * @param fileReport the FileReport for the current tag's comment.
     * @param lineNumber the line number of the current tag's comment.
     */
    private void doCommentLine(FileReport fileReport, Integer lineNumber) {

        String comment = fileReport.getComment(lineNumber);
        if (comment == null || comment.isEmpty()) {
            comment = "--" + bundle.getString("report.taglist.nocomment") + "--";
        }

        String link = null;
        if (xrefLocation != null) {
            String fileLink = xrefLocation + "/" + fileReport.getClassNameWithSlash() + ".html";
            File xrefFile = new File(siteOutputDirectory, fileLink.substring(2));

            // Link only if file exists in xref
            if (xrefFile.exists()) {
                link = fileLink + "#L" + lineNumber;
            }
        }
        // If the file was not linked to xref and there is a test xref location check it
        if (link == null && testXrefLocation != null) {
            String testFileLink = testXrefLocation + "/" + fileReport.getClassNameWithSlash() + ".html";
            File testXrefFile = new File(siteOutputDirectory, testFileLink.substring(2));

            // Link only if file exists in test xref
            if (testXrefFile.exists()) {
                link = testFileLink + "#L" + lineNumber;
            }
        }

        tableRow(new String[] {comment, createLinkPatternedText(String.valueOf(lineNumber), link)});
    }

    /**
     * Set the source code cross reference location.
     *
     * @param xrefLocation the location of the source code cross reference.
     */
    public void setXrefLocation(String xrefLocation) {
        this.xrefLocation = xrefLocation;
    }

    /**
     * Get the source code cross reference location.
     *
     * @return the source code cross reference location.
     */
    public String getXrefLocation() {
        return xrefLocation;
    }

    /**
     * Get the test code cross reference location.
     *
     * @return the test code cross reference location.
     */
    public String getTestXrefLocation() {
        return testXrefLocation;
    }

    /**
     * Set the test code cross reference location.
     *
     * @param testXrefLocation the location of the test code cross reference.
     */
    public void setTestXrefLocation(String testXrefLocation) {
        this.testXrefLocation = testXrefLocation;
    }
}
