package com;

/** This is the basic config test class.
  *
  * 
  */
class BasicConfig
{
	/** This is the test for the output file check.
	 * 
	 * @create_output: This is the tag for the output test.
	 */
	def basic1() { }
	
	/** Multiple line comments tests.
	 * 
	 * @multiple_line_comment: This is line one,
	 * this is line two,
	 * and this is line three.
	 */
	def basic2() { }
	
	/** Empty comments tests.
	 * 
	 * @empty_comment
	 */
	def basic3() { }
	
	/** Colons tests.
	 * 
	 * @colons This is without colon.
	 * 
	 * @colons: This is with colon.
	 */
	def basic3() { }
	
	/** Empty comments and colons tests.
	 * 
	 * NOTE:  The following tag MUST be on line #40 for the unit.scala test to pass.
	 * @empty_no_colons
	 * 
	 * NOTE:  The following tag MUST be on line #43 for the unit.scala test to pass.
	 * @empty_colons:
	 */
	def basic3() { }
	
	/** Show empty details tests.
	 * 
	 * @show_empty_details_tag_in_code this tag is in the Java code
	 * 
	 * NOTE:  The tag @show_empty_details_tag_not_in_code is NOT here on 
	 */
	def basic4() { }
	
	

};
