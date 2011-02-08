package pt.arquivo.httrack2arc;

//HTTrack2ArcConverter - Handles the conversion process of Httrack crawls to ARC files.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pt.arquivo.httrack2arc.controller.Exporter;
import pt.arquivo.httrack2arc.controller.RecursiveCrawlExporter;

public class HTTrack2ArcConverter {

	private File source;
	private File destination;
	private Date defaultDate;
	
	private Exporter exporter;
	
	public HTTrack2ArcConverter(String s, String d, Date date) throws FileNotFoundException {
		source = new File(s);
		destination = new File(d);
		defaultDate = date;
		
		if ( !source.exists() || !destination.exists() ) {
			throw new FileNotFoundException("Source: "+ source +"\tDestination: "+ destination );
		}
		
		//exporter = new SingleCrawlExporter();
		exporter = new RecursiveCrawlExporter();
	}
	
	public void run() {
		exporter.setup( source, destination, defaultDate );
		exporter.process();
		exporter.finalize();
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
		
		if ( args.length == 2 ) {
			HTTrack2ArcConverter converter = new HTTrack2ArcConverter(args[0], args[1],
					new Date());
			converter.run();
		} else if ( args.length == 3 && args[2].startsWith("--default-time=")) {
			String dateString = args[2].split("=")[1];
			HTTrack2ArcConverter converter = new HTTrack2ArcConverter(args[0], args[1],
					dateFormatter.parse(dateString));
			converter.run();
		} else {
			System.err.println("Command: java HTTrack2ArcConverter <source_dir> <destination_dir> [--default-time=<default_time>]");
		}
	}

}
