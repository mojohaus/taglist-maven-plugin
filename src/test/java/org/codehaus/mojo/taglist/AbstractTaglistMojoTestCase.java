package org.codehaus.mojo.taglist;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public abstract class AbstractTaglistMojoTestCase extends AbstractMojoTestCase {
    public static final String TEST_ENCODING = "UTF-8";

    /**
     * Returns a {@link TagListReport} configured by pluginXmlFile.
     *
     * @param pluginXmlFile file to configure Mojo with, must exist.
     * @return a configured Mojo, never null.
     * @throws Exception in case of non-existing pluginXmlFile or mojo not found.
     */
    protected TagListReport getTagListReport(File pluginXmlFile) throws Exception {
        assertTrue("Cannot find plugin file.", pluginXmlFile.exists());
        TagListReport mojo = (TagListReport) lookupMojo("taglist", pluginXmlFile);
        assertNotNull("Mojo not found.", mojo);
        setVariableValueToObject(mojo, "inputEncoding", TEST_ENCODING);
        setVariableValueToObject(mojo, "includes", new String[] {"**/*.java"});
        setVariableValueToObject(mojo, "xmlOutputDirectory", new File(mojo.getOutputDirectory(), "taglist"));

        return mojo;
    }

    /**
     * Reads the generated taglist report into a String.
     * @param mojo to use as source
     * @return a String containing the contents.
     * @throws IOException in case of generic I/O errors.
     */
    protected String getGeneratedOutput(TagListReport mojo) throws IOException {
        File outputDir = mojo.getReportOutputDirectory();

        String filename = mojo.getOutputName() + ".html";
        File outputHtml = new File(outputDir, filename);
        assertTrue("Cannont find output html file", outputHtml.exists());
        return readFileContentWithoutNewLine(outputHtml);
    }

    /**
     * Reads the generated taglist XML report into a String.
     * @param mojo to use as source
     * @return a String containing the contents.
     * @throws IOException in case of generic I/O errors.
     */
    protected String getGeneratedXMLOutput(TagListReport mojo) throws IOException {
        File outputDir = new File(mojo.getXMLOutputDirectory());

        String filename = mojo.getOutputName() + ".xml";
        File outputXML = new File(outputDir, filename);
        assertTrue("Cannont find output xml file", outputXML.exists());
        return readFileContentWithoutNewLine(outputXML);
    }

    protected String readFileContentWithoutNewLine(File file) throws IOException {
        return Files.readAllLines(file.toPath(), Charset.forName(TEST_ENCODING)).stream()
                .map(String::trim)
                .collect(Collectors.joining());
    }
}
