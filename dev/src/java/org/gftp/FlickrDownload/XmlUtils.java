/*
  FlickrDownload - Copyright(C) 2010 Brian Masney <masneyb@onstation.org>.
  If you have any questions, comments, or suggestions about this program, please
  feel free to email them to me. You can always find out the latest news about
  FlickrDownload from my website at http://www.onstation.org/flickrdownload/

  FlickrDownload comes with ABSOLUTELY NO WARRANTY; for details, see the COPYING
  file. This is free software, and you are welcome to redistribute it under
  certain conditions; for details, see the COPYING file.
*/

package org.gftp.FlickrDownload;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.flickr4java.flickr.photos.Photo;

public class XmlUtils {
	public static String RAW_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
	public static DateFormat rawDateFormatter = new SimpleDateFormat(RAW_DATE_FORMAT);
	public static List<String> photoListDownloaded = new ArrayList<String>();

	
	public static void downloadMedia(File localFilename, String remoteUrl, Photo photo) throws IOException {
		if(photoListDownloaded.contains(localFilename.getName())) {
			Logger.getLogger(XmlUtils.class).info(String.format("Skip file %s, already present in another set ", localFilename.getName()));
		}else {
			photoListDownloaded.add(localFilename.getName());
			if (remoteUrl == null || localFilename.exists()){
				Logger.getLogger(XmlUtils.class).info(String.format("Skip file %s, already exist", localFilename.getName()));
			}else{
				IOUtils.downloadUrl(remoteUrl, localFilename, photo);
			}	
		}
	}

}
