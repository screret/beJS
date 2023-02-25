package screret.bejs;

import dev.architectury.registry.registries.Registrar;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import screret.bejs.kubejs.BlockEntityJS;
import screret.bejs.kubejs.EntityBlockJS;
import screret.bejs.kubejs.MultiBlockBuilder;
import screret.bejs.recipe.RecipeTypeBuilder;

import static dev.latvian.mods.kubejs.KubeJSRegistries.genericRegistry;
import static net.minecraft.core.Registry.RECIPE_TYPE_REGISTRY;

public class BEJSPlugin extends KubeJSPlugin {
    public static final RegistryObjectBuilderTypes<RecipeType<?>> RECIPE_TYPE = RegistryObjectBuilderTypes.add(RECIPE_TYPE_REGISTRY, RecipeType.class);

    public static Registrar<RecipeType<?>> recipeTypes() {
        return genericRegistry(RECIPE_TYPE_REGISTRY);
    }

    @Override
    public void init() {
        RegistryObjectBuilderTypes.BLOCK.addType("entity", EntityBlockJS.Builder.class, EntityBlockJS.Builder::new);

        RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addType("basic", BlockEntityJS.Builder.class, BlockEntityJS.Builder::new);
        RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addType("multiblock", MultiBlockBuilder.class, MultiBlockBuilder::new);
        RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addType("multiblock", MultiBlockBuilder.class, MultiBlockBuilder::new);

        RECIPE_TYPE.addType("basic", RecipeTypeBuilder.class, RecipeTypeBuilder::new);
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
