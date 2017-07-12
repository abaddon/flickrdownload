/*
  FlickrDownload - Copyright(C) 2010-2011 Brian Masney <masneyb@onstation.org>.
  If you have any questions, comments, or suggestions about this program, please
  feel free to email them to me. You can always find out the latest news about
  FlickrDownload from my website at http://www.onstation.org/flickrdownload/

  FlickrDownload comes with ABSOLUTELY NO WARRANTY; for details, see the COPYING
  file. This is free software, and you are welcome to redistribute it under
  certain conditions; for details, see the COPYING file.
 */

package org.gftp.FlickrDownload;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;

public class FlickrDownload {
	
	public static String TOPLEVEL_XML_FILENAME = "flickr.xml";

	protected static class Arguments {
		@Option(name="--authDir", required=false)
		public String authDirectory;

		@Option(name="--authUsername", required=false)
		public String authUsername;

		@Option(name="--photosUsername", required=false)
		public String photosUsername; // FIXME - don't make this mandatory

		@Option(name="--photosDir", required=true)
		public String photosDirectory;

		@Option(name="--partial", required=false)
		public boolean partial = false;

		@Option(name="--addExtensionToUnknownFiles", required=false)
		public String addExtensionToUnknownFiles;

		@Option(name="--limitToSet", required=false, multiValued=true)
		public List<String> limitDownloadsToSets = new ArrayList<String>();		

		@Option(name="--debug", required=false)
		public boolean debug = false;
		
		@Option(name="--downloadExifData", required=false)
		public boolean downloadExifData = false;

		@Option(name="--onlyData", required=false)
		public boolean onlyData = false;

		@Option(name="--onlyOriginals", required=false)
		public boolean onlyOriginals = false;
	}

	public static String getApplicationName() {
		return StringUtils.defaultString(FlickrDownload.class.getPackage().getImplementationTitle(), "FlickrDownload");
	}

	public static String getApplicationVersion() {
		return StringUtils.defaultString(FlickrDownload.class.getPackage().getImplementationVersion(), "?");
	}
	
	private static File getToplevelXmlFilename(File photosBaseDirectory) {
				return new File(photosBaseDirectory, TOPLEVEL_XML_FILENAME);
			}

	public static String getApplicationWebsite() {
		return "http://www.onstation.org/flickrdownload/";
	}

	private static void usage(CmdLineParser parser, String error) {
		System.err.println(error);
		System.err.print("usage: FlickrDownload ");
		parser.printSingleLineUsage(System.err);
		System.err.println();
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		System.out.println(String.format("%s %s - Copyright(C) 2007,2010-2011 Brian Masney <masneyb@onstation.org>.", 
				getApplicationName(), getApplicationVersion()));
		System.out.println("If you have any questions, comments, or suggestions about this program, please");
		System.out.println("feel free to email them to me. You can always find out the latest news about");
		System.out.println(String.format("%s from my website at %s.", getApplicationName(), getApplicationWebsite()));
		System.out.println();
		System.out.println(String.format("%s is distributed under the terms of the GPLv3 and comes with", getApplicationName()));
		System.out.println("ABSOLUTELY NO WARRANTY; for details, see the COPYING file. This is free");
		System.out.println("software, and you are welcome to redistribute it under certain conditions;");
		System.out.println("for details, see COPYING file.");
		System.out.println();

		Arguments values = new Arguments();
		CmdLineParser parser = new CmdLineParser(values);

		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			usage(parser, e.getMessage());
		}

		File toplevelXmlFilename = getToplevelXmlFilename(new File(values.photosDirectory));
		if (StringUtils.isBlank(values.authUsername))
			values.authUsername = Stats.getAuthUsername(toplevelXmlFilename);
		if (StringUtils.isBlank(values.photosUsername))
			values.photosUsername = Stats.getPhotosUsername(toplevelXmlFilename);

		if (values.authUsername == null)
			usage(parser, "--authUsername must be specified");

		Collection<String> createdToplevelFiles = new HashSet<String>();

		Flickr flickr = Authentication.getFlickr();
		Configuration configuration = new Configuration(
				flickr,
				new File(values.photosDirectory), 
					StringUtils.isBlank(values.authDirectory) ? null : new File(values.authDirectory), 
				values.authUsername);

		createdToplevelFiles.add(configuration.authUser.getId() + ".auth");

		if (StringUtils.isBlank(values.photosUsername))
			configuration.photosUser = configuration.authUser;
		else {
			PeopleInterface pi = flickr.getPeopleInterface();
			User pu = pi.findByUsername(values.photosUsername);
			if (pu == null)
				throw new IllegalArgumentException("Cannot find user with ID " + values.photosUsername);
			configuration.photosUser = pi.getInfo(pu.getId());
		} 

        configuration.onlyData = values.onlyData;
        configuration.onlyOriginals = values.onlyOriginals;


		configuration.downloadExifData = values.downloadExifData;
		configuration.partialDownloads = values.partial;
		configuration.addExtensionToUnknownFiles = values.addExtensionToUnknownFiles;
        configuration.limitDownloadsToSets = values.limitDownloadsToSets;

		if (values.debug) {
			Flickr.debugRequest = true;
			Flickr.debugStream = true;
		}

		Sets sets = new Sets(configuration, flickr);

		// The photos must be downloaded before the toplevel XML files are created
		sets.downloadAllPhotos();

	}
}
