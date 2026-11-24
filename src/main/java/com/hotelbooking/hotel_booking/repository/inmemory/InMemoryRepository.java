package com.hotelbooking.hotel_booking.repository.inmemory;

import com.hotelbooking.hotel_booking.repository.BaseRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class InMemoryRepository<T> implements BaseRepository<T> {

    private final Map<String, T> store = new ConcurrentHashMap<>();
    private final Function<T, String> idOf;

    protected InMemoryRepository(Function<T, String> idOf) {
        this.idOf = idOf;
    }

    @Override
    public T save(T aggregate) {
        store.put(idOf.apply(aggregate), aggregate);
        return aggregate;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<T> findAll() {
        return List.copyOf(store.values());
    }

    protected List<T> findWhere(Predicate<T> condition) {
        return store.values().stream()
                .filter(condition)
                .toList();
    }
}
