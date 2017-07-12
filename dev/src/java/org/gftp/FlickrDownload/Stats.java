/*
  FlickrDownload - Copyright(C) 2011 Brian Masney <masneyb@onstation.org>.
  If you have any questions, comments, or suggestions about this program, please
  feel free to email them to me. You can always find out the latest news about
  FlickrDownload from my website at http://www.onstation.org/flickrdownload/

  FlickrDownload comes with ABSOLUTELY NO WARRANTY; for details, see the COPYING
  file. This is free software, and you are welcome to redistribute it under
  certain conditions; for details, see the COPYING file.
*/

package org.gftp.FlickrDownload;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;

import com.flickr4java.flickr.FlickrException;

public class Stats {
	private Sets sets;

	public Stats(Sets sets) {
		this.sets = sets;
	}


	public static String getPhotosUsername(File xmlFilename) throws IOException, JDOMException {
		if (!xmlFilename.exists())
			return null;

		SAXBuilder builder = new SAXBuilder();
		Reader in = null;
		try {
			in = new FileReader(xmlFilename);
			Document doc = builder.build(in);
			Element root = doc.getRootElement();

			Element userEle = root.getChild("user");
			if (userEle == null)
				return null;

			Element usernameEle = userEle.getChild("username");
			if (usernameEle == null)
				return null;
			
			return usernameEle.getText();
		}
		finally {
			in.close();
		}
	}

	public static String getAuthUsername(File xmlFilename) throws IOException, JDOMException {
		if (!xmlFilename.exists())
			return null;

		SAXBuilder builder = new SAXBuilder();
		Reader in = null;
		try {
			in = new FileReader(xmlFilename);
			Document doc = builder.build(in);
			Element root = doc.getRootElement();

			Element userEle = root.getChild("user");
			if (userEle == null)
				return null;

			Element usernameEle = userEle.getChild("authUser");
			if (usernameEle == null)
				return null;
			
			return usernameEle.getAttributeValue("username");
		}
		finally {
			in.close();
		}
	}


}
