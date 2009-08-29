package com;

/** This is the tag classes test class.
  *
  * 
  */
class TagClasses
{
	/** This is the test for a defaulting exact match.
	 * 
	 * exactMatchDefaultString: This is the tag for the exact match default 1 of 1.
	 * 
	 * exactmatchDefaultString:  This should NOT match.
	 */
	void tc1() { }
	
	/** This is the test for a specified exact match.
	 * 
	 * exactMatchString:  This is hte tag for the exact match 1 of 1.
	 * 
	 * exactmatchstring:  This should NOT match.
	 */
	void tc2() { }
	
	/** This is the test case for the ignore case match.
	 * 
	 * ignoreCase:  ignore case 1 of 3.
	 * igNorecAse:  ignore case 2 of 3.
	 * IGNORECASE:  ignore case 3 of 3.
	 * 
	 * ignore2case:  This should NOT match.
	 */
	void tc3() { }
	
	/** This is the test case for the regular expression match.
	 * 
	 * regEx1:  reg ex match 1 of 3.
	 * regEx2:  reg ex match 2 of 3.
	 * regEx3:  reg ex match 3 of 3.
	 * 
	 * regEx44:  this should NOT match.
	 */
	void tc4() { }
	
	

};
