package org.codehaus.mojo.taglist;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractTaglistMojoTestCase
    extends AbstractMojoTestCase
{
    public static final String TEST_ENCODING = "UTF-8";

    /**
     * Returns a {@link TagListReport} configured by pluginXmlFile.
     * 
     * @param pluginXmlFile file to configure Mojo with, must exist.
     * @return a configured Mojo, never null.
     * @throws Exception in case of non-existing pluginXmlFile or mojo not found.
     */
    protected TagListReport getTagListReport( File pluginXmlFile )
        throws Exception
    {
        assertTrue( "Cannot find plugin file.", pluginXmlFile.exists() );
        TagListReport mojo = (TagListReport) lookupMojo( "taglist", pluginXmlFile );
        assertNotNull( "Mojo not found.", mojo );
        setVariableValueToObject( mojo, "encoding", TEST_ENCODING );
        setVariableValueToObject( mojo, "xmlOutputDirectory", new File( mojo.getOutputDirectory(), 
                                                                        "taglist" ) );

        return mojo;
    }

    /**
     * Reads the generated taglist report into a String.
     * @param mojo to use as source
     * @return a String containing the contents.
     * @throws IOException in case of generic I/O errors.
     */
    protected String getGeneratedOutput( TagListReport mojo )
        throws IOException
    {
        File outputDir = mojo.getReportOutputDirectory();

        String filename = mojo.getOutputName() + ".html";
        File outputHtml = new File( outputDir, filename );
        assertTrue( "Cannont find output html file", outputHtml.exists() );
        String htmlString = FileUtils.fileRead( outputHtml, TEST_ENCODING );

        return htmlString;
    }
    
    /**
     * Reads the generated taglist XML report into a String.
     * @param mojo to use as source
     * @return a String containing the contents.
     * @throws IOException in case of generic I/O errors.
     */
    protected String getGeneratedXMLOutput( TagListReport mojo )
        throws IOException
    {
        File outputDir = new File(mojo.getXMLOutputDirectory());

        String filename = mojo.getOutputName() + ".xml";
        File outputXML = new File( outputDir, filename );
        assertTrue( "Cannont find output xml file", outputXML.exists() );
        String xmlString = FileUtils.fileRead( outputXML, TEST_ENCODING );

        return xmlString;
    }

}
