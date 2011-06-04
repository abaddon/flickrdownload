package org.gftp.FlickrDownload;

import java.io.IOException;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.jdom.Element;

public interface MediaIndexer {
	void addToIndex(String setId, Element mediaElement);

	Collection<String> writeIndex() throws IOException, TransformerException;
}
