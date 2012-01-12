package pt.arquivo.httrack2arc.util;

//HttpStatusCode - Define and obtain information about the HTTP status code
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

public enum HttpStatusCodes {
	
	OK (200, "OK"),
	CREATED (201, "Created"),
	ACCEPTED (202, "Accepted"),
	NON_AUTHORITATIVE_INFORMATION (203, "Non-Authoritative Information"),
	NO_CONTENT (204, "No Content"),
	RESET_CONTENT (205, "Reset Content"),
	PARTIAL_CONTENT (206, "Partial Content"),
	MULTI_STATUS (207, "Multi-Status");
	
	private final int code;
	private final String description;
	
	public int getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}
	
	private HttpStatusCodes( int code, String description ) {
		this.code = code;
		this.description = description;
	}
	
	public static String getDescription( int code ) {
		String description = null;
		HttpStatusCodes[] values = values();
		
		for (int i = 0; i < values.length; i++) {
			if ( values[i].code == code ) {
				description = values[i].description;
			}
		}
		
		return description; 
	}
	
}
