package screret.bejs;

import dev.architectury.registry.registries.Registrar;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeTypesEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import screret.bejs.common.BlockEntityJS;
import screret.bejs.common.EntityBlockJS;
import screret.bejs.common.MultiBlockControllerBlock;
import screret.bejs.kubejs.ProcessingRecipeJS;
import screret.bejs.recipe.RecipeTypeBuilder;
import screret.bejs.util.BlockInWorldExtended;

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
        RegistryObjectBuilderTypes.BLOCK.addType("multiblock", MultiBlockControllerBlock.Builder.class, MultiBlockControllerBlock.Builder::new);

        RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addType("basic", BlockEntityJS.Builder.class, BlockEntityJS.Builder::new);

        RECIPE_TYPE.addType("basic", RecipeTypeBuilder.class, RecipeTypeBuilder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("BlockEntity", BlockEntity.class);
        event.add("ForgeCapabilities", ForgeCapabilities.class);

        event.add("BlockPatternBuilder", BlockPatternBuilder.class);
        event.add("BlockInWorld", BlockInWorldExtended.class);
        event.add("BlockPredicate", BlockStatePredicate.class);
    }

    @Override
    public void registerRecipeTypes(RegisterRecipeTypesEvent event) {
        for (var type : RecipeTypeBuilder.ALL_RECIPES.keySet()) {
            event.register(type, ProcessingRecipeJS::new);
        }
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
