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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.mojo.taglist.beans.FileReport;
import org.codehaus.mojo.taglist.beans.TagReport;
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
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.PathTool;
import org.codehaus.plexus.util.StringUtils;

/**
 * Scans the source files for tags and generates a report on their occurrences.
 * 
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard</a>
 */
@Mojo( name="taglist", requiresDependencyResolution = ResolutionScope.COMPILE)
public class TagListReport
    extends AbstractMavenReport
{
    /**
     * Specifies the Locale of the source files.
     * 
     * @since 2.4
     */
    @Parameter( property = "sourceFileLocale", defaultValue = "en" )
    private String sourceFileLocale;

    /** Default locale used if the source file locale is null. */
    private static final String DEFAULT_LOCALE = "en";

    /**
     * List of files to include. Specified as fileset patterns which are relative to the source directory.
     *
     * @since 3.0.0
     */
    @Parameter( defaultValue = "**/*.java" )
    private String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
     *
     * @since 3.0.0
     */
    @Parameter( defaultValue = "" )
    private String[] excludes;

    /**
     * Specifies the directory where the xml output will be generated.
     * 
     * @since 2.3
     */
    @Parameter( defaultValue = "${project.build.directory}/taglist", required = true )
    private File xmlOutputDirectory;

    /**
     * This parameter indicates whether for simple tags (like "TODO"), the analyzer should look for multiple line
     * comments.
     * 
     */
    @Parameter( defaultValue = "true" )
    private boolean multipleLineComments;

    /**
     * This parameter indicates whether to look for tags even if they don't have a comment.
     */
    @Parameter( defaultValue = "true" )
    private boolean emptyComments;

    /**
     * Link the tag line numbers to the source xref. Defaults to true and will link automatically if jxr plugin is being
     * used.
     */
    @Parameter( defaultValue = "true", property = "taglists.linkXRef")
    private boolean linkXRef;

    /**
     * Location of the Xrefs to link to.
     */
    @Parameter( defaultValue = "${project.reporting.outputDirectory}/xref", property = "taglists.xrefLocation" )
    private File xrefLocation;

    /**
     * Location of the Test Xrefs to link to.
     */
    @Parameter( defaultValue = "${project.reporting.outputDirectory}/xref-test", property = "taglists.testXrefLocation" )
    private File testXrefLocation;

    /**
     * The projects in the reactor for aggregation report.
     */
    @Parameter( readonly = true, defaultValue = "${reactorProjects}" )
    private List reactorProjects;

    /**
     * Whether to build an aggregated report at the root, or build individual reports.
     */
    @Parameter( defaultValue = "false", property = "taglists.aggregate" )
    private boolean aggregate;

    /**
     * The locale used for rendering the page.
     */
    private Locale currentLocale;

    /**
     * This parameter indicates whether to generate details for tags with zero occurrences.
     * 
     * @since 2.2
     */
    @Parameter( defaultValue = "false" )
    private boolean showEmptyDetails;

    /**
     * Skips reporting of test sources.
     * 
     * @since 2.4
     */
    @Parameter( defaultValue = "false" )
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
     *
     * @since 2.4
     */
    @Parameter
    private org.codehaus.mojo.taglist.options.TagListOptions tagListOptions;

    private String[] tags;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    protected void executeReport( Locale locale )
            throws MavenReportException
    {
        this.currentLocale = locale;

        // User entered no tags and no tagOptions, then default tags
        if ( ( tagListOptions == null || tagListOptions.getTagClasses().size() == 0 ) )
        {
            tags = new String[] { "@todo", "TODO" };
        }

        if ( StringUtils.isEmpty( getInputEncoding() ) )
        {
            getLog().warn(
                           "File encoding has not been set, using platform encoding "
                               + System.getProperty( "file.encoding" ) + ", i.e. build is platform dependent!" );
        }

        // Create the tag classes
        List<TagClass> tagClasses = new ArrayList<>();

        // If any old style tags were used, then add each tag as a tag class
        if ( tags != null && tags.length > 0 )
        {
            getLog().warn( "Using legacy tag format.  This is not recommended." );
            for ( int i = 0; i < tags.length; i++ )
            {
                TagClass tc = new TagClass( tags[i] );
                try
                {
                    AbsTag newTag = TagFactory.createTag( "exact", tags[i] );
                    tc.addTag( newTag );

                    tagClasses.add( tc );
                }
                catch ( InvalidTagException e )
                {
                    // This should be impossible since exact is supported.
                    getLog().error( "Invalid tag type used.  tag type: exact" );
                }
            }
        }

        // If the new style of tag options were used, add them
        if ( tagListOptions != null && tagListOptions.getTagClasses().size() > 0 )
        {
            // Scan each tag class
            Iterator classIter = tagListOptions.getTagClasses().iterator();
            while ( classIter.hasNext() )
            {
                org.codehaus.mojo.taglist.options.TagClass tcOption =
                    (org.codehaus.mojo.taglist.options.TagClass) classIter.next();

                // Store the tag class display name.
                TagClass tc = new TagClass( tcOption.getDisplayName() );

                // Scan each tag within this tag class.
                Iterator tagIter = tcOption.getTags().iterator();
                while ( tagIter.hasNext() )
                {
                    org.codehaus.mojo.taglist.options.Tag tagOption =
                        (org.codehaus.mojo.taglist.options.Tag) tagIter.next();

                    // If a match type is not specified use default.
                    String matchType = tagOption.getMatchType();
                    if ( matchType == null || matchType.length() == 0 )
                    {
                        matchType = TagFactory.getDefaultTagType();
                    }

                    try
                    {
                        // Create the tag based on the match type, and add it to the tag class
                        AbsTag newTag = TagFactory.createTag( matchType, tagOption.getMatchString() );
                        tc.addTag( newTag );
                    }
                    catch ( InvalidTagException e )
                    {
                        // This should be impossible since exact is supported.
                        getLog().error( "Invalid tag type used.  tag type: " + matchType );
                    }
                }

                // Add this new tag class to the container.
                tagClasses.add( tc );
            }
        }

        // let's proceed to the analysis
        FileAnalyser fileAnalyser = new FileAnalyser( this, tagClasses );
        Collection tagReports = fileAnalyser.execute();

        // Renders the report
        ReportGenerator generator = new ReportGenerator( this, tagReports );
        if ( linkXRef )
        {
            String relativePath = getRelativePath( xrefLocation );
            if ( xrefLocation.exists() )
            {
                // XRef was already generated by manual execution of a lifecycle binding
                generator.setXrefLocation( relativePath );
                generator.setTestXrefLocation( getRelativePath( testXrefLocation ) );
            }
            else
            {
                // Not yet generated - check if the report is on its way
                for ( Iterator reports = getProject().getReportPlugins().iterator(); reports.hasNext(); )
                {
                    ReportPlugin report = (ReportPlugin) reports.next();

                    String artifactId = report.getArtifactId();
                    if ( "maven-jxr-plugin".equals( artifactId ) || "jxr-maven-plugin".equals( artifactId ) )
                    {
                        getLog().error(
                                        "Taglist plugin MUST be executed after the JXR plugin."
                                            + "  No links to xref were generated." );
                    }
                }
            }

            if ( generator.getXrefLocation() == null )
            {
                getLog().warn( "Unable to locate Source XRef to link to - DISABLED" );
            }
        }
        generator.generateReport();

        // Generate the XML report
        generateXmlReport( tagReports );
    }

    /**
     * Generate an XML report that can be used by other plugins like the dashboard plugin.
     * 
     * @param tagReports a collection of the tag reports to be output.
     */
    private void generateXmlReport( Collection tagReports )
    {
        TagListXMLReport report = new TagListXMLReport();
        report.setModelEncoding( getInputEncoding() );

        // Iterate through each tag and populate an XML tag object.
        for ( Iterator ite = tagReports.iterator(); ite.hasNext(); )
        {
            TagReport tagReport = (TagReport) ite.next();

            TagListXMLTag tag = new TagListXMLTag();
            tag.setName( tagReport.getTagName() );
            tag.setCount( Integer.toString( tagReport.getTagCount() ) );

            // Iterate though each file that contains the current tag and generate an
            // XML file object within the current XML tag object.
            for ( Iterator fite = tagReport.getFileReports().iterator(); fite.hasNext(); )
            {
                FileReport fileReport = (FileReport) fite.next();

                TagListXMLFile file = new TagListXMLFile();
                file.setName( fileReport.getClassName() );
                file.setCount( Integer.toString( fileReport.getLineIndexes().size() ) );

                // Iterate though each comment that contains the tag and generate an
                // XML comment object within the current xml file object.
                for ( Iterator cite = fileReport.getLineIndexes().iterator(); cite.hasNext(); )
                {
                    Integer lineNumber = (Integer) cite.next();

                    TagListXMLComment comment = new TagListXMLComment();
                    comment.setLineNumber( Integer.toString( lineNumber.intValue() ) );
                    comment.setComment( fileReport.getComment( lineNumber ) );

                    file.addComment( comment );
                }
                tag.addFile( file );
            }
            report.addTag( tag );
        }

        // Create the writer for the XML output file.
        xmlOutputDirectory.mkdirs();
        File xmlFile = new File( xmlOutputDirectory, "taglist.xml" );
        FileOutputStream fos = null;
        OutputStreamWriter output = null;

        try
        {
            fos = new FileOutputStream( xmlFile );
            output = new OutputStreamWriter( fos, getInputEncoding());

            // Write out the XML output file.
            TaglistOutputXpp3Writer xmlWriter = new TaglistOutputXpp3Writer();
            xmlWriter.write( output, report );
        }
        catch ( Exception e )
        {
            getLog().warn( "Could not save taglist xml file: " + e.getMessage() );
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    /**
     * Returns the path relative to the output directory.
     * 
     * @param location the location to make relative.
     * @return the relative path.
     */
    private String getRelativePath( File location )
    {
        String relativePath =
            PathTool.getRelativePath( getReportOutputDirectory().getAbsolutePath(), location.getAbsolutePath() );
        if ( StringUtils.isEmpty( relativePath ) )
        {
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
    public boolean canGenerateReport()
    {
        boolean canGenerate = !constructSourceDirs().isEmpty();
        if ( aggregate && !getProject().isExecutionRoot() )
        {
            canGenerate = false;
        }
        return canGenerate;
    }

    /**
     * Removes empty dirs from the list.
     * 
     * @param sourceDirectories the original list of directories.
     * @return a new list containing only non empty dirs.
     */
    private List pruneSourceDirs( List sourceDirectories )
    {
        List pruned = new ArrayList( sourceDirectories.size() );
        for ( Iterator i = sourceDirectories.iterator(); i.hasNext(); )
        {
            String dir = (String) i.next();
            if ( !pruned.contains( dir ) && hasSources( new File( dir ) ) )
            {
                pruned.add( dir );
            }
        }
        return pruned;
    }

    /**
     * Checks whether the given directory contains source files.
     * 
     * @param dir the source directory.
     * @return true if the folder or one of its subfolders contains at least 1 source file that matches includes/excludes.
     */
    private boolean hasSources( File dir )
    {
        boolean found = false;
        if ( dir.exists() && dir.isDirectory() )
        {
            try {
                if ( ! FileUtils.getFiles( dir, getIncludes(), getExcludes() ).isEmpty() ) {
                    found = true;
                }
            } catch (IOException e) {
                // should never get here
                getLog().error("Error whilst trying to scan the directory " + dir.getAbsolutePath(), e);
            }
            File[] files = dir.listFiles();
            if ( files != null ) {
                for ( int i = 0; i < files.length && !found; i++ ) {
                    File currentFile = files[i];
                    if ( currentFile.isDirectory() ) {
                        boolean hasSources = hasSources( currentFile );
                        if ( hasSources ) {
                            found = true;
                        }
                    }
                }
            }
        }
        return found;
    }

    /**
     * Construct the list of source directories to analyze.
     * 
     * @return the list of dirs.
     */
    public List constructSourceDirs()
    {
        List dirs = new ArrayList( getProject().getCompileSourceRoots() );
        if ( !skipTestSources )
        {
            dirs.addAll( getProject().getTestCompileSourceRoots() );
        }

        if ( aggregate )
        {
            for ( Iterator i = reactorProjects.iterator(); i.hasNext(); )
            {
                MavenProject reactorProject = (MavenProject) i.next();

                // TODO should this be more like ! "pom".equals(...)
                if ( "java".equals( reactorProject.getArtifact().getArtifactHandler().getLanguage() ) )
                {
                    dirs.addAll( reactorProject.getCompileSourceRoots() );
                    if ( !skipTestSources )
                    {
                        dirs.addAll( reactorProject.getTestCompileSourceRoots() );
                    }
                }
            }
        }

        dirs = pruneSourceDirs( dirs );
        return dirs;
    }

    /**
     * Get the files to include, as a comma separated list of patterns.
     */
    public String getIncludes()
    {
        if ( includes != null ) {
            return String.join(",", includes);
        } else {
            return null;
        }
    }

    /**
     * Get the files to exclude, as a comma separated list of patterns.
     */
    public String getExcludes()
    {
      if ( excludes != null ) {
          return String.join(",", excludes);
      } else {
          return null;
      }
    }

  /**
     * Returns the Locale of the source files.
     * 
     * @return The Locale of the source files.
     */
    public Locale getLocale()
    {
        // The locale string should never be null.
        if ( sourceFileLocale == null )
        {
            sourceFileLocale = DEFAULT_LOCALE;
        }
        return new Locale( sourceFileLocale );
    }

    /**
     * Tells whether to look for comments over multiple lines.
     * 
     * @return Returns true if the analyzer should look for multiple lines.
     */
    public boolean isMultipleLineComments()
    {
        return multipleLineComments;
    }

    /**
     * Tells whether to look for tags without comments.
     * 
     * @return the emptyComments.
     */
    public boolean isEmptyComments()
    {
        return emptyComments;
    }

    /**
     * Tells whether to generate details for tags with zero occurrences.
     * 
     * @return the showEmptyTags.
     */
    public boolean isShowEmptyDetails()
    {
        return showEmptyDetails;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    /**
     * Get the absolute path to the XML output directory.
     * 
     * @return string of the absolute path.
     */
    protected String getXMLOutputDirectory()
    {
        return xmlOutputDirectory.getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.taglist.description" );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.taglist.name" );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "taglist";
    }

    /**
     * Returns the correct resource bundle according to the locale.
     * 
     * @return the bundle corresponding to the locale used for rendering the report.
     */
    public ResourceBundle getBundle()
    {
        return getBundle( currentLocale );
    }

    /**
     * Returns the correct resource bundle according to the locale.
     * 
     * @param locale the locale of the user.
     * @return the bundle corresponding to the locale.
     */
    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "taglist-report", locale, this.getClass().getClassLoader() );
    }

    @Override
    protected String getInputEncoding()
    {
        return super.getInputEncoding();
    }
}
