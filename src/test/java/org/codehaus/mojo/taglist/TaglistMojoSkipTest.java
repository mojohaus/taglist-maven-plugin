package org.codehaus.mojo.taglist;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaglistMojoSkipTest extends AbstractTaglistMojoTestCase {

    @Test
    void noSourcesDirectory() throws Exception {
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
