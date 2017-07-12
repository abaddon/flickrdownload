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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

	public static void downloadUrl(String url, File destFile, Photo photo) throws IOException, HTTPException {
		File tmpFile = new File(destFile.getAbsoluteFile() + ".tmp");
		Logger.getLogger(IOUtils.class).debug(String.format("Downloading URL %s to %s", url, tmpFile));

		tmpFile.getParentFile().mkdirs();

        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        get.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        int code = client.executeMethod(get);
        if (code >= 200 && code < 300) {
        		copyToFileAndCloseStreams(get.getResponseBodyAsStream(), tmpFile);
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
