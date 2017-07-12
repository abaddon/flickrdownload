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
import java.io.IOException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.Size;

public abstract class AbstractSet {
	public static String SET_XML_FILENAME = "photos.xml";
	public static String SMALL_SQUARE_PHOTO_DESCRIPTION = "Small Square";
	public static String THUMBNAIL_PHOTO_DESCRIPTION = "Thumbnail";
	public static String SMALL_PHOTO_DESCRIPTION = "Small";
	public static String MEDIUM_PHOTO_DESCRIPTION = "Medium";
	public static String LARGE_PHOTO_DESCRIPTION = "Large";
	public static String ORIGINAL_MEDIA_DESCRIPTION = "Original";

	private Configuration configuration;
	
	public AbstractSet (Configuration configuration) {
		this.configuration = configuration;
	}

	protected abstract int getMediaCount();
	protected abstract String getSetId();
	protected abstract String getSetTitle();
	protected abstract String getSetDescription();
	protected abstract String getPrimaryPhotoId();
	protected abstract String getPrimaryPhotoSmallSquareUrl();
	protected abstract void download(Flickr flickr) throws IOException, SAXException, FlickrException;

	public File getSetDirectory() {
		return new File(this.configuration.photosBaseDirectory, getSetTitle());
	}

	protected void processPhoto(Photo photo, Flickr flickr) throws IOException, SAXException, FlickrException {
            // We probably have some of the photo data from a search
            // result, but probably not all, so fetch it all.
            photo = flickr.getPhotosInterface().getPhoto(photo.getId());

            String originalUrl = null;
            String originalBaseFilename;
            if (photo.getMedia().equals("video")) {
	            	originalUrl = getOriginalVideoUrl(flickr, photo.getId());
	            	if(originalUrl == null){
	            		Logger.getLogger(getClass()).warn(String.format("Missing video, Flicker ID: %s",photo.getId()));
	            		return;
	            	}
	            	originalBaseFilename = String.format("%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS_%2$s_orig.%3$s",
	            			photo.getDateTaken(), 
	            			photo.getId(), 
	            			IOUtils.getVideoExtension(originalUrl));
            }else {
	            	try {
	            		originalUrl = photo.getOriginalUrl();
	            	}
	            	catch (FlickrException e) {
	            		// NOOP - original URL not available
	            	}
	        		originalBaseFilename = String.format("%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS_%2$s_orig.%3$s", 
	        				photo.getDateTaken(),
	        				photo.getId(), 
	        				photo.getOriginalFormat());
            }

            XmlUtils.downloadMedia(
            				new File(getSetDirectory(), originalBaseFilename), 
            				originalUrl,
            				photo);
            
	}

	public void createSetlevel(Flickr flickr) throws IOException, SAXException, FlickrException {
		Logger.getLogger(getClass()).info(String.format("Downloading information for set %s - %s", getSetId(), getSetTitle()));

		download(flickr);
		
	}

	private static String getOriginalVideoUrl(Flickr flickr, String photoId) throws IOException, FlickrException, SAXException {
		String origUrl = null;
		String hdUrl = null;
		String siteUrl = null;
		for (Size size : flickr.getPhotosInterface().getSizes(photoId, true)) {
			if (size.getSource().contains("/play/orig"))
				origUrl = size.getSource();
			else if (size.getSource().contains("/play/hd"))
				hdUrl = size.getSource();
			else if (size.getSource().contains("/play/site"))
				siteUrl = size.getSource();
		}
		if (origUrl != null)
			return origUrl;
		else if (hdUrl != null)
			return hdUrl;
		else if (siteUrl != null)
			return siteUrl;
		else
			return null;
	}
}
