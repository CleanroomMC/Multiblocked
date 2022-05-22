# Multiblocked

**Multiblocked (mbd) is an extremely flexible yet vanilla-esque multiblock mod, that embraces aspects of MultiblockTweaker and Modular Machinery.**

The goal was to make it as easy as possible for modpack/mod authors to create multiblocks to interacte IO from other mods, and provide as much detail control as possible. Mbd is essentially both a tweaker mod and a library mod that provides rendering and logic APIs.

#### Other versions: [1.16.5+](https://github.com/Low-Drag-MC/Multiblocked)

------

#### Features:

1. Mbd deals with all IO capability in **proxy**. This means we don't need to create specific `XXXHatch` for capabilities. For example, item input can be a chest, a furnace, or other container  which items can be extracted. Besides, you could also specify a specific block or capability, mbd only care about the IO and not the interaction of the block itself.

2. We noticed that modpack authors struggled with script writing when creating multiblocks and adding recipes. We designed a **visual editor** to help users create multiblocks intuitively and interactively. You can create multiblocks, recipes, and configure their details without writing any scripts.  

3. Provides modpack authors with **extensive function interfaces**, mbd exposed nearly everything via CT. For example, update logic, data sync, custom recipe logic, and so on.

4. Provides a wide variety of rendering techniques, **allowing you to create a wide variety of renderers without using Java or even ct script**. You can easily use mbd to create multiblock like gregtech style (**dynamically extensible structure**), IE style (**dynamic model**), thaumcraft style (**animation model**). Currently supported renderers: BlockState, Java Model, OBJ, B3D, Gregtech Model, Geo, Particle.  Renderers are extensible, and you can register your renderer with Java.

   We are compatible with **Geckolib** to use render bedrock animate models.  

   Provides ways for add **emissive textures** for all models.  

   Provides **particle system** that supports rendering textured particles，laser particles and shader particles.  

5. Take advantage of async threads for incredible performance, 20tps forever. 