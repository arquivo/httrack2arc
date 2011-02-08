package pt.arquivo.httrack2arc.controller;

//LogReader - Reads and interprets the crawl logs of Httrack. This allows
//the next step of creating ARC files with this information.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.arquivo.httrack2arc.model.LogEntry;


public class LogReader {
	public static enum LogColumn {
		STATUS_CODE (4),
		HTTRACK_STATUS (5),
		MIME (6),
		DATE_OR_ETAG_ANNOUNCE (7),
		DATE_OR_ETAG (8),
		URL (10),
		LOCAL (11),
		NO_DATE_LOCAL (8);

		private final int pos;

		private LogColumn(int pos) {
			this.pos = pos;
		}

		public int position() {
			return pos;
		}
	}

	private static Pattern pattern = Pattern.compile(
			"^(\\d{2}:\\d{2}:\\d{2})\\s+" + // Tempo
			"(\\d+/[-\\d]+)\\s+" + // Bytes
			"([-A-Z]{6})\\s+" + // Flags
			"(\\d{3})\\s+" + //HTTP Status Code
			"(\\w+\\s[,'()A-Za-z0-9%]+)\\s+" + // HTTP Status message
			"(\\w+/?[-+\\w\\.]*)\\s+" + // MIME
			//"(date|etag):([-\\w,:%]+%20[-\\dA-Z]{3,5}|(W/)?[-\\w\\.;%:!=]+)\\s+" + // DATE or ETAG
			"(date|etag):([-\\w,:%]+|(W/)?[-\\w\\.;%:!=]+)\\s+" + // DATE or ETAG
			"(\\S+)\\s+" + // URL
			"([-ºª\\w\\./:#@]+)?(\\s*.*)" ); // Local path & referer

	private static Pattern noDatePattern = Pattern.compile(
			"^(\\d{2}:\\d{2}:\\d{2})\\s+" + // Tempo
			"(\\d+/[-\\d]+)\\s+" + // Bytes
			"([-A-Z]{6})\\s+" + // Flags
			"(\\d{3})\\s+" + //HTTP Status Code
			"(\\w+\\s[,'()A-Za-z0-9%]+)\\s+" + // HTTP Status message
			"(\\w+/?[-+\\w\\.]*)\\s+" + // MIME 
			"(\\S+)\\s+" + // URL
			"([-ºª\\w\\./:#@]+)?(\\s*.*)" ); // Local path & referer

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE,%20dd%20MMM%20yyyy%20HH:mm:ss%20zzz", new Locale("en_UK"));
	private static SimpleDateFormat longDateFormat = new SimpleDateFormat("EEEEEEEE,%20dd-MMM-yy%20HH:mm:ss%20zzz", new Locale("en_UK"));
	private static SimpleDateFormat shortdateFormat = new SimpleDateFormat("EEE,%20dd%20MMM%20yyyy%20HH:mm%20zzz", new Locale("en_UK"));
	private static SimpleDateFormat numericTZFormat = new SimpleDateFormat("EEE,%20dd%20MMM%20yyyy%20HH:mm:ss%20ZZZZZ", new Locale("en_UK"));
	private static SimpleDateFormat noTZDateFormat = new SimpleDateFormat("EEE,%20dd%20MMM%20yyyy%20HH:mm:ss", new Locale("en_UK"));

	private BufferedReader reader;

	public void setup( String filename ) throws FileNotFoundException {
		reader = new BufferedReader(
				new FileReader( filename ) 
		);
	}

	public void setup( File file ) throws FileNotFoundException {
		reader = new BufferedReader(
				new FileReader( file )		
		);
	}

	public void reset() {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Collection<LogEntry> parse() {

		Collection<LogEntry> entries = new ArrayList<LogEntry>();

		try {
			String line;

			while ( (line = reader.readLine()) != null ) {

				Matcher match = pattern.matcher(line);
				try {
					match.matches();

					LogEntry entry = new LogEntry();

					String url = match.group(LogColumn.URL.position());
					if ( !url.startsWith("http://") )
						url = "http://" + url;
					entry.setUrl( url );
					entry.setLocal( match.group(LogColumn.LOCAL.position()) );
					entry.setStatusCode( Integer.parseInt(match.group(LogColumn.STATUS_CODE.position())) );
					
					String mime = match.group( LogColumn.MIME.position() );
					
					if ( mime.equals("text")) {
						entry.setMime( "text/plain" );
					} else if ( mime.equals("xpto")) {
						entry.setMime( URLConnection.guessContentTypeFromName(
								match.group(LogColumn.LOCAL.position()) )
						);
					} else {
						entry.setMime( match.group(LogColumn.MIME.position()) );
					}

					if ( match.group(LogColumn.DATE_OR_ETAG_ANNOUNCE.position()).equals("date"))
						entry.setCrawlDate( parseDateString( match.group(LogColumn.DATE_OR_ETAG.position()) ) );
					else
						entry.setEtag(  match.group(LogColumn.DATE_OR_ETAG.position()).replace( "%22", "") );

					entries.add(entry);

				} catch (IllegalStateException e) {

				} catch (Exception e) {

					System.err.print("NO MATCH:\t");
					System.err.println( line );
					e.printStackTrace();
					System.err.println("RETRY without date pattern");
					
					match = noDatePattern.matcher(line);
					
					try {
					match.matches();

					LogEntry entry = new LogEntry();

					String url = match.group(LogColumn.URL.position());
					if ( !url.startsWith("http://") )
						url = "http://" + url;
					entry.setUrl( url );
					entry.setLocal( match.group(LogColumn.NO_DATE_LOCAL.position()) );
					entry.setStatusCode( Integer.parseInt(match.group(LogColumn.STATUS_CODE.position())) );
					
					
					String mime = match.group( LogColumn.MIME.position() );
					
					if ( mime.equals("text")) {
						entry.setMime( "text/plain" );
					} else if ( mime.equals("xpto")) {
						entry.setMime( URLConnection.guessContentTypeFromName(
								match.group(LogColumn.LOCAL.position()) )
						);
					} else {
						entry.setMime( match.group(LogColumn.MIME.position()) );
					}
					
					entries.add(entry);
					} catch (IllegalStateException noDateException) {
						e.printStackTrace();
					}
				}
			}

		} catch (IOException e)  {
			e.printStackTrace();
		} catch (Exception allExcep) {
			allExcep.printStackTrace();
		}

		return entries;
	}

	public Date parseDateString( String dateString ) throws ParseException {
		Date d = null;
		
		try {
			d = dateFormat.parse( dateString);
		} catch (ParseException e) {
			try {
				d = longDateFormat.parse( dateString );
			} catch (ParseException e1) {
				try {
					d = shortdateFormat.parse( dateString );
				} catch (ParseException e2) {
					try {
						d = numericTZFormat.parse( dateString );
					} catch (ParseException e3) {
						try {
							d = noTZDateFormat.parse( dateString );
						} catch (ParseException e4) {
							throw new ParseException("Not a valid date fomat: "+ dateString, 0);
						}
					}
				}
			}
		}
		return d;
	}

}
