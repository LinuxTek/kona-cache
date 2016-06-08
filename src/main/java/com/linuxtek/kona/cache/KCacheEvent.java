/*
 * Copyright (C) 2014 LinuxTek, Inc.  All Rights Reserved.
 */

package com.linuxtek.kona.cache;

import java.util.EventObject;

public class KCacheEvent<V> extends EventObject {
	private static final long serialVersionUID = 1L;

	public enum Action { Added, Modified, Removed }

	public static final Action Added 	= Action.Added;
	public static final Action Modified = Action.Modified;
	public static final Action Removed 	= Action.Removed;

	private Action action = null;

	public KCacheEvent(KCacheObject<V> source, Action action) {
		super(source);
		this.action = action;
	}

	public Action getAction() {
		return (action);
	}
}
