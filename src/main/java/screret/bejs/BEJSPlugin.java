package screret.bejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import screret.bejs.kubejs.BlockEntityJS;
import screret.bejs.kubejs.EntityBlockJS;

public class BEJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        RegistryObjectBuilderTypes.BLOCK.addType("entity", EntityBlockJS.Builder.class, EntityBlockJS.Builder::new);

        RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addType("basic", BlockEntityJS.Builder.class, BlockEntityJS.Builder::new);
    }
}
