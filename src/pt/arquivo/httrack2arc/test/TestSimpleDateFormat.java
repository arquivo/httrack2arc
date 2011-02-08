package pt.arquivo.httrack2arc.test;

//TestSimpleDateFormat - Test the parsing of various date formats.
//Original Creator: David Cruz <david.cruz@fccn.pt>
//For: SAW Group - FCCN <sawfccn@fccn.pt>
//Copyright (C) 2009
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestSimpleDateFormat {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		String dateStr = "%20Jan%202009%2011:51:49";
		String dateStr2 = "Tue,%2013%20Aug%202002%2008:43:07%20-0700";
		
		System.out.println( normalize(dateStr2) );
	}
	
	public static Date normalize(String date_string) throws ParseException {
        //SimpleDateFormat from = new SimpleDateFormat("EEE,%20dd%20MMM%20yyyy%20HH:mm:ss", new Locale("en_US"));
		//SimpleDateFormat from = new SimpleDateFormat("%20MMM%20yyyy%20HH:mm:ss");
		//SimpleDateFormat from2 = new SimpleDateFormat("%20MMM%20yyyy%20HH:mm:ss", new Locale("en_US"));
		SimpleDateFormat numericTZFormat = new SimpleDateFormat("EEE,%20dd%20MMM%20yyyy%20HH:mm:ss%20ZZZZZ", new Locale("en_UK"));

		//System.out.println( from.parse(date_string).getTime() );
		//System.out.println( from2.parse(date_string).getTime() );
		
		System.out.println( numericTZFormat.parse(date_string).getTime() );
		
        return numericTZFormat.parse(date_string);
    }

}
