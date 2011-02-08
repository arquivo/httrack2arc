package pt.arquivo.httrack2arc.model;

//LogEntry - Collect info about archived pages for further log.
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

import java.util.Date;

public class LogEntry {
	
	private String url;
	private String local;
	private Date crawlDate;
	private String etag;
	private String mime;
	private int statusCode;
	//TODO - we need to catch charset's info
	//private String charset;
	
	public LogEntry () {}
	
	public LogEntry (String url,
			String local,
			Date crawlDate,
			String etag,
			String mime,
			int statusCode) 
	{
		this.url = url;
		this.local = local;
		this.crawlDate = crawlDate;
		this.etag = etag;
		this.mime = mime;
		this.statusCode = statusCode;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLocal() {
		return local;
	}
	public void setLocal(String local) {
		this.local = local;
	}
	public Date getCrawlDate() {
		return crawlDate;
	}
	public void setCrawlDate(Date crawlDate) {
		this.crawlDate = crawlDate;
	}
	public void setEtag(String etag) {
		this.etag = etag;
	}
	public String getEtag() {
		return etag;
	}
	public String getMime() {
		return mime;
	}
	public void setMime(String mime) {
		this.mime = mime;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("=====\n");
		builder.append("URL: ");
		builder.append(url);
		builder.append("\nLocal: ");
		builder.append(local);
		builder.append("\nStatus Code: ");
		builder.append(statusCode);
		builder.append("\nCrawl Date: ");
		builder.append(crawlDate);
		builder.append("\nEtag: ");
		builder.append(etag);
		builder.append("\nMIME: ");
		builder.append(mime);
		builder.append("\n=====\n\n");
		
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean b = false;

		LogEntry entry = null;
		try {
			entry = (LogEntry)obj;

			if (this.url.equals( entry.getUrl()) ) {
					if (  this.crawlDate == null && entry.getCrawlDate() == null) {
						b = true;
					} else if ( this.crawlDate != null 
							&& entry.getCrawlDate() != null
							&& this.crawlDate.equals( entry.getCrawlDate() )) 
					{
						b = true;
					}	
			}
		} catch (Exception e) {
			System.err.println(this.toString());
			System.err.println(entry.toString());
		}

		return b;
	}

}
