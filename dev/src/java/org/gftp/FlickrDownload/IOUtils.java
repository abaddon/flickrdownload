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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.log4j.Logger;

import com.flickr4java.flickr.photos.Photo;
import com.google.common.io.ByteStreams;


public class IOUtils {
	

	public static String md5Sum(File file) {
		InputStream istr = null;
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			istr = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			int n;
			while ((n = istr.read(buffer)) != -1) {
				digest.update(buffer, 0, n);
			}
			istr.close();
			return new BigInteger(1, digest.digest()).toString(16);
		}
		catch (Exception e) {
			Logger.getLogger(IOUtils.class).error(String.format("Could not get md5sum of %s: %s", file, e.getMessage()), e);
			return "";
		}
	}
	

	public static void copyToFileAndCloseStreams(InputStream data, File destFile) throws IOException {
		OutputStream output =null;
		try {
			output = new FileOutputStream(destFile);
 			try {
 		        ByteStreams.copy(data, output);
 		    } finally {
 		    }
		}
		finally {
			if (output != null)
				output.close();
			if (data != null)
				data.close();
		}
	}

	@SuppressWarnings("deprecation")
	public static void downloadUrl(String url, File destFile, Photo photo) throws IOException, HTTPException {
		File tmpFile = new File(destFile.getAbsoluteFile() + ".tmp");
		Logger.getLogger(IOUtils.class).debug(String.format("Downloading URL %s to %s", url, tmpFile));

		tmpFile.getParentFile().mkdirs();

        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        get.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        int code = client.executeMethod(get);
        if (code >= 200 && code < 300) {
        	InputStream data = get.getResponseBodyAsStream();
        	
        		copyToFileAndCloseStreams(get.getResponseBodyAsStream(), tmpFile);
//        		try {
//        			IImageMetadata metadata = Sanselan.getMetadata(tmpFile);
//        			TiffImageMetadata exif = metadata.getExif();
//        	        TiffOutputSet outputSet = exif.getOutputSet();
//        	        
//        			 
//        			final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
//        			
//        			DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
//        			
//        			TiffOutputField exifDateTime = new TiffOutputField(
//        					ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, 
//        					FieldType.FIELD_TYPE_ASCII, 
//        					formatter.format(photo.getDateTaken().getTime()).length(), 
//        					formatter.format(photo.getDateTaken().getTime()).getBytes()
//        					);
//        			exifDirectory.add(exifDateTime);
//        			
//        		}catch(Exception ex) {
//        			Logger.getLogger(IOUtils.class).fatal(ex.getMessage());
//        		}
            tmpFile.renameTo(destFile);

        }
        else
        	Logger.getLogger(IOUtils.class).fatal("Got HTTP response code " + code + " when trying to download " + url);
	}

	private static String getRemoteFilename(String url) throws IOException, HTTPException {
        HttpClient client = new HttpClient();
        HeadMethod get = new HeadMethod(url);
        get.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        int code = client.executeMethod(get);
        
        if (code >= 200 && code < 400) {
            Header disposition = get.getResponseHeader("Content-Disposition");
            if (disposition != null)
            	return disposition.getValue().replace("attachment; filename=", "");
        }
       	Logger.getLogger(IOUtils.class).fatal("Got HTTP response code " + code + " when trying to download " + url + ". Returning null.");
        return null;
	}

	public static String getVideoExtension(String url) throws IOException, HTTPException {
		String filename = getRemoteFilename(url);
		if (filename == null || filename.endsWith("."))
			return "mp4"; // FIXME
		return filename.substring(filename.lastIndexOf(".") + 1);
	}
}
