package org.codehaus.mojo.taglist.stubs;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;

/**
 * The Maven Project stub file for testing the different types of tags.
 * 
 * This stub is used to get the test and source file directories.  This
 * allows the TagList plugin unit tests run against the unit test directories
 * instead of using the default project directories.
 */
public class TagsProjectStub
    extends org.apache.maven.plugin.testing.stubs.MavenProjectStub
{	
	public List<String> getCompileSourceRoots()
	{
		return Collections.singletonList(
				PlexusTestCase.getBasedir() + "/target/test-classes/unit/tag-test/java-sources" );
	}

	public List<String> getTestCompileSourceRoots()
	{
		return Collections.singletonList(
				PlexusTestCase.getBasedir() + "/target/test-classes/unit/tag-test/test-sources" );
	}	
}
