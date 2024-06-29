package org.codehaus.mojo.taglist;

import java.io.File;

public class TaglistMojoSkipTest extends AbstractTaglistMojoTestCase {

    public void testNoSourcesDirectory() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "src/test/resources/unit/no-sources-test/default-pom.xml");

        TagListReport mojo = super.getTagListReport(pluginXmlFile);

        // Run the TagList mojo
        mojo.execute();

        // output should not be generated
        File outputDirectory = mojo.getReportOutputDirectory();
        assertNotNull(outputDirectory);
        assertFalse(outputDirectory.exists());
    }
}
