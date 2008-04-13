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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.model.ReportPlugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.plexus.util.PathTool;
import org.codehaus.plexus.util.StringUtils;

/**
 * Scans the source files for tags and generates a report on their occurrences.
 * 
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard</a>
 * @goal taglist
 */
public class TagListReport
    extends AbstractMavenReport
{
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private SiteRenderer siteRenderer;

    /**
     * Source directories of the project.
     * 
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List sourceDirs;

    /**
     * Test directories of the project.
     * 
     * @parameter expression="${project.testCompileSourceRoots}"
     * @required
     * @readonly
     */
    private List testSourceDirs;

    /**
     * The output directory for the report. Note that this parameter is only evaluated if the goal is run directly from
     * the command line or from a build life cycle phase. If the goal is run indirectly as part of a site generation, the
     * output directory configured in the Maven Site Plugin is used instead.
     * 
     * @parameter default-value="${project.reporting.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * List of tags to look for, specified as &lt;tag&gt; tags. The tags can be either:
     * <ul>
     * <li>Javadoc tags: "@todo" for instance</li>
     * <li>Simple tags: "TODO" for instance. In this case, the tags will be searched in any Java comment (//, /* or
     * /**).</li>
     * </ul>
     * 
     * @parameter
     */
    private String[] tags;

    /**
     * This parameter indicates whether for simple tags (like "TODO"), the analyser should look for multiple line
     * comments.
     * 
     * @parameter default-value="true"
     */
    private boolean multipleLineComments;

    /**
     * This parameter indicates whether to look for tags even if they don't have a comment.
     * 
     * @parameter default-value="true"
     */
    private boolean emptyComments;

    /**
     * Link the tag line numbers to the source xref. Defaults to true and will link automatically if jxr plugin is being
     * used.
     * 
     * @parameter expression="${linkXRef}" default-value="true"
     */
    private boolean linkXRef;

    /**
     * Location of the Xrefs to link to.
     * 
     * @parameter default-value="${project.reporting.outputDirectory}/xref"
     */
    private File xrefLocation;

    /**
     * Location of the Test Xrefs to link to.
     * 
     * @parameter default-value="${project.reporting.outputDirectory}/xref-test"
     */
    private File testXrefLocation;

    /**
     * The projects in the reactor for aggregation report.
     * 
     * @parameter expression="${reactorProjects}"
     * @readonly
     */
    private List reactorProjects;

    /**
     * Whether to build an aggregated report at the root, or build individual reports.
     * 
     * @parameter expression="${aggregate}" default-value="false"
     */
    private boolean aggregate;

    /**
     * The locale used for rendering the page.
     */
    private Locale currentLocale;

    /**
     * This parameter indicates whether to generate details for tags with zero occurrences.
     * 
     * @parameter default-value="false"
     * @since 2.2
     */
    private boolean showEmptyDetails;

    /**
     * Cf. overriden method documentation. {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        this.currentLocale = locale;

        if ( tags == null || tags.length == 0 )
        {
            tags = new String[] { "@todo", "TODO" };
        }

        // let's proceed to the analysis
        FileAnalyser fileAnalyser = new FileAnalyser( this );
        Collection tagReports = fileAnalyser.execute();

        // Renders the report
        ReportGenerator generator = new ReportGenerator( this, tagReports );
        if ( linkXRef )
        {
            String relativePath = getRelativPath( xrefLocation );
            if ( xrefLocation.exists() )
            {
                // XRef was already generated by manual execution of a lifecycle binding
                generator.setXrefLocation( relativePath );
                generator.setTestXrefLocation( getRelativPath( testXrefLocation ) );
            }
            else
            {
                // Not yet generated - check if the report is on its way
                for ( Iterator reports = project.getReportPlugins().iterator(); reports.hasNext(); )
                {
                    ReportPlugin report = (ReportPlugin) reports.next();

                    String artifactId = report.getArtifactId();
                    if ( "maven-jxr-plugin".equals( artifactId ) || "jxr-maven-plugin".equals( artifactId ) )
                    {
                        generator.setXrefLocation( relativePath );
                        generator.setTestXrefLocation( getRelativPath( testXrefLocation ) );
                    }
                }
            }

            if ( generator.getXrefLocation() == null )
            {
                getLog().warn( "Unable to locate Source XRef to link to - DISABLED" );
            }
        }
        generator.generateReport();
    }

    /**
     * Returns the path relativ to the output directory.
     * 
     * @param location the location to make relativ.
     * @return the relativ path.
     */
    private String getRelativPath( File location )
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
     * Cf. overriden method documentation. {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.MavenReport#canGenerateReport()
     */
    public boolean canGenerateReport()
    {
        boolean canGenerate = !constructSourceDirs().isEmpty();
        if ( aggregate && !project.isExecutionRoot() )
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
     * Checks whether the given directory contains Java files.
     * 
     * @param dir the source directory.
     * @return true if the folder or one of its subfolders coantins at least 1 Java file.
     */
    private boolean hasSources( File dir )
    {
        boolean found = false;
        if ( dir.exists() && dir.isDirectory() )
        {
            File[] files = dir.listFiles();
            for ( int i = 0; i < files.length && !found; i++ )
            {
                File currentFile = files[i];
                if ( currentFile.isFile() && currentFile.getName().endsWith( ".java" ) )
                {
                    found = true;
                }
                else if ( currentFile.isDirectory() )
                {
                    boolean hasSources = hasSources( currentFile );
                    if ( hasSources )
                    {
                        found = true;
                    }
                }
            }
        }
        return found;
    }

    /**
     * Construct the list of source directories to analyse.
     * 
     * @return the list of dirs.
     */
    public List constructSourceDirs()
    {
        List dirs = new ArrayList( sourceDirs );
        dirs.addAll( testSourceDirs );

        if ( aggregate )
        {
            for ( Iterator i = reactorProjects.iterator(); i.hasNext(); )
            {
                MavenProject reactorProject = (MavenProject) i.next();

                if ( "java".equals( reactorProject.getArtifact().getArtifactHandler().getLanguage() ) )
                {
                    dirs.addAll( reactorProject.getCompileSourceRoots() );
                    dirs.addAll( reactorProject.getTestCompileSourceRoots() );
                }
            }
        }

        dirs = pruneSourceDirs( dirs );
        return dirs;
    }

    /**
     * Returns the tags to look for.
     * 
     * @return a collection of String objects representing the tag names.
     */
    public String[] getTags()
    {
        return tags;
    }

    /**
     * Tells whether to look for comments over multiple lines.
     * 
     * @return Returns true if the analyser should look for multiple lines.
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
     * Cf. overriden method documentation. {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * Cf. overriden method documentation. {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    /**
     * Cf. overriden method documentation. {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    public MavenProject getProject()
    {
        return project;
    }

    /**
     * Cf. overriden method documentation. {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.taglist.description" );
    }

    /**
     * Cf. overriden method documentation. {@inheritDoc}
     * 
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.taglist.name" );
    }

    /**
     * Cf. overriden method documentation. {@inheritDoc}
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
     * @return the bundle correponding to the locale used for rendering the report.
     */
    public ResourceBundle getBundle()
    {
        return getBundle( currentLocale );
    }

    /**
     * Returns the correct resource bundle according to the locale.
     * 
     * @param locale the locale of the user.
     * @return the bundle correponding to the locale.
     */
    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "taglist-report", locale, this.getClass().getClassLoader() );
    }

}
