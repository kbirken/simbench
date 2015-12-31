package org.nanosite.common.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {

	public static boolean ensureDir (String dirname) {
		File dir = new File(dirname);
		if (dir.exists())
			return true;

		// directory doesn't exist, create it
		return dir.mkdirs();
	}

	public static boolean copy (String sourcefile, String destfile) {
		FileChannel in = null, out = null;
		FileInputStream instream = null;
		FileOutputStream outstream = null;
		boolean ok = false;
		try {
			File infile = new File(sourcefile);
			File outfile = new File(destfile);

			instream = new FileInputStream(infile);
			outstream = new FileOutputStream(outfile);
			in = instream.getChannel();
			out = outstream.getChannel();

			long size = in.size();
			MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
			out.write(buf);
			ok = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
				instream.close();
				outstream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ok;
	}
}
