package com.guigarage.jvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hendrikebbers
 * @see http
 *      ://blog.hgomez.net/blog/2012/07/20/understanding-java-from-command-line
 *      -on-osx/
 * 
 */
public class JavaVersionFetcher {

	public List<JavaVersion> fetch() throws IOException, InterruptedException {
		List<JavaVersion> versions = new ArrayList<JavaVersion>();

		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("/usr/libexec/java_home -V");
		process.waitFor();
		InputStream inputStream = process.getErrorStream();
		InputStreamReader reader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(reader);
		try {
			if (bufferedReader.ready()) {
				// First Line with Title
				bufferedReader.readLine();
			}
			while (bufferedReader.ready()) {
				String versionString = bufferedReader.readLine();
				versionString = versionString.trim();
				String[] splitted = versionString.split("	");
				if (splitted.length == 3) {

					if (splitted[0].endsWith(":")) {
						splitted[0] = splitted[0].substring(0,
								splitted[0].length() - 1);
					}
					versions.add(new JavaVersion(splitted[0], splitted[1],
							splitted[2]));
				}
			}
		} finally {
			bufferedReader.close();
		}
		return versions;
	}

	public static void main(String[] args) throws Exception {
		new JavaVersionFetcher().fetch();
	}

}
