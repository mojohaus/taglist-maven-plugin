package com;

/** This is the tags test class.
  *
  * @todo: This is the default tag #1.
  * TODO: This is the default tag #2.
  * 
  */
class Tags
{
	/** This is the test for the output file check.
	 * 
	 * @create_output: This is the tag for the output test.
	 */
	void tag1() { }
	
	/** Test a C style comment in souce code.
	 * 
	 */
	void tag2() 
	{
		/* c_style_tag: This is a C style tag. */
	}
	
	/** Test a C++ style comment in souce code.
	 * 
	 */
	void tag3() 
	{
		// c++_style_tag: This is a C++ style tag.
	}
	
	/** Test a JavaDoc single style comment in souce code.
	 * 
	 */
	void tag4() 
	{
		/** javadoc_single_style_tag: This is a JavaDoc single style tag.
	}
	
	/** Test a JavaDoc multi style comment in souce code.
	 * 
	 */
	void tag5() 
	{
		/** 
		 * javadoc_multi_style_tag: This is a JavaDoc multi style tag.
		 */
	}
	
	/** Test a tag not at the start of a line.
	 * 
	 */
	void tag6() 
	{
		/** 
		 * The tag "not_start_of_line_tag" should NOT be found.
		 */
	}
	
	/** Test a tag variable in source code.
	 * 
	 */
	void tag7() 
	{
		int source_code_variable_tag;
		
		source_code_variable_tag += 7;
	}

};
