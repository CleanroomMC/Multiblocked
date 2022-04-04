package com.cleanroommc.multiblocked.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Vector3 {
    public static final Vector3 X = new Vector3(1, 0, 0);
    public static final Vector3 Y = new Vector3(0, 1, 0);
    public static final Vector3 Z = new Vector3(0, 0, 1);

    public double x;
    public double y;
    public double z;

    public Vector3(double d, double d1, double d2) {
        this.x = d;
        this.y = d1;
        this.z = d2;
    }

    public Vector3(Vector3 vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public Vector3(Vec3d vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public Vector3(Vec3i vec) {
        this.x = vec.getX();
        this.y = vec.getY();
        this.z = vec.getZ();
    }

    public Vector3(Tuple3f vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public static Vector3 fromNBT(NBTTagCompound tag) {
        return new Vector3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    }

    public Vec3d vec3() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public BlockPos pos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public Vector3 rotate(double angle, Vector3 axis) {
        Quat.aroundAxis(axis.copy().normalize(), angle).rotate(this);
        return this;
    }

    public double angle(Vector3 vec) {
        return Math.acos(this.copy().normalize().dotProduct(vec.copy().normalize()));
    }

    public double dotProduct(Vector3 vec) {
        double d = vec.x * this.x + vec.y * this.y + vec.z * this.z;
        if (d > 1.0D && d < 1.00001D) {
            d = 1.0D;
        } else if (d < -1.0D && d > -1.00001D) {
            d = -1.0D;
        }

        return d;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setDouble("x", this.x);
        tag.setDouble("y", this.y);
        tag.setDouble("z", this.z);
        return tag;
    }

    @SideOnly(Side.CLIENT)
    public Vector3f vector3f() {
        return new Vector3f((float)this.x, (float)this.y, (float)this.z);
    }

    @SideOnly(Side.CLIENT)
    public Vector4f vector4f() {
        return new Vector4f((float)this.x, (float)this.y, (float)this.z, 1.0F);
    }

    @SideOnly(Side.CLIENT)
    public void glVertex() {
        GlStateManager.glVertex3f((float)this.x, (float)this.y, (float)this.z);
    }

    public Vector3 set(double x1, double y1, double z1) {
        this.x = x1;
        this.y = y1;
        this.z = z1;
        return this;
    }

    public Vector3 set(Vec3i vec) {
        return this.set((double)vec.getX(), (double)vec.getY(), (double)vec.getZ());
    }

    public Vector3 add(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
        return this;
    }

    public Vector3 add(double d) {
        return this.add(d, d, d);
    }

    public Vector3 add(Vector3 vec) {
        return this.add(vec.x, vec.y, vec.z);
    }

    public Vector3 add(BlockPos pos) {
        return this.add((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
    }

    public Vector3 subtract(double dx, double dy, double dz) {
        this.x -= dx;
        this.y -= dy;
        this.z -= dz;
        return this;
    }

    public Vector3 subtract(double d) {
        return this.subtract(d, d, d);
    }

    public Vector3 subtract(Vector3 vec) {
        return this.subtract(vec.x, vec.y, vec.z);
    }

    public Vector3 subtract(BlockPos pos) {
        return this.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
    }

    public Vector3 multiply(double fx, double fy, double fz) {
        this.x *= fx;
        this.y *= fy;
        this.z *= fz;
        return this;
    }

    public Vector3 multiply(double f) {
        return this.multiply(f, f, f);
    }

    public Vector3 multiply(Vector3 f) {
        return this.multiply(f.x, f.y, f.z);
    }

    public Vector3 divide(double fx, double fy, double fz) {
        this.x /= fx;
        this.y /= fy;
        this.z /= fz;
        return this;
    }

    public Vector3 divide(double f) {
        return this.divide(f, f, f);
    }

    public Vector3 divide(Vector3 vec) {
        return this.divide(vec.x, vec.y, vec.z);
    }

    public Vector3 divide(BlockPos pos) {
        return this.divide((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
    }

    public Vector3 floor() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        return this;
    }

    public Vector3 ceil() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        this.z = Math.ceil(this.z);
        return this;
    }

    public double mag() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double magSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vector3 xCrossProduct() {
        double d = this.z;
        double d1 = -this.y;
        this.x = 0.0D;
        this.y = d;
        this.z = d1;
        return this;
    }

    public Vector3 zCrossProduct() {
        double d = this.y;
        double d1 = -this.x;
        this.x = d;
        this.y = d1;
        this.z = 0.0D;
        return this;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Vector3)) {
            return false;
        } else {
            Vector3 v = (Vector3)o;
            return this.x == v.x && this.y == v.y && this.z == v.z;
        }
    }

    public boolean equalsT(Vector3 v) {
        return between(this.x - 1.0E-5D, v.x, this.x + 1.0E-5D) && between(this.y - 1.0E-5D, v.y, this.y + 1.0E-5D) && between(this.z - 1.0E-5D, v.z, this.z + 1.0E-5D);
    }

    public static boolean between(double min, double value, double max) {
        return min <= value && value <= max;
    }

    public Vector3 copy() {
        return new Vector3(this);
    }

    public String toString() {
        MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
        return "Vector3(" + new BigDecimal(this.x, cont) + ", " + new BigDecimal(this.y, cont) + ", " + new BigDecimal(this.z, cont) + ")";
    }

    public Vector3 normalize() {
        double d = mag();
        if (d != 0) {
            multiply(1 / d);
        }
        return this;
    }
}
