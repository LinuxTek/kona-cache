/*
 * Copyright (C) 2014 LinuxTek, Inc.  All Rights Reserved.
 */

package com.linuxtek.kona.cache;

import java.util.*;


public class KCacheObject<V> implements KCacheListener<V> {
	private V object = null;
	private int accessCount = 0;
	private Date dateAdded = null;
	private Date lastAccessed = null;
	private boolean isModified = false; //????

	public KCacheObject(V object) {
		this.object = object;
		dateAdded = new Date();
		lastAccessed = new Date();
		accessCount += 1;

		//cache.addCacheListener(this);	
	}

	public V getObject() {
		lastAccessed = new Date();
		accessCount += 1;
		return (object);
	}

	public int getAccessCount() {
		return (accessCount);
	}

	public Date getDateAdded() {
		return (dateAdded);
	}

	public Date getLastAccessed() {
		return (lastAccessed);
	}

	public boolean isModified() {
		return (isModified);
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
	}

	public void objectAdded(KCacheEvent<V> e) {
	}

	public void objectModified(KCacheEvent<V> e) {
	}

	public void objectRemoved(KCacheEvent<V> e) {
	}
}