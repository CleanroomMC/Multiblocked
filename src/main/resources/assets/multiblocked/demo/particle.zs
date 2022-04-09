import mods.multiblocked.MBDParticle;

import crafttweaker.world.IWorld;
import crafttweaker.util.IRandom;

val world = MBDParticle.getWorld();
val rand = world.random;

for i in 0 to 20 {
    val particle = MBDParticle.texture(world, 0.5, 2, 0.5);
    particle.setMotion(
        (rand.nextFloat() * 2 - 1) * 0.1,
        (rand.nextFloat() * 2 - 1) * 0.1,
        (rand.nextFloat() * 2 - 1) * 0.1);
    particle.setBackLayer(true);
    particle.setMotionless(true);
    particle.setAddBlend(true);
    val alpha = rand.nextInt(100) + 100;
    particle.setColor(alpha, 0x5f, 0xff, 0xff);
    particle.setScale(1);
    particle.setTexturesCount(2);
    particle.setTexturesIndex(1, 1);
    particle.setLightingMap(15, 15);
    particle.setLife(40);
    particle.setTexture("multiblocked:textures/fx/fx.png");
    particle.create();
}

val particle = MBDParticle.texture(world, 0.5, 2, 0.5, true);
particle.setTexture("multiblocked:start");
particle.setBackLayer(true);
particle.setScale(16);
particle.setLightingMap(15, 15);
particle.setImmortal();
particle.create();
