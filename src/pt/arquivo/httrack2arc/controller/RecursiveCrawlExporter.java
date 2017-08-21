package pt.arquivo.httrack2arc.controller;

//RecurseCrawlExporter - Recursively traverse the Httrack crawl directories
//and export them to ARC file.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import pt.arquivo.httrack2arc.model.LogEntry;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;

public class RecursiveCrawlExporter implements Exporter {

	private final long earliestDate;
	private final long oldestDate;
	
	private LogReader logReader;
	private ArcWriter arcWriter;

	private Collection<LogEntry> newLogMetadata;
	private Collection<LogEntry> oldLogMetadata;

	private File source;
	private File destination;
	private Date defaultDate;

	private File oldCache;
	private File newCache;

	private File currentDir;

	public RecursiveCrawlExporter() {
		logReader = new LogReader();
		oldestDate = new GregorianCalendar(1990, 1, 1).getTimeInMillis();
		earliestDate = new GregorianCalendar(2030, 1, 1).getTimeInMillis();
	}

	public void setup (java.io.File source, java.io.File destination, Date defaultDate) {
		
		this.source = new File(source);
		this.destination = new File(destination);
		this.defaultDate = defaultDate;

		arcWriter = new ArcWriter();
		arcWriter.setup( this.destination );
	}

	public void traverse (File path) {

		//System.out.println( path.getAbsolutePath() );

		if (path.isDirectory()) {

			File[] childs = (File[])path.listFiles();
			for (File child : childs) {
				if (child.isDirectory() ) { 
					if (child.getName().equals("hts-cache") ) {
						System.out.println("PROCESSING: "+ path.getAbsolutePath() );

						currentDir = path;
						process( child );
					} else {
						traverse (child);
					}
				}
			}
		}
	}

	public void process () {
		traverse( source );
	}

	public void process (File cacheDir) {			

		oldCache = null;
		newCache = null;

		File[] cacheChildrens = (File[])cacheDir.listFiles();

		for ( File f1 : cacheChildrens ) {
			if ( f1.getName().equals("new.zip") )
				newCache = f1;
			else if ( f1.getName().equals("new.txt") ) {
				System.out.println("NEW LOG: "+ f1);
				newLogMetadata = processLog(f1);
			}
			else if ( f1.getName().equals("old.zip") )
				oldCache = f1;
			else if ( f1.getName().equals("old.txt") ) {
				System.out.println("OLD LOG: "+ f1);
				oldLogMetadata = processLog(f1);
			}
		}

		if ( oldCache != null)
			processOldCache( oldCache );
		if ( newCache != null )
			processNewCache( newCache );
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
		System.out.println("NEW CACHE: "+ f);

		processCache( f, newLogMetadata );
	}

	private void processOldCache( File f ) {
		System.out.println("OLD CACHE: "+ f);

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

		File cache = f;

		try {

			if (cache.isDirectory()) {

				File[] childs = (File[])cache.listFiles();
				for (File child : childs) {

					boolean found = false;
					
					// Some retrieved docs can be exposed as directory on the
					// zip archive file.
					// So, we process files and dirs alike
					
					// Extract the URL from the file path
					String filename = child.getAbsolutePath();
					boolean http = false;
					int pos = filename.indexOf("http:/");
					if ( pos != -1) {
						filename = filename.substring(pos);
						http = true;
					}
					
					
					if ( !http ) {
						pos = filename.substring("https:/");
						if ( pos != -1) {
							filename = filename.substring(pos);
						}
					}	
					String cacheEntry = filename.replaceFirst("/", "//");

					for (LogEntry logEntry : metadata) {

						if ( cacheEntry.equals( logEntry.getUrl() ) ) {
							
							found = true;
							
							//TODO - check duplicated

							if ( logEntry.getCrawlDate() == null ) {
								Date childDate = new Date(child.lastModified());
								logEntry.setCrawlDate( childDate );
							}
							
							/*** Ensure that the date is valid    ***/
							/*** Else, reset it to a default date ***/
							Date childDate = logEntry.getCrawlDate();
							if (childDate.getTime() < oldestDate || childDate.getTime() > earliestDate) {
								childDate = new Date( new java.io.File(logEntry.getLocal()).lastModified() );
								
								if (childDate.getTime() < oldestDate || childDate.getTime() > earliestDate) {
									childDate = defaultDate;
								}
								System.err.println("DATE CORRECTION - old: "+ logEntry.getCrawlDate() +"\tnew: "+ childDate );
								logEntry.setCrawlDate(childDate);
							}		

							if( arcWriter.createNewRecord( logEntry ) ) {
								System.out.println("WRITING: "+ logEntry.getUrl() );

								byte[] data = new byte[2048];
								int count;
								boolean empty = true;

								FileInputStream in = null; 
								try {
									in = new FileInputStream(child);
									while ( (count = in.read( data )) != -1 ) {
										empty = false;
										arcWriter.write(data, 0, count);
									}
								} catch (Exception e1 ) {
									System.err.println("FILE NOT FOUND: "+ child);
								} finally {
									try {
										in.close();
									} catch (Exception e) {}
								}
								
								// If the file is empty
								// Get it from the local file-system
								if (empty) {

									File f_local = null;

									if ( logEntry.getLocal().startsWith("/") )
										f_local = new File( logEntry.getLocal() );
									else {
										f_local = new File( currentDir.getAbsolutePath() +"/"+ logEntry.getLocal() );
									}
									
									try {
									java.io.BufferedInputStream input = new java.io.BufferedInputStream( new java.io.FileInputStream(f_local) );
									while ( (count = input.read(data) ) != -1 ) {
										empty = false;
										arcWriter.write(data, 0, count);
									}
									input.close();
									} catch (FileNotFoundException fnfe) {
										fnfe.getMessage();
									}
								}
								arcWriter.closeRecord();
							}
							break;
						}
					}
					
					if ( !found ) {
						if ( child.isDirectory() && !cacheEntry.endsWith("/") ) {
							processSingleFile(child, cacheEntry.concat("/"), metadata, arcWriter);							
						} else {
							System.out.println("NOT WRITTEN: "+ cacheEntry );
							System.out.println();
						}
					}

					// If 'child' is a dir
					// Continue traversing the dir(s)
					if (child.isDirectory() ) {
						processCache( child, metadata);

					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processSingleFile(File f, String currentUrl, Collection<LogEntry> metadata, ArcWriter writer) {

		for (LogEntry logEntry : metadata) {

			try {
				if ( currentUrl.equals( logEntry.getUrl() ) ) {
					//TODO - check duplicated

					if ( logEntry.getCrawlDate() == null ) {
						Date childDate = new Date(f.lastModified());
						logEntry.setCrawlDate( childDate );
					}
					
					/*** Ensure that the date is valid    ***/
					/*** Else, reset it to a default date ***/
					Date childDate = logEntry.getCrawlDate();
					if (childDate.getTime() < oldestDate || childDate.getTime() > earliestDate) {
						childDate = new Date( new java.io.File(logEntry.getLocal()).lastModified() );
						
						if (childDate.getTime() < oldestDate || childDate.getTime() > earliestDate) {
							childDate = defaultDate;
						}
						System.err.println("DATE CORRECTION - old: "+ logEntry.getCrawlDate() +"\tnew: "+ childDate );
						logEntry.setCrawlDate(childDate);
					}

					if( arcWriter.createNewRecord( logEntry ) ) {
						System.out.println("WRITING RETRY: "+ logEntry.getUrl() );

						byte[] data = new byte[2048];
						int count;
						boolean empty = true;

						FileInputStream in = null; 
						try {
							in = new FileInputStream(f);
							while ( (count = in.read( data )) != -1 ) {
								empty = false;
								arcWriter.write(data, 0, count);
							}
						} catch (Exception e1 ) {
							System.err.println("FILE NOT FOUND: "+ f);
						} finally {
							try {
								in.close();
							} catch (Exception e) {}
						}

						// If the file is empty
						// Get it from the local file-system
						if (empty) {

							File f_local = null;

							if ( logEntry.getLocal().startsWith("/") )
								f_local = new File( logEntry.getLocal() );
							else {
								f_local = new File( currentDir.getAbsolutePath() +"/"+ logEntry.getLocal() );
							}

							java.io.BufferedInputStream input = new java.io.BufferedInputStream( new java.io.FileInputStream(f_local) );
							while ( (count = input.read(data) ) != -1 ) {
								empty = false;
								arcWriter.write(data, 0, count);
							}
							input.close();
						}
						arcWriter.closeRecord();
					}
				} 
			} catch(FileNotFoundException e) {
				e.getMessage();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				break;
			}
		}
	}
}
