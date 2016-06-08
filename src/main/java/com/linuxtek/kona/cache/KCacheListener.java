/*
 * Copyright (C) 2014 LinuxTek, Inc.  All Rights Reserved.
 */

package com.linuxtek.kona.cache;

import java.util.EventListener;

public interface KCacheListener<V> extends EventListener {
	public void objectAdded(KCacheEvent<V> e);
	public void objectModified(KCacheEvent<V> e);
	public void objectRemoved(KCacheEvent<V> e);
}
