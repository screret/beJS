package screret.bejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import screret.bejs.kubejs.BlockEntityJS;
import screret.bejs.kubejs.EntityBlockJS;

public class BEJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        RegistryObjectBuilderTypes.BLOCK.addType("entity", EntityBlockJS.Builder.class, EntityBlockJS.Builder::new);

        RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addType("basic", BlockEntityJS.Builder.class, BlockEntityJS.Builder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("BlockEntity", BlockEntity.class);
        event.add("ForgeCapabilities", ForgeCapabilities.class);
        //event.add("BeJSCapabilities", BeJSCapabilities.class);
    }

    /*
    @Override
    public void afterInit() {
        if(Platform.isModLoaded("powerfuljs")) {
            for (var builder : RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.objects.entrySet()) {
                BlockEntityTypeBuilder beBuilder = (BlockEntityTypeBuilder) builder.getValue();
                for (var capBuilder : beBuilder.capabilityBuilders) {
                    CapabilityService.INSTANCE.addBECapability(beBuilder.get(), capBuilder);
                }
            }
        }
    }
    */
}
