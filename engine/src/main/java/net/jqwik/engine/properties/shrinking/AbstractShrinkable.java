package net.jqwik.engine.properties.shrinking;

import java.util.*;

import net.jqwik.api.*;

public abstract class AbstractShrinkable<T> implements Shrinkable<T> {

	private final T value;

	public AbstractShrinkable(T value) {
		this.value = value;
	}

	@Override
	public T value() {
		return value;
	}

	@Override
	public ShrinkingSequence<T> shrink(Falsifier<T> falsifier) {
		return new DeepSearchShrinkingSequence<>(this, this::shrinkCandidatesFor, falsifier);
	}

	@Override
	public List<Shrinkable<T>> shrinkingSuggestions() {
		ArrayList<Shrinkable<T>> shrinkables = new ArrayList<>(shrinkCandidatesFor(this));
		shrinkables.sort(null);
		return shrinkables;
	}

	public abstract Set<Shrinkable<T>> shrinkCandidatesFor(Shrinkable<T> shrinkable);

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AbstractShrinkable<?> that = (AbstractShrinkable<?>) o;

		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return String.format("%s<%s>(%s:%s)", //
			getClass().getSimpleName(), //
			value().getClass().getSimpleName(), //
			value(), distance());
	}
}
