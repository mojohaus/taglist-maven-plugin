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

import java.io.File;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.site.renderer.SiteRenderer;

/**
 * MOJO for the tag list report.
 * 
 * @goal taglist
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard </a>
 */
public class TagListReport
    extends AbstractMavenReport
{

    /**
     * @parameter expression="${project}"
     * @required @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${component.org.codehaus.doxia.site.renderer.SiteRenderer}"
     * @required @readonly
     */
    private SiteRenderer siteRenderer;

    /**
     * Specifies the location of the source files to be used for Checkstyle
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private String sourceDirectory;

    /**
     * Output folder where the report will be copied to.
     * @parameter expression="${project.build.directory}/site"
     * @required
     */
    private String outputDirectory;

    /**
     * List of tags to look for, specified as &lt;tag&gt; tags.
     * The tags can be either:
     * <ul>
     * <li>Javadoc tags: "@todo" for instance</li>
     * <li>Simple tags: "TODO" for instance. In this case, the tags will be
     * searched in any Java comment (//, /* or /**).</li>
     * </ul>
     * 
     * @parameter
     */
    private String[] tags;

    /**
     * This parameter indicates whether for simple tags (like "TODO"), the
     * analyser should look for multiple line comments.
     * 
     * @parameter default-value="true"
     */
    private boolean multipleLineComments;

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        if ( tags == null || tags.equals( "" ) )
        {
            tags = new String[] { "@todo", "TODO" };
        }

        File outputDir = new File( outputDirectory );
        if ( !outputDir.exists() )
        {
            boolean success = outputDir.mkdirs();
            if ( !success )
            {
                throw new MavenReportException( "Folder " + outputDirectory + " could not be created." );
            }
        }

        // let's proceed to the analysis
        FileAnalyser fileAnalyser = new FileAnalyser( this );
        Collection tagReports = fileAnalyser.execute();

        // Renders the report
        ReportGenerator.generateReport( tagReports, getBundle( locale ), getSink() );
    }

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.apache.maven.reporting.MavenReport#canGenerateReport()
     */
    public boolean canGenerateReport()
    {
        File srcDir = new File( sourceDirectory );
        if ( !srcDir.exists() )
        {
            return false;
        }
        return true;
    }

    /**
     * Returns the tags to look for.
     * @return a collection of String objects representing the tag names.
     */
    public String[] getTags()
    {
        return tags;
    }

    /**
     * Tells whether we should look for comments over multiple lines 
     * @return Returns true if the analyser should look for multiple lines.
     */
    public boolean isMultipleLineComments()
    {
        return multipleLineComments;
    }

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    public MavenProject getProject()
    {
        return project;
    }

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.taglist.description" );
    }

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.taglist.name" );
    }

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "taglist";
    }

    /**
     * Returns the correct resource bundle according to the locale
     * 
     * @param locale :
     *            the locale of the user
     * @return the bundle correponding to the locale
     */
    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "taglist-report", locale, this.getClass().getClassLoader() );
    }

}