package org.codehaus.mojo.taglist;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public abstract class AbstractTaglistMojoTestCase
    extends AbstractMojoTestCase
{

    /**
     * Returns a {@link TagListReport} configured by pluginXmlFile.
     * @param pluginXmlFile file to configure Mojo with, must exist.
     * @return a configured Mojo, never null.
     * @throws Exception in case of non-existing pluginXmlFile or mojo not found.
     */
    TagListReport getTagListReport( File pluginXmlFile )
        throws Exception
    {
        assertTrue ("Cannot find plugin file.", pluginXmlFile.exists());
        TagListReport mojo = (TagListReport)lookupMojo( "taglist", pluginXmlFile );
        assertNotNull( "Mojo not found.", mojo );
        setVariableValueToObject( mojo, "encoding", "UTF-8" );
        
        return mojo;
    }
    
}
