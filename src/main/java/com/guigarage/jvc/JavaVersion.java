package com.guigarage.jvc;

public class JavaVersion {

	private String version;
	
	private String name;
	
	private String path;
	
	public JavaVersion(String version, String name, String path) {
		this.version = version;
		this.name = name;
		this.path = path;
	}

	public String getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}
}
