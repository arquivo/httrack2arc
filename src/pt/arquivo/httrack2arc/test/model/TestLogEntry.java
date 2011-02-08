package pt.arquivo.httrack2arc.test.model;

//TestLogEntry - Tests adding elements to the LogEntry collection.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import pt.arquivo.httrack2arc.model.LogEntry;

public class TestLogEntry {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Date d = new Date(System.currentTimeMillis());
		
		LogEntry e1 = new LogEntry();
		e1.setUrl("http://teste.com/1");
		e1.setCrawlDate( d );
		
		LogEntry e2 = new LogEntry();
		e2.setUrl("http://teste.com/1");
		e2.setCrawlDate( d );
		
		System.out.println( e1.equals(e2));
		
		Collection<LogEntry> coll = new ArrayList<LogEntry>();
		coll.add(e1);
		
		System.out.println( coll.contains(e2) );
	}

}
