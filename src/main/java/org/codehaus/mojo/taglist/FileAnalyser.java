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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.taglist.beans.FileReport;
import org.codehaus.mojo.taglist.beans.TagReport;
import org.codehaus.mojo.taglist.tags.TagClass;
import org.codehaus.plexus.util.FileUtils;

/**
 * Class that analyzes a file with a special comment tag. For instance:
 *
 * <pre>
 * // TODO: Example of an Eclipse/IntelliJ-like "todo" tag
 * </pre>
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard </a>
 * @todo : This is another example of "todo" tag
 */
public class FileAnalyser {
    /**
     * String that is used for beginning a comment line.
     */
    private static final String STAR_COMMENT = "*";

    /**
     * String that is used for beginning a comment line.
     */
    private static final String SLASH_COMMENT = "//";

    /**
     * Maximum length of a comment.
     */
    private static final int MAX_COMMENT_CHARACTERS = 99999;

    /**
     * The character encoding of the files to analyze.
     */
    private final String encoding;

    /**
     * The Locale of the files to analyze.
     */
    private final Locale sourceFileLocale;

    /**
     * The directories to analyze.
     */
    private final Collection<String> sourceDirs;

    /**
     * The files to include, as a comma separated list of patterns.
     */
    private final String includes;

    /**
     * The files top exclude, as a comma separated list of patterns.
     */
    private final String excludes;

    /**
     * Log for debug output.
     */
    private final Log log;

    /**
     * Set to true if the analyzer should look for multiple line comments.
     */
    private final boolean multipleLineCommentsOn;

    /**
     * Set to true if the analyzer should look for tags without comments.
     */
    private final boolean emptyCommentsOn;

    /**
     * ArrayList of tag classes.
     */
    private final List<TagClass> tagClasses;

    /**
     * Constructor.
     *
     * @param report the MOJO that is using this analyzer.
     * @param tagClasses the array of tag classes to use for searching
     */
    public FileAnalyser(TagListReport report, List<TagClass> tagClasses) {
        // TODO - direct class TagListReport should not be used,
        //  we can add a separate args or new class/interface for group of args
        multipleLineCommentsOn = report.isMultipleLineComments();
        emptyCommentsOn = report.isEmptyComments();
        log = report.getLog();
        sourceDirs = report.getSourceDirs();
        encoding = report.getInputEncoding();
        sourceFileLocale = report.getSourceFileLocale();
        this.tagClasses = tagClasses;
        this.includes = report.getIncludesCommaSeparated();
        this.excludes = report.getExcludesCommaSeparated();
    }

    /**
     * Execute the analysis for the configuration given by the TagListReport.
     *
     * @return a collection of TagReport objects.
     */
    public Collection<TagReport> execute() throws IOException {
        List<File> fileList = findFilesToScan();

        for (File file : fileList) {
            if (file.exists()) {
                scanFile(file);
            }
        }

        // Get the tag reports from each of the tag classes.
        Collection<TagReport> tagReports = new ArrayList<>();
        for (TagClass tc : tagClasses) {
            tagReports.add(tc.getTagReport());
        }

        return tagReports;
    }

    /**
     * Gives the list of files to scan.
     *
     * @return a List of File objects.
     */
    private List<File> findFilesToScan() throws IOException {
        List<File> filesList = new ArrayList<>();
        for (String sourceDir : sourceDirs) {
            filesList.addAll(FileUtils.getFiles(new File(sourceDir), includes, excludes));
        }
        return filesList;
    }

    /**
     * Access an input reader that uses the current file encoding.
     *
     * @param file the file to open in the reader.
     * @return a reader with the current file encoding.
     * @throws IOException the IO exception.
     */
    private Reader getReader(File file) throws IOException {
        InputStream in = Files.newInputStream(file.toPath());
        return (encoding == null) ? new InputStreamReader(in) : new InputStreamReader(in, encoding);
    }

    /**
     * Scans a file to look for task tags.
     *
     * @param file the file to scan.
     */
    public void scanFile(File file) {
        try (LineNumberReader reader = new LineNumberReader(getReader(file))) {

            String currentLine = reader.readLine();
            while (currentLine != null) {
                int index;
                // look for a tag on this line
                for (TagClass tagClass : tagClasses) {
                    index = tagClass.tagMatchContains(currentLine, sourceFileLocale);
                    if (index != TagClass.NO_MATCH) {
                        // there's a tag on this line
                        String commentType = extractCommentType(currentLine, index);
                        if (commentType == null) {
                            // this is not a valid comment tag: skip other tag classes and
                            // go to the next line
                            break;
                        }

                        int tagLength = tagClass.getLastTagMatchStringLength();
                        int commentStartIndex = reader.getLineNumber();
                        StringBuilder comment = new StringBuilder();

                        String firstLine = StringUtils.strip(currentLine.substring(index + tagLength));
                        firstLine = StringUtils.removeEnd(firstLine, "*/"); // MTAGLIST-35
                        if (firstLine.isEmpty() || ":".equals(firstLine)) {
                            // this is not a valid comment tag: nothing is written there
                            if (!emptyCommentsOn) {
                                continue;
                            }
                        } else {
                            // this tag has a comment
                            if (firstLine.charAt(0) == ':') {
                                comment.append(firstLine.substring(1).trim());
                            } else {
                                comment.append(firstLine);
                            }

                            if (multipleLineCommentsOn) {
                                // Mark the current position, set the read forward limit to
                                // a large number that should not be met.
                                reader.mark(MAX_COMMENT_CHARACTERS);

                                // next line
                                String futureLine = reader.readLine();

                                // we're looking for multiple line comments
                                while (futureLine != null
                                        && futureLine.trim().startsWith(commentType)
                                        && !futureLine.contains(tagClass.getLastTagMatchString())) {
                                    String currentComment = futureLine
                                            .substring(futureLine.indexOf(commentType) + commentType.length())
                                            .trim();
                                    if (currentComment.startsWith("@")
                                            || currentComment.isEmpty()
                                            || "/".equals(currentComment)) {
                                        // the comment is finished
                                        break;
                                    }
                                    // try to look if the next line is not a new tag
                                    boolean newTagFound = false;
                                    for (TagClass tc : tagClasses) {
                                        if (tc.tagMatchStartsWith(currentComment, sourceFileLocale)) {
                                            newTagFound = true;
                                            break;
                                        }
                                    }
                                    if (newTagFound) {
                                        // this is a new comment: stop here the current comment
                                        break;
                                    }
                                    // nothing was found: this means the comment is going on this line
                                    comment.append(" ");
                                    comment.append(currentComment);
                                    futureLine = reader.readLine();
                                }

                                // Reset the reader to the marked position before the multi
                                // line check was performed.
                                reader.reset();
                            }
                        }
                        TagReport tagReport = tagClass.getTagReport();
                        FileReport fileReport = tagReport.getFileReport(file, encoding);
                        fileReport.addComment(comment.toString(), commentStartIndex);
                    }
                }
                currentLine = reader.readLine();
            }
        } catch (IOException e) {
            log.error("Error while scanning the file " + file.getPath(), e);
        }
    }

    /**
     * Finds the type of comment the tag is in.
     *
     * @param currentLine the line to analyze.
     * @param index the index of the tag in the line.
     * @return "*" or "//" or null.
     */
    private String extractCommentType(String currentLine, int index) {
        String commentType = null;
        String beforeTag = currentLine.substring(0, index).trim();
        if (beforeTag.endsWith(SLASH_COMMENT)) {
            commentType = SLASH_COMMENT;
        } else if (beforeTag.endsWith(STAR_COMMENT)) {
            commentType = STAR_COMMENT;
        }
        return commentType;
    }
}
