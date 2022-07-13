package com.cleanroommc.multiblocked.client.shader.uniform;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.FloatBuffer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class UniformEntry<T> {

	public static final Predicate<UniformEntry> IS_INT = uniformEntry -> uniformEntry instanceof IntUniformEntry;
	public static final Predicate<UniformEntry> IS_FLOAT = uniformEntry -> uniformEntry instanceof FloatUniformEntry;
	public static final Predicate<UniformEntry> IS_MATRIX = uniformEntry -> uniformEntry instanceof MatrixUniformEntry;
	public static final Predicate<UniformEntry> IS_BOOLEAN = uniformEntry -> uniformEntry instanceof BooleanUniformEntry;

	public abstract boolean check(T other);

	public static class IntUniformEntry extends UniformEntry<int[]> {

		public static Function<int[], UniformEntry<int[]>> NEW = IntUniformEntry::new;

		private final int[] cache;

		public IntUniformEntry(int... cache) {
			this.cache = cache;
		}

		@Override
		public boolean check(int... other) {
			if (cache.length != other.length) {
				return false;
			}
			for (int i = 0; i < cache.length; i++) {
				if (cache[i] != other[i]) {
					return false;
				}
			}
			return true;
		}
	}

	public static class FloatUniformEntry extends UniformEntry<float[]> {

		public static Function<float[], UniformEntry<float[]>> NEW = FloatUniformEntry::new;

		private final float[] cache;

		public FloatUniformEntry(float... cache) {
			this.cache = cache;
		}

		@Override
		public boolean check(float... other) {
			if (cache.length != other.length) {
				return false;
			}
			for (int i = 0; i < cache.length; i++) {
				if (cache[i] != other[i]) {
					return false;
				}
			}
			return true;
		}
	}

	public static class MatrixUniformEntry extends UniformEntry<Pair<FloatBuffer, Boolean>> {

		public static Function<Pair<FloatBuffer, Boolean>, UniformEntry<Pair<FloatBuffer, Boolean>>> NEW = MatrixUniformEntry::new;

		FloatBuffer matrix;
		boolean transpose;

		public MatrixUniformEntry(Pair<FloatBuffer, Boolean> other) {
			this.matrix = other.getKey();
			this.transpose = other.getValue();
		}

		@Override
		public boolean check(Pair<FloatBuffer, Boolean> other) {
			return matrix.equals(other.getKey()) && transpose == other.getValue();
		}
	}

	public static class BooleanUniformEntry extends UniformEntry<Boolean> {

		public static Function<Boolean, UniformEntry<Boolean>> NEW = BooleanUniformEntry::new;

		private final boolean bool;

		public BooleanUniformEntry(boolean bool) {
			this.bool = bool;
		}

		@Override
		public boolean check(Boolean other) {
			return bool == other;
		}
	}
}
