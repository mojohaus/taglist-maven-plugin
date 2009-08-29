package com;

/** This is the Locale test class.
  *
  * 
  */
class LocaleClass
{
	/** This is the test for an English comment.
	 * 
	 * EnglishLocaleExactTag: Should match under Locale en  1 of 3.
	 * 
	 * EnglishLocaleIgnoreCaseTag: Should match under Locale en  2 of 3.
	 * 
	 * EnglishLocaleRegEx7Tag: Should match under Locale en  3 of 3.
	 */
	void tc1() { }
	
	/** This is the test for an Turkish comment.
	 * 
	 * TurkishLocaleExactTag: Should match under Locale tr  1 of 2.
	 * 
	 * TurkishLocaleIgnoreCaseTag: Will not match in Turkish Locale because toLower of 'I'.
	 * 
	 * TurkishLocaleRegEx7Tag: Should match under Locale tr  2 of 2.
	 */
	void tc2() { }
	
	
	

};
