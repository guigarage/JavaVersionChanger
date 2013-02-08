package com.guigarage.jvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hendrikebbers
 * @see http 
 *      ://stackoverflow.com/questions/135688/setting-environment-variables-in
 *      -os-x
 */
public class LaunchdConfig {

	private static final String LAUNCHD_PATH = "/etc/launchd.conf";

	private static final String VERSION_BLOCK_START = "# Java Version Changer start";

	private static final String VERSION_BLOCK_END = "# Java Version Changer end";

	private String rootPwd;

	public LaunchdConfig(String rootPwd) {
		this.rootPwd = rootPwd;
	}

	private void changePermissionToWriteable() throws IOException,
			InterruptedException {
		if (!new File(LAUNCHD_PATH).canWrite()) {
			Runtime runtime = Runtime.getRuntime();
			Process chmodProcess = runtime.exec("sudo chmod 777 "
					+ LAUNCHD_PATH);
			OutputStream chmodProcessOutputStream = chmodProcess
					.getOutputStream();
			OutputStreamWriter chmodProcessWriter = new OutputStreamWriter(
					chmodProcessOutputStream);
			try {
				chmodProcessWriter.write(rootPwd);
			} finally {
				chmodProcessWriter.close();
			}
			chmodProcess.waitFor();
			InputStream chmodProcessInputStream = chmodProcess.getErrorStream();
			InputStreamReader chmodProcessReader = new InputStreamReader(
					chmodProcessInputStream);
			BufferedReader chmodProcessBufferedReader = new BufferedReader(
					chmodProcessReader);
			try {
				while (chmodProcessBufferedReader.ready()) {
					// TODO: ERROR
					System.out.println(chmodProcessBufferedReader.readLine());
				}
			} finally {
				chmodProcessBufferedReader.close();
			}
		}
	}

	private void createLaunchdIfNotExists() throws IOException,
			InterruptedException {
		File launchdFile = new File(LAUNCHD_PATH);
		if (!launchdFile.exists()) {
			Runtime runtime = Runtime.getRuntime();
			Process touchProcess = runtime.exec("sudo touch " + LAUNCHD_PATH);
			OutputStream touchProcessOutputStream = touchProcess
					.getOutputStream();
			OutputStreamWriter touchProcessWriter = new OutputStreamWriter(
					touchProcessOutputStream);
			try {
				touchProcessWriter.write(rootPwd);
			} finally {
				touchProcessWriter.close();
			}
			touchProcess.waitFor();
			InputStream touchProcessInputStream = touchProcess.getErrorStream();
			InputStreamReader touchProcessReader = new InputStreamReader(
					touchProcessInputStream);
			BufferedReader touchProcessBufferedReader = new BufferedReader(
					touchProcessReader);
			try {
				while (touchProcessBufferedReader.ready()) {
					// TODO: ERROR
					System.out.println(touchProcessBufferedReader.readLine());
				}
			} finally {
				touchProcessBufferedReader.close();
			}
			changePermissionToWriteable();

			FileWriter writer = new FileWriter(launchdFile);
			try {
				writer.write("# Set environment variables here so they are available globally to all apps"
						+ System.getProperty("line.separator"));
				writer.write("# (and Terminal), including those launched via Spotlight."
						+ System.getProperty("line.separator"));
				writer.write("#" + System.getProperty("line.separator"));
				writer.write("# After editing this file run the following command from the terminal to update"
						+ System.getProperty("line.separator"));
				writer.write("# environment variables globally without needing to reboot."
						+ System.getProperty("line.separator"));
				writer.write("# NOTE: You will still need to restart the relevant application (including"
						+ System.getProperty("line.separator"));
				writer.write("# Terminal) to pick up the changes!"
						+ System.getProperty("line.separator"));
				writer.write("# grep -E \"^setenv\" /etc/launchd.conf | xargs -t -L 1 launchctl"
						+ System.getProperty("line.separator"));
				writer.flush();
			} finally {
				writer.close();
			}
		}
	}

	private void insertJavaVersion(JavaVersion version) throws IOException,
			InterruptedException {
		changePermissionToWriteable();

		List<String> lines = readCompleteLaunchdFile();
		lines.add(VERSION_BLOCK_START);
		lines.add("setenv JAVA_HOME " + version.getPath());
		lines.add(VERSION_BLOCK_END);
		writeCompleteLaunchdFile(lines);
	}

	private void writeCompleteLaunchdFile(List<String> lines)
			throws IOException {
		FileWriter writer = new FileWriter(new File(LAUNCHD_PATH));
		try {
			for (String line : lines) {
				writer.write(line + System.getProperty("line.separator"));
			}
		} finally {
			writer.close();
		}
	}

	private List<String> readCompleteLaunchdFile() throws IOException,
			InterruptedException {
		changePermissionToWriteable();
		List<String> lines = new ArrayList<String>();
		File launchdFile = new File(LAUNCHD_PATH);
		FileReader reader = new FileReader(launchdFile);
		BufferedReader bufferedReader = new BufferedReader(reader);
		try {
			while (bufferedReader.ready()) {
				lines.add(bufferedReader.readLine());
			}
		} finally {
			bufferedReader.close();
		}
		return lines;
	}

	private void removeCurrentJavaVersion() throws IOException,
			InterruptedException {
		changePermissionToWriteable();
		List<String> lines = readCompleteLaunchdFile();

		int startLine = -1;
		int endLine = -1;
		
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).equals(VERSION_BLOCK_START)) {
				startLine = i;
				break;
			}
		}

		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).equals(VERSION_BLOCK_END)) {
				endLine = i;
				break;
			}
		}
		if (startLine >= 0 && endLine > 0 && endLine > startLine) {
			for (int i = startLine; i <= endLine; i++) {
				lines.remove(startLine);
			}
		}
		writeCompleteLaunchdFile(lines);
	}

	private void updateGlobalEnv() throws IOException, InterruptedException {
		Runtime runtime = Runtime.getRuntime();
		System.out.println("grep -E \"^setenv\" " + LAUNCHD_PATH
				+ " | xargs -t -L 1 launchctl");
		Process updateProcess = runtime.exec(new String[] {
				"/bin/sh",
				"-c",
				"grep -E \"^setenv\" " + LAUNCHD_PATH
						+ " | xargs -t -L 1 launchctl" });
		updateProcess.waitFor();
		InputStream updateProcessInputStream = updateProcess.getErrorStream();
		InputStreamReader updateProcessReader = new InputStreamReader(
				updateProcessInputStream);
		BufferedReader updateProcessBufferedReader = new BufferedReader(
				updateProcessReader);
		try {
			while (updateProcessBufferedReader.ready()) {
				// TODO: ERROR
				System.out.println(updateProcessBufferedReader.readLine());
			}
		} finally {
			updateProcessBufferedReader.close();
		}
	}

	public void setJavaVersion(JavaVersion version) throws IOException,
			InterruptedException {
		createLaunchdIfNotExists();

		removeCurrentJavaVersion();
		insertJavaVersion(version);

		updateGlobalEnv();
	}

	public static void main(String[] args) throws Exception {
		new LaunchdConfig("XXXX").setJavaVersion(new JavaVersionFetcher()
				.fetch().get(2));
	}

}
