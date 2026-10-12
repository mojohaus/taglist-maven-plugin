package org.codehaus.mojo.taglist;

import java.io.File;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaglistMojoSkipTest extends AbstractTaglistMojoTestCase {

    @Test
    @InjectMojo(goal = "taglist", pom = "src/test/resources/unit/no-sources-test/default-pom.xml")
    void testNoSourcesDirectory(TagListReport mojo) throws Exception {
        getTagListReport(mojo, "no-sources-test");

        // Run the TagList mojo
        mojo.execute();

        // output should not be generated
        File outputDirectory = mojo.getReportOutputDirectory();
        assertNotNull(outputDirectory);
        assertFalse(outputDirectory.exists());
    }
}
