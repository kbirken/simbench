package org.nanosite.common.util.file;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ResourceExtractor {
	AbstractUIPlugin plugin = null;

	public ResourceExtractor (AbstractUIPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean extractResource(String filename, String sourcePath, String targetPath) {
		URL url = FileLocator.find(plugin.getBundle(), new Path(sourcePath + "/" + filename), null);
		if (url==null) {
			return false;
		}
		InputStream in;
		FileOutputStream os;
		try {
			in = url.openStream();
			os = new FileOutputStream(targetPath + "/" + filename);
			byte[] buffer = new byte[4096]; // to hold file contents
			int bytes_read; // how many bytes in buffer
			while ((bytes_read = in.read(buffer)) != -1) {
				// read until EOF
				os.write(buffer, 0, bytes_read); // write
			}
			in.close();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
