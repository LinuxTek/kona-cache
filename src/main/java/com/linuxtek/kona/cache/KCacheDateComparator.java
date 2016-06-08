/*
 * Copyright (C) 2014 LinuxTek, Inc.  All Rights Reserved.
 */

package com.linuxtek.kona.cache;

import java.util.*;


public class KCacheDateComparator<V> implements Comparator<KCacheObject<V>>
{
	private enum DateType { DateAdded, LastAccessed };

	public static final DateType DateAdded = DateType.DateAdded;
	public static final DateType LastAccessed = DateType.LastAccessed;

	private DateType dateType = null;

	public KCacheDateComparator()
	{
		this.dateType = DateAdded;
	}

	public KCacheDateComparator(DateType dateType)
	{
		this.dateType = dateType;
	}
		
	public int compare(KCacheObject<V> co1, KCacheObject<V> co2)
	{
		//KCacheObject co1 = (KCacheObject) o1;
		//KCacheObject co2 = (KCacheObject) o2;

		if (dateType == DateAdded)
		{
			long t1 = co1.getDateAdded().getTime();
			long t2 = co2.getDateAdded().getTime();
			int c = (int)(t1 - t2);
			return (c);
		}
		else
		{
			long t1 = co1.getLastAccessed().getTime();
			long t2 = co2.getLastAccessed().getTime();
			int c = (int)(t1 - t2);
			return (c);
		}
	}
}
