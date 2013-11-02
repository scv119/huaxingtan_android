package cn.huaxingtan.model;

import java.io.Serializable;
import java.util.Map;

public class AudioItem implements Serializable{

	public static enum Status {
		STOPED, QUEUED, STARTED, FINISHED, 
	}
	
	private static final long serialVersionUID = 3110886420315494447L;
	
	
	private String name;
	private String detail;
	private String serialName;
	private String serialNO;
	private int serialId;
	private String author;
	private String coverUrl;
	private String fileUrl;
	private long fileSize;
	private int duration;
	private boolean hasLyric;
	private long fileId;
	private long finishedSize;
	private String path;
	private Status status;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getSerialName() {
		return serialName;
	}
	public void setSerialName(String serialName) {
		this.serialName = serialName;
	}
	public String getSerialNO() {
		return serialNO;
	}
	public void setSerialNO(String serialNO) {
		this.serialNO = serialNO;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCoverUrl() {
		return coverUrl;
	}
	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public boolean isHasLyric() {
		return hasLyric;
	}
	public void setHasLyric(boolean hasLyric) {
		this.hasLyric = hasLyric;
	}
	public long getFileId() {
		return fileId;
	}
	public void setFileId(long fileId) {
		this.fileId = fileId;
	}
	public long getFinishedSize() {
		return finishedSize;
	}
	public void setFinishedSize(long finishedSize) {
		this.finishedSize = finishedSize;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	

	public int getSerialId() {
		return serialId;
	}
	public void setSerialId(int serialId) {
		this.serialId = serialId;
	}
	@SuppressWarnings("rawtypes")
	public static AudioItem loadJson(Object o) {
		assert(o instanceof Map);
		Map map = (Map)o;
		AudioItem item = new AudioItem();
		item.name = (String) map.get("name");
		item.author = (String) map.get("author");
		item.serialName = (String) map.get("serialName");
		item.serialNO = (String) map.get("serialNO");
		item.fileUrl = (String) map.get("fileUrl");
		double tmp = (Double) map.get("fileSize");
		item.fileSize = (long) tmp;
		item.hasLyric = "yes".equals((String)map.get("hasLyric")) ? true : false;
		tmp = (Double) map.get("duration");
		item.duration = (int)tmp;
		tmp = (Double) map.get("id");
		item.fileId = (long) tmp;
		tmp = (Double) map.get("serialId");
		item.serialId = (int) tmp;
		item.status = Status.STOPED;
		return item;
	}
	
	
}
