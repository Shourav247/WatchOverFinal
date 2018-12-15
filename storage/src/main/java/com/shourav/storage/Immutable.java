package com.shourav.storage;

import java.io.Serializable;

/**
 * @param <T>
 * @param <S>
 */
public class Immutable<T, S> implements Serializable {
	private static final long serialVersionUID = 40;

	public final T element1;
	public final S element2;

	public Immutable() {
		element1 = null;
		element2 = null;
	}

	public Immutable(T element1, S element2) {
		this.element1 = element1;
		this.element2 = element2;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Immutable == false) {
			return false;
		}

		Object object1 = ((Immutable<?, ?>) object).element1;
		Object object2 = ((Immutable<?, ?>) object).element2;

		return element1.equals(object1) && element2.equals(object2);
	}

	@Override
	public int hashCode() {
		return element1.hashCode() << 16 + element2.hashCode();
	}
}
