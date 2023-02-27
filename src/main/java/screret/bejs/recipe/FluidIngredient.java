package screret.bejs.recipe;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FluidIngredient {

    public static final FluidIngredient EMPTY = new FluidIngredient(Stream.empty());
    private final FluidIngredient.Value[] values;
    @Nullable
    private FluidStack[] fluidStacks;
    private int invalidationCounter;

    protected FluidIngredient(Stream<? extends FluidIngredient.Value> pValues) {
        this.values = pValues.toArray(Value[]::new);
    }

    public FluidStack[] getItems() {
        this.dissolve();
        return this.fluidStacks;
    }

    private void dissolve() {
        if (this.fluidStacks == null) {
            this.fluidStacks = Arrays.stream(this.values).flatMap((value) -> value.getFluids().stream()).distinct().toArray(FluidStack[]::new);
        }
    }

    public boolean test(@Nullable FluidStack pStack) {
        if (pStack == null) {
            return false;
        } else {
            this.dissolve();
            if (this.fluidStacks.length == 0) {
                return pStack.isEmpty();
            } else {
                for(FluidStack fluidStack : this.fluidStacks) {
                    if (fluidStack.isFluidEqual(pStack)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public final void toNetwork(FriendlyByteBuf pBuffer) {
        this.dissolve();
        pBuffer.writeCollection(Arrays.asList(this.fluidStacks), FriendlyByteBuf::writeFluidStack);
    }

    public JsonElement toJson() {
        if (this.values.length == 1) {
            return this.values[0].serialize();
        } else {
            JsonArray jsonarray = new JsonArray();

            for(FluidIngredient.Value value : this.values) {
                jsonarray.add(value.serialize());
            }

            return jsonarray;
        }
    }

    public boolean isEmpty() {
        return this.values.length == 0 && (this.fluidStacks == null || this.fluidStacks.length == 0);
    }

    public static FluidIngredient fromValues(Stream<? extends FluidIngredient.Value> pStream) {
        FluidIngredient ingredient = new FluidIngredient(pStream);
        return ingredient.values.length == 0 ? EMPTY : ingredient;
    }

    public static FluidIngredient of() {
        return EMPTY;
    }

    public static FluidIngredient of(int amount, Fluid... pItems) {
        return of(Arrays.stream(pItems).map(fluid -> new FluidStack(fluid, amount)));
    }

    public static FluidIngredient of(FluidStack... pStacks) {
        return of(Arrays.stream(pStacks));
    }

    public static FluidIngredient of(Stream<FluidStack> pStacks) {
        return fromValues(pStacks.filter((p_43944_) -> {
            return !p_43944_.isEmpty();
        }).map(FluidIngredient.FluidValue::new));
    }

    public static FluidIngredient of(TagKey<Fluid> pTag, int amount) {
        return fromValues(Stream.of(new FluidIngredient.TagValue(pTag, amount)));
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf pBuffer) {
        int size = pBuffer.readVarInt();
        if (size == -1) return of(pBuffer.readFluidStack());
        return fromValues(Stream.generate(() -> new FluidIngredient.FluidValue(pBuffer.readFluidStack())).limit(size));
    }

    public static FluidIngredient fromJson(@Nullable JsonElement pJson) {
        if (pJson != null && !pJson.isJsonNull()) {
            if (pJson.isJsonObject()) {
                return fromValues(Stream.of(valueFromJson(pJson.getAsJsonObject())));
            } else if (pJson.isJsonArray()) {
                JsonArray jsonarray = pJson.getAsJsonArray();
                if (jsonarray.size() == 0) {
                    throw new JsonSyntaxException("Fluid array cannot be empty, at least one item must be defined");
                } else {
                    return fromValues(StreamSupport.stream(jsonarray.spliterator(), false).map((jsonElement) -> {
                        return valueFromJson(GsonHelper.convertToJsonObject(jsonElement, "fluid"));
                    }));
                }
            } else {
                throw new JsonSyntaxException("Expected fluid to be object or array of objects");
            }
        } else {
            throw new JsonSyntaxException("Fluid cannot be null");
        }
    }

    public static FluidIngredient.Value valueFromJson(JsonObject pJson) {
        if (pJson.has("FluidName") && pJson.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (pJson.has("FluidName")) {
            FluidStack fluid = FluidStack.CODEC.decode(JsonOps.INSTANCE, pJson).map(Pair::getFirst).result().orElse(FluidStack.EMPTY);
            return new FluidIngredient.FluidValue(fluid);
        } else if (pJson.has("tag")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "tag"));
            TagKey<Fluid> tag = TagKey.create(Registry.FLUID_REGISTRY, resourcelocation);
            int amount = pJson.has("Amount") ? GsonHelper.getAsInt(pJson, "Amount") : 1;
            return new FluidIngredient.TagValue(tag, amount);
        } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
    }

    public static class FluidValue implements FluidIngredient.Value {
        private final FluidStack fluid;

        public FluidValue(FluidStack pItem) {
            this.fluid = pItem;
        }

        @Override
        public Collection<FluidStack> getFluids() {
            return Collections.singleton(this.fluid);
        }

        public JsonObject serialize() {
            JsonObject result = FluidStack.CODEC.encode(fluid, JsonOps.INSTANCE, new JsonObject()).result().orElse(new JsonObject()).getAsJsonObject();
            return result;
        }
    }

    public static class TagValue implements FluidIngredient.Value {
        private final TagKey<Fluid> tag;
        private final int amount;

        public TagValue(TagKey<Fluid> pTag) {
            this.tag = pTag;
            this.amount = 1;
        }

        public TagValue(TagKey<Fluid> pTag, int amount) {
            this.tag = pTag;
            this.amount = amount;
        }

        @Override
        public Collection<FluidStack> getFluids() {
            List<FluidStack> list = Lists.newArrayList();

            for(Holder<Fluid> holder : Registry.FLUID.getTagOrEmpty(this.tag)) {
                list.add(new FluidStack(holder.value(), amount));
            }

            if (list.size() == 0) {
                list.add(new FluidStack(Fluids.EMPTY, amount));
            }
            return list;
        }

        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("tag", this.tag.location().toString());
            json.addProperty("Amount", amount);
            return json;
        }
    }

    public interface Value {
        Collection<FluidStack> getFluids();

        JsonObject serialize();
    }
}
