package com.rokhmanov.aws;

public class LogRecord {
	private String filesystem;
	private String size;
	private String used;
	private String available;
	private String usedPercent;
	private String mount;

	public LogRecord(){
		super();
	}
	
	public LogRecord(String filesystem, String size, String used, String available, String usedPercent, String mount) {
		super();
		this.filesystem = filesystem;
		this.size = size;
		this.used = used;
		this.available = available;
		this.usedPercent = usedPercent;
		this.mount = mount;
	}
	
	public String getFilesystem() {
		return filesystem;
	}
	public void setFilesystem(String filesystem) {
		this.filesystem = filesystem;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getUsed() {
		return used;
	}
	public void setUsed(String used) {
		this.used = used;
	}
	public String getAvailable() {
		return available;
	}
	public void setAvailable(String available) {
		this.available = available;
	}
	public String getUsedPercent() {
		return usedPercent;
	}
	public void setUsedPercent(String usedPercent) {
		this.usedPercent = usedPercent;
	}
	public String getMount() {
		return mount;
	}
	public void setMount(String mount) {
		this.mount = mount;
	}


}
