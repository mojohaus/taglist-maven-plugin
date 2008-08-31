package org.codehaus.mojo.taglist.beans;

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
import org.codehaus.plexus.PlexusTestCase;

/**
 * Tests for file report.
 *
 * @author Dennis Lundberg
 */
public class FileReportTestCase extends PlexusTestCase
{
  public void testGetClassName() {
    File file = new File(getBasedir() + "/src/test/resources/org/codehaus/mojo/taglist/beans/XYjavatest.java");
    FileReport fileReport = new FileReport(file, "UTF-8");
    assertEquals("org.codehaus.mojo.taglist.beans.XYjavatest", fileReport.getClassName());
  }
}