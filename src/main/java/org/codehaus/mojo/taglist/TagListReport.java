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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Reporting;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.mojo.taglist.beans.FileReport;
import org.codehaus.mojo.taglist.beans.TagReport;
import org.codehaus.mojo.taglist.options.Tag;
import org.codehaus.mojo.taglist.output.TagListXMLComment;
import org.codehaus.mojo.taglist.output.TagListXMLFile;
import org.codehaus.mojo.taglist.output.TagListXMLReport;
import org.codehaus.mojo.taglist.output.TagListXMLTag;
import org.codehaus.mojo.taglist.output.io.xpp3.TaglistOutputXpp3Writer;
import org.codehaus.mojo.taglist.tags.AbsTag;
import org.codehaus.mojo.taglist.tags.InvalidTagException;
import org.codehaus.mojo.taglist.tags.TagClass;
import org.codehaus.mojo.taglist.tags.TagFactory;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.PathTool;
import org.codehaus.plexus.util.StringUtils;

/**
 * Scans the source files for tags and generates a report on their occurrences.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard</a>
 */
@Mojo(name = "taglist", requiresDependencyResolution = ResolutionScope.COMPILE)
public class TagListReport extends AbstractMavenReport {
    /**
     * Specifies the Locale of the source files. Syntax is like "en", "en_US" or "en_US_win".
     *
     * @since 2.4
     */
    // TODO rename to sourceFilesLocale
    @Parameter(property = "sourceFileLocale", defaultValue = "en")
    private String sourceFileLocale;

    /**
     * List of files to include. Specified as fileset patterns which are relative to the source directory.
     *
     * @since 3.0.0
     */
    @Parameter(defaultValue = "**/*.java")
    private String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
     *
     * @since 3.0.0
     */
    @Parameter()
    private String[] excludes;

    /**
     * Specifies the directory where the xml {@code taglist.xml} output will be generated.
     * <br>
     * The xml report has a <a href="taglistOutput.html">format</a>.
     *
     * @since 2.3
     */
    @Parameter(defaultValue = "${project.build.directory}/taglist", required = true)
    private File xmlOutputDirectory;

    /**
     * This parameter indicates whether for simple tags (like "TODO"), the analyzer should look for multiple line
     * comments.
     */
    @Parameter(defaultValue = "true")
    private boolean multipleLineComments;

    /**
     * This parameter indicates whether to look for tags even if they don't have a comment.
     */
    @Parameter(defaultValue = "true")
    private boolean emptyComments;

    /**
     * Link the tag line numbers to the source xref. Defaults to true and will link automatically if jxr plugin is being
     * used.
     */
    @Parameter(defaultValue = "true", property = "taglists.linkXRef")
    private boolean linkXRef;

    /**
     * Location where Source XRef is generated for this project.
     * <br>
     * <strong>Default</strong>: {@link #getReportOutputDirectory()} + {@code /xref}
     */
    @Parameter
    private File xrefLocation;

    /**
     * Location where Test Source XRef is generated for this project.
     * <br>
     * <strong>Default</strong>: {@link #getReportOutputDirectory()} + {@code /xref-test}
     */
    @Parameter
    private File testXrefLocation;

    /**
     * Whether to build an aggregated report at the root, or build individual reports.
     */
    @Parameter(defaultValue = "false", property = "taglists.aggregate")
    private boolean aggregate;

    /**
     * This parameter indicates whether to generate details for tags with zero occurrences.
     *
     * @since 2.2
     */
    @Parameter(defaultValue = "false")
    private boolean showEmptyDetails;

    /**
     * Skips reporting of test sources.
     *
     * @since 2.4
     */
    @Parameter(defaultValue = "false")
    private boolean skipTestSources;

    /**
     * Defines each tag class (grouping) and the individual tags within each class. The user can also specify a title
     * for each tag class and the matching logic used by each tag.
     * <ul>
     * <li><b>Exact Match</b> <br/>
     * &lt;matchString&gt;todo&lt;/matchString&gt;<br/>
     * &lt;matchType&gt;exact&lt;/matchType&gt; <br/>
     * <i>Matches: todo </i></li>
     * <li><b>Ignore Case Match</b> <br/>
     * &lt;matchString&gt;todo&lt;/matchString&gt;<br/>
     * &lt;matchType&gt;ignoreCase&lt;/matchType&gt; <br/>
     * <i>Matches: todo, Todo, TODO... </i></li>
     * <li><b>Regular Expression Match</b> <br/>
     * &lt;matchString&gt;tod[aeo]&lt;/matchString&gt;<br/>
     * &lt;matchType&gt;regEx&lt;/matchType&gt; <br/>
     * <i>Matches: toda, tode, todo </i></li>
     * </ul>
     * <br/>
     * <br/>
     * For complete examples see the <a href="usage.html"><b>Usage</b></a> page. <br/>
     * Type description <a href="taglistOptions.html"><b>taglistOptions</b></a>
     *
     * @since 2.4
     */
    @Parameter
    private org.codehaus.mojo.taglist.options.TagListOptions tagListOptions;

    /**
     * Skip generating report if no tags found in sources.
     *
     * @since 3.1.0
     */
    @Parameter(property = "taglist.skipEmptyReport", defaultValue = "false")
    private boolean skipEmptyReport;

    private final AtomicReference<List<String>> sourceDirs = new AtomicReference<>();

    private Collection<TagReport> tagReportsResult;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    @Override
    protected void executeReport(Locale locale) throws MavenReportException {

        if (StringUtils.isEmpty(getInputEncoding())) {
            getLog().warn("File encoding has not been set, using platform encoding "
                    + Charset.defaultCharset().displayName() + ", i.e. build is platform dependent!");
        }

        executeAnalysis();

        // Renders the report
        TaglistReportRenderer renderer = new TaglistReportRenderer(this, tagReportsResult);
        renderer.setXrefLocation(constructXrefLocation(false));
        renderer.setTestXrefLocation(constructXrefLocation(true));
        renderer.setBundle(getBundle(locale));
        renderer.render();

        // Generate the XML report
        generateXmlReport(tagReportsResult);
    }

    protected String constructXrefLocation(boolean test) {
        String location = null;
        if (linkXRef) {
            File xrefLocation = getXrefLocation(test);

            String relativePath = PathTool.getRelativePath(
                    getReportOutputDirectory().getAbsolutePath(), xrefLocation.getAbsolutePath());
            if (relativePath == null || relativePath.isEmpty()) {
                relativePath = ".";
            }
            relativePath = relativePath + "/" + xrefLocation.getName();
            if (xrefLocation.exists()) {
                // XRef was already generated by manual execution of a lifecycle binding
                location = relativePath;
            } else {
                // Not yet generated - check if the report is on its way
                Reporting reporting = project.getModel().getReporting();
                List<ReportPlugin> reportPlugins = reporting != null ? reporting.getPlugins() : Collections.emptyList();
                for (ReportPlugin plugin : reportPlugins) {
                    String artifactId = plugin.getArtifactId();
                    if ("maven-jxr-plugin".equals(artifactId)) {
                        location = relativePath;
                    }
                }
            }

            if (location == null) {
                getLog().warn("Unable to locate" + (test ? " Test" : "") + " Source XRef to link to - DISABLED");
            }
        }
        return location;
    }

    protected File getXrefLocation(boolean test) {
        File location = test ? testXrefLocation : xrefLocation;
        return location != null ? location : new File(getReportOutputDirectory(), test ? "xref-test" : "xref");
    }

    private void executeAnalysis() throws MavenReportException {
        if (tagReportsResult != null) {
            // already analyzed
            return;
        }

        // Create the tag classes
        List<TagClass> tagClasses = new ArrayList<>();

        // If the new style of tag options were used, add them
        if (tagListOptions != null && !tagListOptions.getTagClasses().isEmpty()) {
            // Scan each tag class
            for (org.codehaus.mojo.taglist.options.TagClass tcOption : tagListOptions.getTagClasses()) {
                // Store the tag class display name.
                TagClass tc = new TagClass(tcOption.getDisplayName());

                // Scan each tag within this tag class.
                for (Tag tagOption : tcOption.getTags()) {
                    // If a match type is not specified use default.
                    String matchType = tagOption.getMatchType();
                    if (matchType == null || matchType.isEmpty()) {
                        matchType = TagFactory.getDefaultTagType();
                    }

                    try {
                        // Create the tag based on the match type, and add it to the tag class
                        AbsTag newTag = TagFactory.createTag(matchType, tagOption.getMatchString());
                        tc.addTag(newTag);
                    } catch (InvalidTagException e) {
                        // This should be impossible since exact is supported.
                        getLog().error("Invalid tag type used.  tag type: " + matchType);
                    }
                }

                // Add this new tag class to the container.
                tagClasses.add(tc);
            }
        }

        // default tags
        if (tagClasses.isEmpty()) {
            tagClasses.add(createTagClass("@todo"));
            tagClasses.add(createTagClass("TODO"));
            tagClasses.add(createTagClass("FIXME"));
        }

        // let's proceed to the analysis
        FileAnalyser fileAnalyser = new FileAnalyser(this, tagClasses);
        try {
            tagReportsResult = fileAnalyser.execute();
        } catch (IOException e) {
            throw new MavenReportException(e.getMessage(), e);
        }
    }

    private TagClass createTagClass(String tag) {
        TagClass tc = new TagClass(tag);
        try {
            AbsTag newTag = TagFactory.createTag("exact", tag);
            tc.addTag(newTag);
        } catch (InvalidTagException e) {
            // This should be impossible since exact is supported.
        }
        return tc;
    }

    /**
     * Generate an XML report that can be used by other plugins like the dashboard plugin.
     *
     * @param tagReports a collection of the tag reports to be output.
     */
    private void generateXmlReport(Collection<TagReport> tagReports) {
        TagListXMLReport report = new TagListXMLReport();
        report.setModelEncoding(getInputEncoding());

        // Iterate through each tag and populate an XML tag object.
        for (TagReport tagReport : tagReports) {
            TagListXMLTag tag = new TagListXMLTag();
            tag.setName(tagReport.getTagName());
            tag.setCount(Integer.toString(tagReport.getTagCount()));

            // Iterate though each file that contains the current tag and generate an
            // XML file object within the current XML tag object.
            for (FileReport fileReport : tagReport.getFileReports()) {
                TagListXMLFile file = new TagListXMLFile();
                file.setName(fileReport.getClassName());
                file.setCount(Integer.toString(fileReport.getLineIndexes().size()));

                // Iterate though each comment that contains the tag and generate an
                // XML comment object within the current xml file object.
                for (Integer lineNumber : fileReport.getLineIndexes()) {
                    TagListXMLComment comment = new TagListXMLComment();
                    comment.setLineNumber(Integer.toString(lineNumber));
                    comment.setComment(fileReport.getComment(lineNumber));

                    file.addComment(comment);
                }
                tag.addFile(file);
            }
            report.addTag(tag);
        }

        // Create the writer for the XML output file.
        xmlOutputDirectory.mkdirs();
        File xmlFile = new File(xmlOutputDirectory, "taglist.xml");

        try (FileOutputStream fos = new FileOutputStream(xmlFile);
                OutputStreamWriter output = new OutputStreamWriter(fos, getInputEncoding())) {

            // Write out the XML output file.
            TaglistOutputXpp3Writer xmlWriter = new TaglistOutputXpp3Writer();
            xmlWriter.write(output, report);
        } catch (Exception e) {
            getLog().warn("Could not save taglist xml file: " + e.getMessage());
        }
    }

    /**
     * Returns the path relative to the output directory.
     *
     * @param location the location to make relative.
     * @return the relative path.
     */
    private String getRelativePath(File location) {
        String relativePath =
                PathTool.getRelativePath(getReportOutputDirectory().getAbsolutePath(), location.getAbsolutePath());
        if (StringUtils.isEmpty(relativePath)) {
            relativePath = ".";
        }
        relativePath = relativePath + "/" + location.getName();
        return relativePath;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.reporting.MavenReport#canGenerateReport()
     */
    @Override
    public boolean canGenerateReport() throws MavenReportException {
        boolean canGenerate = !getSourceDirs().isEmpty();
        if (aggregate && !getProject().isExecutionRoot()) {
            canGenerate = false;
        }

        if (canGenerate) {
            executeAnalysis();
            return !skipEmptyReport || tagReportsResult.stream().anyMatch(tagReport -> tagReport.getTagCount() > 0);
        }

        return false;
    }

    /**
     * Removes empty dirs from the list.
     *
     * @param sourceDirectories the original list of directories.
     * @return a new list containing only non empty dirs.
     */
    private List<String> pruneSourceDirs(List<String> sourceDirectories) throws IOException {
        List<String> pruned = new ArrayList<>(sourceDirectories.size());
        for (String dir : sourceDirectories) {
            if (!pruned.contains(dir) && hasSources(new File(dir))) {
                pruned.add(dir);
            }
        }
        return pruned;
    }

    /**
     * Checks whether the given directory contains source files.
     *
     * @param dir the source directory.
     * @return true if the folder or one of its subfolders contains at least 1 source file that matches
     *         includes/excludes.
     */
    private boolean hasSources(File dir) throws IOException {
        if (dir.exists() && dir.isDirectory()) {
            if (!FileUtils.getFiles(dir, getIncludesCommaSeparated(), getExcludesCommaSeparated())
                    .isEmpty()) {
                return true;
            }

            File[] files = dir.listFiles();
            if (files != null) {
                for (File currentFile : files) {
                    if (currentFile.isDirectory()) {
                        boolean hasSources = hasSources(currentFile);
                        if (hasSources) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Construct the list of source directories to analyze.
     *
     * @return the list of dirs.
     */
    private List<String> constructSourceDirs() {
        List<String> dirs = new ArrayList<>(getProject().getCompileSourceRoots());
        if (!skipTestSources) {
            dirs.addAll(getProject().getTestCompileSourceRoots());
        }

        if (aggregate) {
            for (MavenProject reactorProject : reactorProjects) {
                if ("java"
                        .equals(reactorProject
                                .getArtifact()
                                .getArtifactHandler()
                                .getLanguage())) {
                    dirs.addAll(reactorProject.getCompileSourceRoots());
                    if (!skipTestSources) {
                        dirs.addAll(reactorProject.getTestCompileSourceRoots());
                    }
                }
            }
        }

        /*
         * This try-catch is needed due to a missing declared exception in the
         * 'canGenerateReport()' method. For this reason, neither the 'canGenerateReport()'
         * nor the 'constructSourceDirs()' can throw exceptions.
         * The exception itself is caused by a declaration from the FileUtils, but never used
         * there. The FileUtils.getFiles() should be replaced by an NIO filter at some point.
         */
        try {
            dirs = pruneSourceDirs(dirs);
        } catch (IOException javaIoIOException) {
            getLog().warn("Unable to prune source dirs.", javaIoIOException);
        }

        return dirs;
    }

    protected List<String> getSourceDirs() {
        if (sourceDirs.get() == null) {
            sourceDirs.compareAndSet(null, constructSourceDirs());
        }

        return sourceDirs.get();
    }

    /**
     * Get the files to include, as a comma separated list of patterns.
     */
    String getIncludesCommaSeparated() {
        if (includes != null) {
            return String.join(",", includes);
        } else {
            return "";
        }
    }

    /**
     * Get the files to exclude, as a comma separated list of patterns.
     */
    String getExcludesCommaSeparated() {
        if (excludes != null) {
            return String.join(",", excludes);
        } else {
            return "";
        }
    }

    void setSourceFileLocale(String sourceFileLocale) {
        this.sourceFileLocale = sourceFileLocale;
    }

    /**
     * Returns the Locale of the source files.
     *
     * @return The Locale of the source files.
     */
    public Locale getSourceFileLocale() {
        String[] items = sourceFileLocale.split("_");
        if (sourceFileLocale.isEmpty() || items.length > 3) {
            getLog().warn("Invalid java.util.Locale format '" + sourceFileLocale
                    + "' for 'sourceFileLocale' using 'ENGLISH' locale");
            return Locale.ENGLISH;
        }

        String language = "";
        String country = "";
        String variant = "";

        if (items.length > 0) {
            language = items[0];
        }

        if (items.length > 1) {
            country = items[1];
        }

        if (items.length > 2) {
            variant = items[2];
        }
        return new Locale(language, country, variant);
    }

    /**
     * Tells whether to look for comments over multiple lines.
     *
     * @return Returns true if the analyzer should look for multiple lines.
     */
    public boolean isMultipleLineComments() {
        return multipleLineComments;
    }

    /**
     * Tells whether to look for tags without comments.
     *
     * @return the emptyComments.
     */
    public boolean isEmptyComments() {
        return emptyComments;
    }

    /**
     * Tells whether to generate details for tags with zero occurrences.
     *
     * @return the showEmptyTags.
     */
    public boolean isShowEmptyDetails() {
        return showEmptyDetails;
    }

    /**
     * Get the absolute path to the XML output directory.
     *
     * @return string of the absolute path.
     */
    protected String getXMLOutputDirectory() {
        return xmlOutputDirectory.getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    @Override
    public String getDescription(Locale locale) {
        return getBundle(locale).getString("report.taglist.description");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    @Override
    public String getName(Locale locale) {
        return getBundle(locale).getString("report.taglist.name");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    @Override
    public String getOutputName() {
        return "taglist";
    }

    /**
     * Returns the correct resource bundle according to the locale.
     *
     * @param locale the locale of the user.
     * @return the bundle corresponding to the locale.
     */
    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(
                "taglist-report", locale, this.getClass().getClassLoader());
    }

    @Override
    protected String getInputEncoding() {
        return super.getInputEncoding();
    }
}
