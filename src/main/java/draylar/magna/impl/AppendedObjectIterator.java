package draylar.magna.impl;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSets;

import java.util.SortedSet;

public class AppendedObjectIterator<T> implements ObjectIterator<Long2ObjectMap.Entry<SortedSet<T>>> {
    private final ObjectIterator<Long2ObjectMap.Entry<SortedSet<T>>> originalIterator;
    private final Long2ObjectMap<T> extraInfos;
    
    public AppendedObjectIterator(ObjectIterator<Long2ObjectMap.Entry<SortedSet<T>>> originalIterator, Long2ObjectMap<T> extraInfos) {
        this.originalIterator = originalIterator;
        this.extraInfos = extraInfos;
    }
    
    @Override
    public boolean hasNext() {
        return originalIterator.hasNext() || !extraInfos.isEmpty();
    }
    
    @Override
    public Long2ObjectMap.Entry<SortedSet<T>> next() {
        if (originalIterator.hasNext()) {
            Long2ObjectMap.Entry<SortedSet<T>> entry = originalIterator.next();
            long key = entry.getLongKey();
            SortedSet<T> value = entry.getValue();
            T extraInfo = extraInfos.get(key);
            if (extraInfo != null) {
                extraInfos.remove(key);
                value.add(extraInfo);
                return of(key, value);
            }
            return entry;
        } else if (!extraInfos.isEmpty()) {
            Long2ObjectMap.Entry<T> extraInfoEntry = extraInfos.long2ObjectEntrySet().iterator().next();
            long key = extraInfoEntry.getLongKey();
            T infos = extraInfoEntry.getValue();
            extraInfos.remove(key);
            return of(key, ObjectSortedSets.singleton(infos));
        }
        throw new UnsupportedOperationException();
    }
    
    private Long2ObjectMap.Entry<SortedSet<T>> of(long key, SortedSet<T> value) {
        return new Long2ObjectMap.Entry<SortedSet<T>>() {
            @Override
            public SortedSet<T> getValue() {
                return value;
            }
            
            @Override
            public SortedSet<T> setValue(SortedSet<T> value) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public long getLongKey() {
                return key;
            }
        };
    }
}