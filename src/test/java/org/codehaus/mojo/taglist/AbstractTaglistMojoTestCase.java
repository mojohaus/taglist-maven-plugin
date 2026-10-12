package org.codehaus.mojo.taglist;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MojoTest
abstract class AbstractTaglistMojoTestCase {
    static final String TEST_ENCODING = "UTF-8";

    @Inject
    private RepositorySystem repositorySystem;

    /**
     * Configures a {@link TagListReport} injected from a test POM. The project gets the {@code java-sources} and
     * {@code test-sources} directories of {@code testDir} under {@code target/test-classes/unit} as its source roots,
     * so the report runs against the test sources and not the plugin's own.
     *
     * @param mojo the injected mojo, never null.
     * @param testDir directory name below {@code unit}, such as {@code basic-config-test}.
     * @return the same mojo.
     * @throws Exception in case of reflection or repository errors.
     */
    protected TagListReport getTagListReport(TagListReport mojo, String testDir) throws Exception {
        assertNotNull(mojo, "Mojo not found.");

        MavenProject project = (MavenProject) MojoExtension.getVariableValueFromObject(mojo, "project");
        String sources = MojoExtension.getBasedir() + "/target/test-classes/unit/" + testDir;
        project.getCompileSourceRoots().clear();
        project.getTestCompileSourceRoots().clear();
        project.getCompileSourceRoots().add(sources + "/java-sources");
        project.getTestCompileSourceRoots().add(sources + "/test-sources");

        DefaultRepositorySystemSession repositorySystemSession = new DefaultRepositorySystemSession();
        repositorySystemSession.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(
                repositorySystemSession, new LocalRepository(System.getProperty("localRepository"))));

        // Test need to download a maven-fluido-skin if not present in local repo
        RemoteRepository remoteRepository =
                new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build();

        File outputDirectory = (File) MojoExtension.getVariableValueFromObject(mojo, "outputDirectory");
        MojoExtension.setVariableValueToObject(mojo, "inputEncoding", TEST_ENCODING);
        MojoExtension.setVariableValueToObject(mojo, "includes", new String[] {"**/*.java"});
        MojoExtension.setVariableValueToObject(mojo, "xmlOutputDirectory", new File(outputDirectory, "taglist"));
        MojoExtension.setVariableValueToObject(mojo, "siteDirectory", new File(outputDirectory, "non-existing"));
        MojoExtension.setVariableValueToObject(mojo, "reactorProjects", singletonList(project));
        MojoExtension.setVariableValueToObject(mojo, "repoSession", repositorySystemSession);
        MojoExtension.setVariableValueToObject(
                mojo,
                "remoteProjectRepositories",
                repositorySystem.newResolutionRepositories(
                        repositorySystemSession, Collections.singletonList(remoteRepository)));
        MojoExtension.setVariableValueToObject(
                mojo, "mojoExecution", new MojoExecution(new Plugin(), "taglist", "default"));

        if (MojoExtension.getVariableValueFromObject(mojo, "sourceFileLocale") == null) {
            MojoExtension.setVariableValueToObject(mojo, "sourceFileLocale", "en");
        }

        return mojo;
    }

    /**
     * Reads the generated taglist report into a String.
     *
     * @param mojo to use as source
     * @return a String containing the contents.
     * @throws IOException in case of generic I/O errors.
     */
    protected String getGeneratedOutput(TagListReport mojo) throws IOException {
        File outputDir = mojo.getReportOutputDirectory();

        String filename = mojo.getOutputName() + ".html";
        File outputHtml = new File(outputDir, filename);
        assertTrue(outputHtml.exists(), "Cannont find output html file");
        return readFileContentWithoutNewLine(outputHtml);
    }

    /**
     * Reads the generated taglist XML report into a String.
     *
     * @param mojo to use as source
     * @return a String containing the contents.
     * @throws IOException in case of generic I/O errors.
     */
    protected String getGeneratedXMLOutput(TagListReport mojo) throws IOException {
        File outputDir = new File(mojo.getXMLOutputDirectory());

        String filename = mojo.getOutputName() + ".xml";
        File outputXML = new File(outputDir, filename);
        assertTrue(outputXML.exists(), "Cannont find output xml file");
        return readFileContentWithoutNewLine(outputXML);
    }

    protected String readFileContentWithoutNewLine(File file) throws IOException {
        return Files.readAllLines(file.toPath(), Charset.forName(TEST_ENCODING)).stream()
                .map(String::trim)
                .collect(Collectors.joining());
    }
}
