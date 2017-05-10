package com.proxy.kiwi.core.v2.folder;

import java.io.File;
import java.util.Comparator;

public class FileComparators {

	public static Comparator<File> WINDOWS_LIKE = new Comparator<File>() {

	      private String str1, str2;
	      private int pos1, pos2, len1, len2;

	      public int compare(File f1, File f2)
	      {

	    	str1 = f1.getName();
	        str2 = f2.getName();
	        len1 = str1.length();
	        len2 = str2.length();
	        pos1 = pos2 = 0;

	        int result = 0;
	        while (result == 0 && pos1 < len1 && pos2 < len2)
	        {
	          char ch1 = str1.charAt(pos1);
	          char ch2 = str2.charAt(pos2);

	          if (Character.isDigit(ch1))
	          {
	            result = Character.isDigit(ch2) ? compareNumbers() : -1;
	          }
	          else if (Character.isLetter(ch1))
	          {
	            result = Character.isLetter(ch2) ? compareOther(true) : 1;
	          }
	          else
	          {
	            result = Character.isDigit(ch2) ? 1
	                   : Character.isLetter(ch2) ? -1
	                   : compareOther(false);
	          }

	          pos1++;
	          pos2++;
	        }

	        return result == 0 ? len1 - len2 : result;
	      }

	      private int compareNumbers()
	      {
	        int end1 = pos1 + 1;
	        while (end1 < len1 && Character.isDigit(str1.charAt(end1)))
	        {
	          end1++;
	        }
	        int fullLen1 = end1 - pos1;
	        while (pos1 < end1 && str1.charAt(pos1) == '0')
	        {
	          pos1++;
	        }

	        int end2 = pos2 + 1;
	        while (end2 < len2 && Character.isDigit(str2.charAt(end2)))
	        {
	          end2++;
	        }
	        int fullLen2 = end2 - pos2;
	        while (pos2 < end2 && str2.charAt(pos2) == '0')
	        {
	          pos2++;
	        }

	        int delta = (end1 - pos1) - (end2 - pos2);
	        if (delta != 0)
	        {
	          return delta;
	        }

	        while (pos1 < end1 && pos2 < end2)
	        {
	          delta = str1.charAt(pos1++) - str2.charAt(pos2++);
	          if (delta != 0)
	          {
	            return delta;
	          }
	        }

	        pos1--;
	        pos2--; 

	        return fullLen2 - fullLen1;
	      }

	      private int compareOther(boolean isLetters)
	      {
	        char ch1 = str1.charAt(pos1);
	        char ch2 = str2.charAt(pos2);

	        if (ch1 == ch2)
	        {
	          return 0;
	        }

	        if (isLetters)
	        {
	          ch1 = Character.toUpperCase(ch1);
	          ch2 = Character.toUpperCase(ch2);
	          if (ch1 != ch2)
	          {
	            ch1 = Character.toLowerCase(ch1);
	            ch2 = Character.toLowerCase(ch2);
	          }
	        }

	        return ch1 - ch2;
	      }

	    };
}
