package org.codehaus.mojo.taglist;

import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

class TagListReportSourceFileLocaleTest {

    static Stream<Arguments> sourceFileLocaleParserTest() {
        return Stream.of(
                of("en", new Locale("en")),
                of("en_US", new Locale("en", "US")),
                of("en_US_win", new Locale("en", "US", "win")),
                of("en_US_win_XX", Locale.ENGLISH),
                of("", Locale.ENGLISH));
    }

    @ParameterizedTest
    @MethodSource
    void sourceFileLocaleParserTest(String inputLocale, Locale expectedLocale) {
        TagListReport tagListReport = new TagListReport();
        tagListReport.setSourceFileLocale(inputLocale);
        assertEquals(expectedLocale, tagListReport.getSourceFileLocale());
    }
}
