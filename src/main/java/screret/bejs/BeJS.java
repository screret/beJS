package screret.bejs;

import dev.latvian.mods.kubejs.KubeJSRegistries;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.bejs.kubejs.BlockEntityJS;

import javax.annotation.Nullable;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BeJS.MODID)
public class BeJS {
    public static final String MODID = "bejs";

    public static final Logger LOGGER = LogManager.getLogger();

    public BeJS() {
        //IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::attachCaps);
    }

    private void attachCaps(final AttachCapabilitiesEvent<BlockEntity> event) {
        if(event.getObject() instanceof BlockEntityJS beJs) {
            BlockEntityJS.Builder builder = beJs.builder == null ? (BlockEntityJS.Builder) RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.objects.get(KubeJSRegistries.blockEntities().getId(beJs.getType())) : beJs.builder;
            if(builder.energyHandler != null) {
                EnergyStorage backend = new EnergyStorage(builder.energyHandler.capacity(), builder.energyHandler.maxReceive(), builder.energyHandler.maxExtract());
                LazyOptional<IEnergyStorage> optionalStorage = LazyOptional.of(() -> backend);

                ICapabilityProvider provider = new ICapabilitySerializable<>() {
                    @Override
                    public Tag serializeNBT() {
                        return backend.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(Tag nbt) {
                        backend.deserializeNBT(nbt);
                    }

                    @Override
                    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction) {
                        return ForgeCapabilities.ENERGY.orEmpty(cap, optionalStorage);
                    }
                };

                event.addCapability(new ResourceLocation(MODID, "energy_cap"), provider);
            }

            if(builder.itemHandler != null) {
                ItemStackHandler backend = new ItemStackHandler(builder.itemHandler.capacity());
                LazyOptional<IItemHandler> optionalStorage = LazyOptional.of(() -> backend);

                ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>() {
                    @Override
                    public CompoundTag serializeNBT() {
                        return backend.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(CompoundTag nbt) {
                        backend.deserializeNBT(nbt);
                    }

                    @Override
                    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction) {
                        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, optionalStorage);
                    }
                };

                event.addCapability(new ResourceLocation(MODID, "item_cap"), provider);
            }

            if(builder.fluidHandler != null) {
                FluidTank backend = new FluidTank(builder.fluidHandler.capacity(), builder.fluidHandler.validator());
                LazyOptional<IFluidHandler> optionalStorage = LazyOptional.of(() -> backend);

                ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>() {
                    @Override
                    public CompoundTag serializeNBT() {
                        return backend.writeToNBT(new CompoundTag());
                    }

                    @Override
                    public void deserializeNBT(CompoundTag nbt) {
                        backend.readFromNBT(nbt);
                    }

                    @Override
                    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction) {
                        return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, optionalStorage);
                    }
                };

                event.addCapability(new ResourceLocation(MODID, "fluid_cap"), provider);
            }
        }
    }
}
