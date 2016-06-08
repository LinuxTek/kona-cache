/*
 * Copyright (C) 2014 LinuxTek, Inc.  All Rights Reserved.
 */

package com.linuxtek.kona.cache;

import com.linuxtek.kona.util.KDateUtil;
import com.linuxtek.kona.util.KMap;

import java.util.*;

import org.apache.log4j.*;

public class KCache<K extends Comparable<? super K>,V>
{
    private static Logger logger = Logger.getLogger(KCache.class);

	private enum PurgePolicy { LRU, FIFO, TTL };

	public static final PurgePolicy LRU = PurgePolicy.LRU;
	public static final PurgePolicy FIFO = PurgePolicy.FIFO;
	public static final PurgePolicy TTL = PurgePolicy.TTL;

	private KMap<K,KCacheObject<V>> cache = null;
	protected List<KCacheListener<V>> cacheListeners = null;
	private int maxObjects = 10000;
	private PurgePolicy policy = LRU;
	private int ttl = 60; //default TTL is 1 minute
	private int minHitCount = 10; //min hits object must have to stay in cache

	public KCache() 
	{
		cache = new KMap<K,KCacheObject<V>>();
		cacheListeners = new ArrayList<KCacheListener<V>>();

		TimerTask purgeTask = new TimerTask() {
			public void run() {
				purge(false);
			}
		};
		Timer timer = new Timer();
		timer.schedule(purgeTask, 1000, 1000);
	}

	public KCache(PurgePolicy policy, int i)
	{
		this();
		this.policy = policy;
		if (policy == TTL)
			ttl = i;
		else if (policy == LRU)
			minHitCount = i;
		else
			maxObjects = i;
	}

	public KCache(int maxObjects)
	{
		this();
		this.maxObjects = maxObjects;
	}

	public PurgePolicy getPolicy()
	{
		return (policy);
	}

	public void setPolicy(PurgePolicy policy)
	{
		this.policy = policy;
	}

	public int getMinHitCount()
	{
		return (minHitCount);
	}

	public void setMinHitCount(int minHitCount)
	{
		this.minHitCount = minHitCount;
	}

	/** ttl in seconds */
	public int getTTL()
	{
		return (ttl);
	}

	/** ttl in seconds */
	public void setTTL(int ttl)
	{
		this.ttl = ttl;
	}

	protected KMap<K,KCacheObject<V>> getCache() {
		return (cache);
	}

	public Iterator<KCacheObject<V>> iterator()
	{
		return (cache.iterator());
	}
    
	public Iterator<K> keys() {
		return cache.keys();
	}
    
	public Set<K> keySet() {
		return cache.keySet();
	}

	public KCacheObject<V> getCacheObject(K key)
	{
		KCacheObject<V> co = cache.get(key);
		return (co);
	}

	public V get(K key)
	{
		KCacheObject<V> co = getCacheObject(key);
		if (co != null) {
			//logger.debug("found object in cache: " + key);
			return (co.getObject());
		} else {
			logger.debug("object not found in cache: " + key);
			return null;
		}
	}
    
	public synchronized void put(K key, V object) {
        insert(key, object);
	}
    
	public synchronized void insert(K key, V object)
	{
		if (cache.size() >= maxObjects)
			purge(true);

		KCacheObject<V> o = new KCacheObject<V>(object);
		cache.put(key, o);
		addCacheListener(o);
        
		processCacheEvent(new KCacheEvent<V>(o, KCacheEvent.Added));
		logger.debug("inserted cache object: " + key+"  count: " +getCount());
	}


	public synchronized KCacheObject<V> remove(K key)
	{
		KCacheObject<V> o = cache.remove(key);

		if (o != null)
			processCacheEvent(new KCacheEvent<V>(o, KCacheEvent.Removed));

		logger.debug("removed cache object: " + key + "  count: " +getCount());
		return (o);
	}

    
	// very simplistic purge for now ...
	public synchronized void purge(boolean quickCheck)
	{
		int padding = (int) .1 * maxObjects;
		int cacheSize = cache.size() + padding;

		if (cacheSize < maxObjects)
			return;
		
		logger.debug("purge called():  cache size: " + 
						cache.size() + "    maxObjects: " + maxObjects);

		if (policy == FIFO)
		{
			logger.debug("performing FIFO check ... ");
			KCacheDateComparator<V> c = new KCacheDateComparator<V>();
			Iterator<K> it = cache.keysSortedByValue(c);
			while (it.hasNext())
			{
				if (cacheSize > maxObjects)
				{
					K key = it.next();
					KCacheObject<V> co = cache.get(key);
					it.remove();
					processCacheEvent(new KCacheEvent<V>(co, KCacheEvent.Removed));
					logger.debug("FIFO purge: removed: " + key);
					if (quickCheck) continue;
				}
			}
		}
		else
		{
			Date now = new Date();
			Iterator<K> it = cache.keys();
			while (it.hasNext())
			{
				K key = it.next();
				KCacheObject<V> co = cache.get(key);
				if (co.isModified())
				{
					it.remove();
					processCacheEvent(new KCacheEvent<V>(co, KCacheEvent.Removed));
					if (quickCheck) continue;
				}
		
				if (policy == LRU)
				{
					logger.debug("performing LRU check ... ");
					if (co.getAccessCount() < minHitCount)
					{
						it.remove();
						processCacheEvent(new KCacheEvent<V>(co, KCacheEvent.Removed));
						logger.debug("LRU purge: removed: " + key);
						if (quickCheck) continue;
					}
				}
	
				if (policy == TTL)
				{
					logger.debug("performing TTL check ... ");
					if (KDateUtil.diffSecs(now, co.getDateAdded()) > ttl)
					{
						it.remove();
						processCacheEvent(new KCacheEvent<V>(co, KCacheEvent.Removed));
						logger.debug("TTL purge: removed: " + key);
						if (quickCheck) continue;
					}
				}
			}
		}
	}

	/** max number of items in cache */
	public int getMaxObjects()
	{
		return (maxObjects);
	}

	/** current number of items in cache */
	public int getCount()
	{
		return (cache.size());
	}

	public boolean isEmpty()
	{
		if (getCount() == 0)
			return (true);

		return (false);
	}

	public synchronized void clear()
	{
		cache.clear();
	}

	public void addCacheListener(KCacheListener<V> l) 
	{
		cacheListeners.add(l);
	}
 
	public void removeCacheListener(KCacheListener<V> l) 
	{
		cacheListeners.remove(l);
	}
 
//	protected void processCacheEvent(KCacheEvent.Action action)
//	{
//		processCacheEvent(new KCacheEvent(this, action));
//	}

	protected void processCacheEvent(KCacheEvent<V> e) 
	{
		synchronized (cacheListeners) 
		{
			for (KCacheListener<V> listener : cacheListeners)
			{
				if (e.getAction() == KCacheEvent.Added)
					listener.objectAdded(e);
				else if (e.getAction() == KCacheEvent.Modified)
					listener.objectModified(e);
				else if (e.getAction() == KCacheEvent.Removed)
					listener.objectRemoved(e);
			}
		}
	}
}

	/*---------------------
	int maxCost ()
	int totalCost ()
	void setMaxCost ( int m )
	type * take ( const QString & k )
	type * find ( const QString & k, bool ref = TRUE )
	void statistics ()

	bool autoDelete ()
	void setAutoDelete ( bool enable )
	---------------*/
