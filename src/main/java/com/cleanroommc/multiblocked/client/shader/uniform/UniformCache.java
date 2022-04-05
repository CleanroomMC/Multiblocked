package com.cleanroommc.multiblocked.client.shader.uniform;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

public class UniformCache {

	private final Int2ObjectMap<UniformEntry> entryCache = new Int2ObjectOpenHashMap<>();
	private final Object2IntMap<String> locationCache = new Object2IntOpenHashMap<>();
	private final int programId;

	public UniformCache(int programId) {
		this.programId = programId;
	}

	public void invalidate() {
		entryCache.clear();
		locationCache.clear();
	}

	public void glUniform1F(String location, float v0) {
		glUniformF(location, (loc) -> GL20.glUniform1f(loc, v0), v0);
	}

	public void glUniform2F(String location, float v0, float v1) {
		glUniformF(location, (loc) -> GL20.glUniform2f(loc, v0, v1), v0, v1);
	}

	public void glUniform3F(String location, float v0, float v1, float v2) {
		glUniformF(location, (loc) -> GL20.glUniform3f(loc, v0, v1, v2), v0, v1, v2);
	}

	public void glUniform4F(String location, float v0, float v1, float v2, float v3) {
		glUniformF(location, (loc) -> GL20.glUniform4f(loc, v0, v1, v2, v3), v0, v1, v2, v3);
	}

	private void glUniformF(String location, IntConsumer callback, float... values) {
		glUniform(location, UniformEntry.IS_FLOAT, UniformEntry.FloatUniformEntry.NEW, callback, values);
	}

	public void glUniform1I(String location, int v0) {
		glUniformI(location, (loc) -> GL20.glUniform1i(loc, v0), v0);
	}

	public void glUniform2I(String location, int v0, int v1) {
		glUniformI(location, (loc) -> GL20.glUniform2i(loc, v0, v1), v0, v1);
	}

	public void glUniform3I(String location, int v0, int v1, int v2) {
		glUniformI(location, (loc) -> GL20.glUniform3i(loc, v0, v1, v2), v0, v1, v2);
	}

	public void glUniform4I(String location, int v0, int v1, int v2, int v3) {
		glUniformI(location, (loc) -> GL20.glUniform4i(loc, v0, v1, v2, v3), v0, v1, v2, v3);
	}

	private void glUniformI(String location, IntConsumer callback, int... values) {
		glUniform(location, UniformEntry.IS_INT, UniformEntry.IntUniformEntry.NEW, callback, values);
	}

	public void glUniformMatrix2(String location, boolean transpose, FloatBuffer matrix) {
		glUniformMatrix(location, (loc) -> GL20.glUniformMatrix2(loc, transpose, matrix), transpose, matrix);
	}

	public void glUniformMatrix4(String location, boolean transpose, FloatBuffer matrix) {
		glUniformMatrix(location, (loc) -> GL20.glUniformMatrix4(loc, transpose, matrix), transpose, matrix);
	}

	public void glUniformMatrix(String location, IntConsumer callback, boolean transpose, FloatBuffer matrix) {
		glUniform(location, UniformEntry.IS_MATRIX, UniformEntry.MatrixUniformEntry.NEW, callback, ImmutablePair.of(matrix, transpose));
	}

	public void glUniformBoolean(String location, boolean value) {
		glUniform(location, UniformEntry.IS_BOOLEAN, UniformEntry.BooleanUniformEntry.NEW, (loc) -> GL20.glUniform1i(loc, value ? 1 : 0), value);
	}

	private int getUniformLocation(String name) {
		int uniformLocation;
		if (locationCache.containsKey(name)) {
			uniformLocation = locationCache.get(name);
		} else {
			uniformLocation = GL20.glGetUniformLocation(programId, name);
			locationCache.put(name, uniformLocation);
		}
		return uniformLocation;
	}

	private <T> void glUniform(String location, Predicate<UniformEntry> isType, Function<T, UniformEntry<T>> createUniform, IntConsumer applyCallback, T value) {
		int loc = getUniformLocation(location);
		boolean update = true;
		if (entryCache.containsKey(loc)) {
			UniformEntry uniformEntry = entryCache.get(loc);
			if (isType.test(uniformEntry)) {
				update = !uniformEntry.check(value);
			}
		}
		if (update) {
			UniformEntry<T> entry = createUniform.apply(value);
			applyCallback.accept(loc);
			entryCache.put(loc, entry);
		}
	}

}
