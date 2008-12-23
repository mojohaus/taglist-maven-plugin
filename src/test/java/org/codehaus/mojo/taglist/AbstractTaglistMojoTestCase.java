package org.codehaus.mojo.taglist;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public abstract class AbstractTaglistMojoTestCase
    extends AbstractMojoTestCase
{

    TagListReport getTagListReport( File pluginXmlFile )
        throws Exception
    {
        TagListReport mojo = (TagListReport)lookupMojo( "taglist", pluginXmlFile );
        assertNotNull( "Mojo not found.", mojo );
        setVariableValueToObject( mojo, "encoding", "UTF-8" );
        
        return mojo;
    }
    
}
