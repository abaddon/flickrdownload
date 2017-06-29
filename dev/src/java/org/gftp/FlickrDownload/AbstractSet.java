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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gftp.FlickrDownload.Stats.MediaStats;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Exif;
import com.flickr4java.flickr.photos.GeoData;
import com.flickr4java.flickr.photos.Note;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.tags.Tag;

public abstract class AbstractSet {
	public static String SET_XML_FILENAME = "photos.xml";
	public static String SMALL_SQUARE_PHOTO_DESCRIPTION = "Small Square";
	public static String THUMBNAIL_PHOTO_DESCRIPTION = "Thumbnail";
	public static String SMALL_PHOTO_DESCRIPTION = "Small";
	public static String MEDIUM_PHOTO_DESCRIPTION = "Medium";
	public static String LARGE_PHOTO_DESCRIPTION = "Large";
	public static String ORIGINAL_MEDIA_DESCRIPTION = "Original";

	private Configuration configuration;
	private Collection<String> expectedFiles = new HashSet<String>(Arrays.asList(
			SET_XML_FILENAME, 
			Sets.SET_THUMBNAIL_FILENAME,
			Sets.SET_DETAIL_FILENAME));
	
	public AbstractSet (Configuration configuration) {
		this.configuration = configuration;
	}

	protected abstract int getMediaCount();
	protected abstract String getSetId();
	protected abstract String getSetTitle();
	protected abstract String getSetDescription();
	protected abstract String getPrimaryPhotoId();
	protected abstract String getPrimaryPhotoSmallSquareUrl();
	protected abstract void download(Flickr flickr, Element setXml) throws IOException, SAXException, FlickrException;

	public File getSetDirectory() {
		return new File(this.configuration.photosBaseDirectory, getSetTitle());
	}

	public File getSetXmlFilename() {
		return new File(getSetDirectory(), SET_XML_FILENAME);
	}

	private Element createStatsXml() throws JDOMException, IOException {
		Map<String, MediaStats> allStats = new HashMap<String, MediaStats>();
		Stats.processXmlFile(getSetXmlFilename(), allStats, getSetId(), NoopMediaIndexer.INSTANCE);
		return Stats.generateStatsXml(allStats);
	}

	public Element createToplevelXml() throws JDOMException, IOException {
		String setThumbnailBaseFilename = String.format("%s_thumb_sq.jpg", getPrimaryPhotoId());
		File setDir = new File(this.configuration.photosBaseDirectory, getSetId());
		return new Element("set")
				.addContent(new Element("id").setText(getSetId()))
				.addContent(new Element("title").setText(getSetTitle()))
				.addContent(new Element("description").setText(getSetDescription()))
				.addContent(createStatsXml());
	}

	protected void processPhoto(Photo photo, Flickr flickr, Element setXml) throws IOException, SAXException, FlickrException {
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
            	originalBaseFilename = String.format("%1$tY%1$tm%1$td_%1$tk%1$tM_%2$s_orig.%3$s",
            			photo.getDateTaken(), 
            			photo.getId(), 
            			IOUtils.getVideoExtension(originalUrl));
            }
            else {
            	try {
            		originalUrl = photo.getOriginalUrl();
            	}
            	catch (FlickrException e) {
            		// NOOP - original URL not available
            	}
        		originalBaseFilename = String.format("%1$tY%1$tm%1$td_%1$tk%1$tM_%2$s_orig.%3$s", 
        				photo.getDateTaken(),
        				photo.getId(), 
        				photo.getOriginalFormat());
            }

            this.expectedFiles.add(originalBaseFilename);


            XmlUtils.downloadMediaAndCreateElement("image",
            				new File(getSetDirectory(), originalBaseFilename), 
            				originalBaseFilename,
            				originalUrl,
            				false,
            				configuration);
            
	}

	public Element createSetlevelXml(Flickr flickr) throws IOException, SAXException, FlickrException {
		Logger.getLogger(getClass()).info(String.format("Downloading information for set %s - %s",
				getSetId(), getSetTitle()));

		Element setXml = new Element("set")
				.addContent(XmlUtils.createApplicationXml())
				.addContent(XmlUtils.createUserXml(this.configuration))
				.addContent(new Element("id").setText(getSetId()))
				.addContent(new Element("title").setText(getSetTitle()))
				.addContent(new Element("description").setText(getSetDescription()));

		download(flickr, setXml);
		
		return setXml;
	}

	private static String getOriginalVideoUrl(Flickr flickr, String photoId) throws IOException, FlickrException, SAXException {
		String origUrl = null;
		String hdUrl = null;
		String siteUrl = null;
		for (Size size : (Collection<Size>) flickr.getPhotosInterface().getSizes(photoId, true)) {
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
