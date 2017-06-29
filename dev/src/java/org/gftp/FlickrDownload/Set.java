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

import java.io.IOException;

import org.apache.log4j.Logger;

import org.jdom.Element;
import org.xml.sax.SAXException;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photosets.Photoset;

public class Set extends AbstractSet {
	private Photoset set;

	public Set(Configuration configuration, Photoset set) {
		super(configuration);
		this.set = set;
	}

	@Override
	protected void download(Flickr flickr) throws IOException, SAXException, FlickrException {
		int pageNum = 1;
		int retrievedPhotos = 0;
		int totalPhotos = 0;
		do {
			PhotoList photos = flickr.getPhotosetsInterface().getPhotos(getSetId(), 500, pageNum++);

			totalPhotos = photos.getTotal();

			for (int i = 0; i < photos.size(); i++) {
				retrievedPhotos++;
				Photo photo = (Photo) photos.get(i);
                Logger.getLogger(Set.class).info("Processing photo " + retrievedPhotos + " of " + totalPhotos + ": " + photo.getUrl());
				processPhoto(photo, flickr);
			}
		} while (retrievedPhotos < totalPhotos);		
	}

	@Override
	protected String getPrimaryPhotoId() {
		return this.set.getPrimaryPhoto().getId();
	}

	@Override
	protected String getPrimaryPhotoSmallSquareUrl() {
		return this.set.getPrimaryPhoto().getSmallSquareUrl();
	}

	@Override
	protected String getSetDescription() {
		return this.set.getDescription();
	}

	@Override
	protected String getSetId() {
		return this.set.getId();
	}

	@Override
	protected String getSetTitle() {
		return this.set.getTitle();
	}

	@Override
	protected int getMediaCount() {
		return this.set.getPhotoCount() + this.set.getVideoCount();
	}
}
