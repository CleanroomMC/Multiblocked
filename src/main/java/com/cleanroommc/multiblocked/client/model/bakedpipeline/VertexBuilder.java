package com.cleanroommc.multiblocked.client.model.bakedpipeline;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class VertexBuilder implements IVertexConsumer {
    public final VertexFormat vertexFormat;
    public final TextureAtlasSprite sprite;
    public int quadTint = -1;
    public EnumFacing quadOrientation;
    public boolean applyDiffuseLighting;
    public final ListMultimap<VertexFormatElement.EnumUsage, float[]> data = MultimapBuilder.enumKeys(VertexFormatElement.EnumUsage.class).arrayListValues().build();

    public VertexBuilder(VertexFormat vertexFormat, TextureAtlasSprite sprite){
        this.vertexFormat = vertexFormat;
        this.sprite = sprite;
    }

    @Override
    public void put(int element, @Nullable float... data) {
        if (data == null) return;
        float[] copy = new float[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        VertexFormatElement ele = vertexFormat.getElement(element);
        this.data.put(ele.getUsage(), copy);
    }

    public Vector3f[] verts() {
        return fromData(data.get(VertexFormatElement.EnumUsage.POSITION), 3);
    }

    public Vector2f[] uvs() {
        return fromData(data.get(VertexFormatElement.EnumUsage.UV), 2);
    }

    @SuppressWarnings("unchecked")
    private <T extends Vector> T[] fromData(List<float[]> data, int size) {
        Vector[] ret = size == 2 ? new Vector2f[data.size()] : new Vector3f[data.size()];
        for (int i = 0; i < data.size(); i++) {
            ret[i] = size == 2 ? new Vector2f(data.get(i)[0], data.get(i)[1]) : new Vector3f(data.get(i)[0], data.get(i)[1], data.get(i)[2]);
        }
        return (T[]) ret;
    }

    @Override
    @Nonnull
    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }

    @Override
    public void setQuadTint(int tint) {
        quadTint = tint;
    }

    @Override
    public void setQuadOrientation(@Nonnull EnumFacing orientation) {
        this.quadOrientation = orientation;
    }

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {
        this.applyDiffuseLighting = diffuse;
    }

    //@Override //soft override, only exists in new forge versions
    public void setTexture(@Nullable TextureAtlasSprite texture) {}
}
