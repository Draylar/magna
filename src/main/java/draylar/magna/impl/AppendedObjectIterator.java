package draylar.magna.impl;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Collection;

public class AppendedObjectIterator<T extends Collection<?>> implements ObjectIterator<Long2ObjectMap.Entry<T>> {
    private final ObjectIterator<Long2ObjectMap.Entry<T>> originalIterator;
    private final Long2ObjectMap<T> extraInfos;
    
    public AppendedObjectIterator(ObjectIterator<Long2ObjectMap.Entry<T>> originalIterator, Long2ObjectMap<T> extraInfos) {
        this.originalIterator = originalIterator;
        this.extraInfos = extraInfos;
    }
    
    @Override
    public boolean hasNext() {
        return originalIterator.hasNext() || !extraInfos.isEmpty();
    }
    
    @Override
    public Long2ObjectMap.Entry<T> next() {
        if (originalIterator.hasNext()) {
            Long2ObjectMap.Entry<T> entry = originalIterator.next();
            long key = entry.getLongKey();
            T extraInfo = extraInfos.get(key);
            if (extraInfo != null) {
                extraInfos.remove(key);
                ((Collection<Object>) extraInfo).addAll(entry.getValue());
                return of(key, extraInfo);
            }
            return entry;
        } else if (!extraInfos.isEmpty()) {
            Long2ObjectMap.Entry<T> extraInfoEntry = extraInfos.long2ObjectEntrySet().iterator().next();
            long key = extraInfoEntry.getLongKey();
            T infos = extraInfoEntry.getValue();
            extraInfos.remove(key);
            return of(key, infos);
        }
        throw new UnsupportedOperationException();
    }
    
    private Long2ObjectMap.Entry<T> of(long key, T value) {
        return new Long2ObjectMap.Entry<T>() {
            @Override
            public T getValue() {
                return value;
            }
            
            @Override
            public T setValue(T value) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public long getLongKey() {
                return key;
            }
        };
    }
}