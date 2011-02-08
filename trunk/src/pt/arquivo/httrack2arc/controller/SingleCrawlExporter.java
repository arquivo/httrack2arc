package pt.arquivo.httrack2arc.controller;

//SingleCrawlExporter - Process a single crawl directory and export it to ARC files.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import pt.arquivo.httrack2arc.model.LogEntry;

public class SingleCrawlExporter implements Exporter {

	private LogReader logReader;
	private ArcWriter arcWriter;

	private Collection<LogEntry> newLogMetadata;
	private Collection<LogEntry> oldLogMetadata;

	private File source;
	private File destination;
	
	private File oldCache;
	private File newCache;

	public SingleCrawlExporter() {
		logReader = new LogReader();
	}

	//TODO - implement the default date mechanism (see RecursiveCrawlExport)
	public void setup (File source, File destination, Date defaultDate) {
		this.source = source;
		this.destination = destination;

		arcWriter = new ArcWriter();
		arcWriter.setup( this.destination );
	}

	public void process () {
		File[] childrens = source.listFiles();

		for ( File f : childrens ) {

			oldCache = null;
			newCache = null;
			
			System.out.println( f.getName() );
			if ( f.getName().equals("hts-cache") ) {
				System.out.println( "FOUND" );
				File[] cacheChildrens = f.listFiles();

				for ( File f1 : cacheChildrens ) {
					if ( f1.getName().equals("new.zip") )
						newCache = f1;
					else if ( f1.getName().equals("new.txt") )
						newLogMetadata = processLog(f1);
					else if ( f1.getName().equals("old.zip") )
						oldCache = f1;
					else if ( f1.getName().equals("old.txt") )
						oldLogMetadata = processLog(f1);
				}
				
				if ( oldCache != null)
					processOldCache( oldCache );
				if ( newCache != null )
					processNewCache( newCache );
			}
		}
	}

	/**
	 * 
	 */
	public void finalize() {
		arcWriter.close();
		
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	private Collection<LogEntry> processLog( File f) {
		Collection<LogEntry> coll = null;

		try {
			logReader.setup( f );
			coll = logReader.parse();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return coll;
	}

	private void processNewCache( File f ) {
		processCache( f, newLogMetadata );
	}
	
	private void processOldCache( File f ) {
		processCache( f, oldLogMetadata );
	}
	
	private boolean duplicated( LogEntry e, Collection<LogEntry> metadata ) {
		boolean b = false;
		
		if ( this.oldLogMetadata == metadata )
			b = false;
		else {
			if ( oldLogMetadata != null && oldLogMetadata.contains( e ) )
				b = true;
		}
		return b;
	}
	
	private void processCache( File f, Collection<LogEntry> metadata) {

		try {
			FileInputStream fis = new FileInputStream( f );
			ZipInputStream zis = new ZipInputStream( fis );
			ZipEntry entry;

			while(( entry = zis.getNextEntry()) != null){
				
				for (LogEntry e : newLogMetadata) {
					
					if ( e.getUrl().equals(entry.getName()) ) { //TODO - remove/use : && !duplicated(e, metadata) ) {
						
						if ( duplicated(e, metadata))
							System.err.println("DUPLICATED: "+ e.getUrl() );
						
						if ( e.getCrawlDate() == null )
							e.setCrawlDate( new Date(entry.getTime()) );
						
						if( arcWriter.createNewRecord(e) ) {
							
							byte[] data = new byte[2048];
							int count;
							boolean empty = true;
							while ( (count = zis.read( data )) != -1 ) {
								empty = false;
								arcWriter.write(data, 0, count);
							}
							
							if (empty) {
								
								File f_local = null;
								
								if ( e.getLocal().startsWith("/") )
									f_local = new File( e.getLocal() );
								else {
									f_local = new File( source.getAbsolutePath() +"/"+ e.getLocal() );
								}
								
								BufferedInputStream input = new BufferedInputStream( new FileInputStream(f_local) );
								while ( (count = input.read(data) ) != -1 ) {
									empty = false;
									arcWriter.write(data, 0, count);
								}
								input.close();
							}

							zis.closeEntry();
							arcWriter.closeRecord();
						}
						
						break;
					}
				}
			}

			zis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
