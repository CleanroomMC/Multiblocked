package com.cleanroommc.multiblocked.client;

import com.cleanroommc.multiblocked.MbdConfig;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraftforge.fml.common.Loader;

import java.io.File;

public class MultiblockedResourceLoader extends FolderResourcePack {

    public static final MultiblockedResourceLoader INSTANCE = new MultiblockedResourceLoader();

    private MultiblockedResourceLoader() {
        super(new File(Loader.instance().getConfigDir(), MbdConfig.location));
        resourcePackFile.mkdir();
        new File(resourcePackFile, "assets").mkdir();
    }

    @Override
    public String getPackName() {
        return "MultiblockedInternalResources";
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) {
        JsonObject metadata = new JsonObject();
        JsonObject packObj = new JsonObject();
        metadata.add("pack", packObj);
        packObj.addProperty("description", "Includes assets provided by the user for Multiblocked.");
        packObj.addProperty("pack_format", 2);
        return metadataSerializer.parseMetadataSection(metadataSectionName, metadata);
    }

}
