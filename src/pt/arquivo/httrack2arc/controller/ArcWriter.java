package pt.arquivo.httrack2arc.controller;

//ArcWriter - Responsable for writing the collected info to ARC files.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCWriter;

import pt.arquivo.httrack2arc.model.LogEntry;
import pt.arquivo.httrack2arc.util.HttpStatusCodes;

public class ArcWriter {

	public static final String DEFAULT_BASENAME = "BASENAME";
	public static final String DEFAULT_PREFIX = "PREFIX";
	public static final String DEFAULT_IP = "1.1.1.1";
	public static final long DEFAULT_ARC_SIZE = ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE;
	
	private ARCWriter writer;
	
	private LogEntry metadata;
	private int lenRecord;
	private ByteArrayOutputStream baos;
	private StringBuilder headerBuilder;
	
	private boolean written;
	
	public void setup (File destination) {
		setup (destination, DEFAULT_BASENAME, DEFAULT_PREFIX, DEFAULT_ARC_SIZE);
	}
	
	public void setup (File destination, long maxSize) {
		setup (destination, DEFAULT_BASENAME, DEFAULT_PREFIX, maxSize);
	}
	
	public void setup (File destination, String basename, String prefix) {
		setup (destination, basename, prefix, DEFAULT_ARC_SIZE);
	}
	
	public void setup (File destination, String basename, String prefix, long maxSize) {
		File[] targetPath = new File[] { destination };
		
		if (writer != null)
			this.close();
		
		writer = new ARCWriter( new AtomicInteger(),     			
				Arrays.asList(targetPath),
				basename +"-"+ prefix,
				true,
				maxSize
		);
	}
	
	public void close () {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean createNewRecord( LogEntry entry ) throws IOException {
		//TODO - handle unsupported with a specific exception
		boolean created = false;
		metadata = entry;		
		
		if ( metadata.getStatusCode() >= 200  && metadata.getStatusCode() <= 207) {
		
		headerBuilder = new StringBuilder();
			
		headerBuilder.append("HTTP/1.1 ");
		headerBuilder.append( entry.getStatusCode() );
		headerBuilder.append(" ");
		headerBuilder.append( HttpStatusCodes.getDescription( entry.getStatusCode() ) );
		headerBuilder.append("\r\n");
		headerBuilder.append("Content-Type: ");
		headerBuilder.append( entry.getMime() );
		headerBuilder.append("\r\n");
		
		headerBuilder.append("\r\n");
		
		created = true;
		written = false;
		}
		
		return created;
	}
	
	public void write(byte[] data) throws IOException {
		write( data, 0, data.length );
	}
	
	public void write(byte[] data, int offset, int length) throws IOException {
		if (!written){
			baos = new ByteArrayOutputStream();
			baos.write( headerBuilder.toString().getBytes());
			
			lenRecord = headerBuilder.toString().getBytes().length;
			
			written = true;
		}
		
		lenRecord += length;
		baos.write( data, offset, length );
	}
	
	@SuppressWarnings(value={"deprecation"})
	public void closeRecord() throws IOException {
		if (lenRecord > 0) {
			
		writer.write( metadata.getUrl(), 
				metadata.getMime(), 
				DEFAULT_IP, 
				metadata.getCrawlDate().getTime(), 
				lenRecord, baos );
		}
		
		baos.close();
		metadata = null;
		lenRecord = 0;
	}
}
