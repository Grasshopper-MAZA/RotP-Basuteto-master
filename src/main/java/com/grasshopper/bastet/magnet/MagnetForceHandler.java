package com.grasshopper.bastet.magnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class MagnetForceHandler {

    public static final double BASE_RADIUS = 5.0D;
    public static final double MAX_RADIUS = 15.0D;

    private static final Random RANDOM = new Random();

    private static final Map<UUID, UUID> ITEM_OWNER = new HashMap<>();

    public static void releaseItems(UUID entityId) {
        ITEM_OWNER.entrySet().removeIf(entry -> entry.getValue().equals(entityId));
    }

    public static double getRadius(LivingEntity entity) {
        float progress = MagnetizedData.getProgress(entity);
        return BASE_RADIUS + (MAX_RADIUS - BASE_RADIUS) * progress;
    }

    public static boolean isStandUser(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) return false;
        try {
            IStandPower power = IStandPower.getPlayerStandPower((PlayerEntity) entity);
            return power != null && power.hasPower();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isMagnetDisabled(LivingEntity entity) {
        return isStandUser(entity);
    }

    public static void applyMagnetForce(LivingEntity entity) {
        if (isMagnetDisabled(entity)) return;

        World world = entity.level;
        Vector3d pos = entity.position();
        double radius = getRadius(entity);

        int attachedCount = pullIronItems(entity, world, pos, radius);
        pullTowardIronBlocks(entity, world, pos, radius);
        pullOtherMagnetizedEntities(entity, world, pos, radius);
        pullIronArmorPlayers(entity, world, pos, radius);

        applySlowdown(entity, attachedCount);
        applyRotationResistance(entity, attachedCount);
        applyMagnetSlowdown(entity, world, pos, radius);
        tryEscape(entity, attachedCount);

        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.85D, 1.0D, 0.85D));
    }

    private static void pullIronArmorPlayers(LivingEntity entity, World world, Vector3d pos, double radius) {
        AxisAlignedBB area = new AxisAlignedBB(pos, pos).inflate(radius);
        List<PlayerEntity> players = world.getEntitiesOfClass(PlayerEntity.class, area);

        for (PlayerEntity player : players) {
            if (player == entity) continue;
            if (!hasIronArmor(player)) continue;

            Vector3d dir = player.position().subtract(pos);
            double dist = dir.length();
            if (dist < 0.001 || dist > radius) continue;

            AxisAlignedBB myBox = entity.getBoundingBox().inflate(0.05D);
            if (myBox.intersects(player.getBoundingBox())) continue;

            dir = dir.normalize();

            double force = 0.3D;
            double scaled = force * (1.0D - dist / radius);

            entity.setDeltaMovement(entity.getDeltaMovement().add(dir.scale(scaled)));
            entity.hurtMarked = true;

            entity.addEffect(new EffectInstance(
                    Effects.MOVEMENT_SLOWDOWN,
                    10,
                    1,
                    false, false, false
            ));
        }
    }

    private static boolean hasIronArmor(PlayerEntity player) {
        return isIronArmorItem(player.getItemBySlot(EquipmentSlotType.HEAD))
                || isIronArmorItem(player.getItemBySlot(EquipmentSlotType.CHEST))
                || isIronArmorItem(player.getItemBySlot(EquipmentSlotType.LEGS))
                || isIronArmorItem(player.getItemBySlot(EquipmentSlotType.FEET));
    }

    private static boolean isIronArmorItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == Items.IRON_HELMET
                || stack.getItem() == Items.IRON_CHESTPLATE
                || stack.getItem() == Items.IRON_LEGGINGS
                || stack.getItem() == Items.IRON_BOOTS
                || stack.getItem() == Items.CHAINMAIL_HELMET
                || stack.getItem() == Items.CHAINMAIL_CHESTPLATE
                || stack.getItem() == Items.CHAINMAIL_LEGGINGS
                || stack.getItem() == Items.CHAINMAIL_BOOTS;
    }

    private static void applyMagnetSlowdown(LivingEntity entity, World world, Vector3d pos, double radius) {
        AxisAlignedBB area = new AxisAlignedBB(pos, pos).inflate(radius);
        List<LivingEntity> others = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != entity && MagnetizedData.isMagnetized(e) && !isStandUser(e));

        if (!others.isEmpty()) {
            entity.addEffect(new EffectInstance(
                    Effects.MOVEMENT_SLOWDOWN,
                    10,
                    3,
                    false, false, false
            ));
            entity.addEffect(new EffectInstance(
                    Effects.WEAKNESS,
                    10,
                    1,
                    false, false, false
            ));
        }
    }

    private static void tryEscape(LivingEntity entity, int attachedCount) {
        if (attachedCount <= 0) return;
        if (entity instanceof PlayerEntity) return;
        if (!(entity instanceof MobEntity)) return;

        MobEntity mob = (MobEntity) entity;

        float ratio = Math.min(1.0F, attachedCount / 30.0F);

        if (mob.tickCount % 20 != 0) return;

        if (RANDOM.nextFloat() > 0.3F + ratio * 0.5F) return;

        Vector3d away = findNearestMagnetDirection(mob);

        if (away != null) {
            double force = 0.2D + ratio * 0.3D;
            mob.setDeltaMovement(mob.getDeltaMovement().add(away.scale(force)));
            mob.hurtMarked = true;
            mob.getNavigation().stop();
        }
    }

    private static Vector3d findNearestMagnetDirection(LivingEntity entity) {
        World world = entity.level;
        Vector3d pos = entity.position();
        double radius = getRadius(entity);

        AxisAlignedBB area = new AxisAlignedBB(pos, pos).inflate(radius);
        List<LivingEntity> others = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != entity && MagnetizedData.isMagnetized(e) && !isStandUser(e));

        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (LivingEntity other : others) {
            double dist = other.distanceToSqr(entity);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = other;
            }
        }

        if (nearest != null) {
            Vector3d dir = pos.subtract(nearest.position());
            double len = dir.length();
            if (len > 0.001) {
                return dir.normalize();
            }
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int r = (int) radius;
        BlockPos nearestBlock = null;
        double nearestBlockDist = Double.MAX_VALUE;

        for (int x = -r; x <= r; x += 2) {
            for (int y = -r; y <= r; y += 2) {
                for (int z = -r; z <= r; z += 2) {
                    mutable.set(pos.x + x, pos.y + y, pos.z + z);
                    if (isIronBlock(world.getBlockState(mutable))) {
                        double dist = pos.distanceToSqr(mutable.getX() + 0.5, mutable.getY() + 0.5, mutable.getZ() + 0.5);
                        if (dist < nearestBlockDist) {
                            nearestBlockDist = dist;
                            nearestBlock = mutable.immutable();
                        }
                    }
                }
            }
        }

        if (nearestBlock != null) {
            Vector3d dir = pos.subtract(new Vector3d(
                    nearestBlock.getX() + 0.5,
                    nearestBlock.getY() + 0.5,
                    nearestBlock.getZ() + 0.5
            ));
            double len = dir.length();
            if (len > 0.001) {
                return dir.normalize();
            }
        }

        return null;
    }

    private static int pullIronItems(LivingEntity entity, World world, Vector3d pos, double radius) {
        AxisAlignedBB area = new AxisAlignedBB(pos, pos).inflate(radius);
        List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, area);

        int attached = 0;
        UUID entityId = entity.getUUID();
        AxisAlignedBB hitbox = entity.getBoundingBox().inflate(0.3D);

        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            if (!isIronItem(stack, world)) continue;

            CompoundNBT nbt = item.getPersistentData();

            UUID owner;
            if (nbt.hasUUID("BastetMagnetOwner")) {
                owner = nbt.getUUID("BastetMagnetOwner");
            } else {
                owner = entityId;
                nbt.putUUID("BastetMagnetOwner", entityId);
            }

            if (!owner.equals(entityId)) continue;

            item.setNoGravity(true);
            item.lifespan = Integer.MAX_VALUE;
            item.setPickUpDelay(Integer.MAX_VALUE);

            Vector3d target = pos.add(0, entity.getBbHeight() * 0.5, 0);
            Vector3d dir = target.subtract(item.position());

            if (hitbox.intersects(item.getBoundingBox())) {
                item.setDeltaMovement(Vector3d.ZERO);
                item.hurtMarked = true;
                attached += stack.getCount();

                if (!nbt.getBoolean("BastetHurtOnce")) {
                    float damage = getItemDamage(stack, entity);
                    if (damage > 0) {
                        entity.hurt(DamageSource.GENERIC, damage);
                        applySharpEnchantEffects(stack, entity);
                        nbt.putBoolean("BastetHurtOnce", true);
                    }
                }

                continue;
            }

            dir = dir.normalize().scale(0.2D);
            item.setDeltaMovement(item.getDeltaMovement().add(dir));
            item.hurtMarked = true;

            attached += stack.getCount();
        }

        return attached;
    }

    private static float getItemDamage(ItemStack stack, LivingEntity target) {
        if (stack.isEmpty()) return 0F;

        float base = 0F;

        if (stack.getItem() instanceof SwordItem) {
            base = ((SwordItem) stack.getItem()).getDamage();
        } else if (stack.getItem() instanceof ToolItem) {
            base = ((ToolItem) stack.getItem()).getAttackDamage();
        } else if (isIronArmorItem(stack)) {
            base = 1.0F;
        } else if (isSharpItem(stack)) {
            base = 2.0F;
        }

        if (base <= 0) return 0F;

        float enchant = getSharpEnchantDamage(stack, target);
        return base + enchant;
    }

    private static float getSharpEnchantDamage(ItemStack stack, LivingEntity target) {
        if (stack.isEmpty()) return 0F;

        float damage = 0F;

        damage += getEnchantLevel(stack, Enchantments.SHARPNESS) * 1.0F;

        int smite = getEnchantLevel(stack, Enchantments.SMITE);
        if (smite > 0 && target.getMobType() == CreatureAttribute.UNDEAD) {
            damage += smite * 2.5F;
        }

        int bane = getEnchantLevel(stack, Enchantments.BANE_OF_ARTHROPODS);
        if (bane > 0 && target.getMobType() == CreatureAttribute.ARTHROPOD) {
            damage += bane * 2.5F;
        }

        return damage;
    }

    private static void applySharpEnchantEffects(ItemStack stack, LivingEntity target) {
        if (stack.isEmpty()) return;

        int fireAspect = getEnchantLevel(stack, Enchantments.FIRE_ASPECT);
        if (fireAspect > 0) {
            target.setSecondsOnFire(fireAspect * 4);
        }

        int knockback = getEnchantLevel(stack, Enchantments.KNOCKBACK);
        if (knockback > 0) {
            Vector3d knockDir = target.position().subtract(target.xo, target.yo, target.zo);
            if (knockDir.lengthSqr() < 0.001) {
                knockDir = new Vector3d(0, 0, 1);
            }
            knockDir = knockDir.normalize().scale(knockback * 0.5D);
            target.push(knockDir.x, 0.1D, knockDir.z);
            target.hurtMarked = true;
        }
    }

    private static int getEnchantLevel(ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) return 0;
        try {
            return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void applySlowdown(LivingEntity entity, int attachedCount) {
        if (attachedCount <= 0) return;

        int level = (int) (attachedCount * 0.5F / 10) + 1;

        if (level > 6) level = 6;

        entity.addEffect(new EffectInstance(
                Effects.MOVEMENT_SLOWDOWN,
                10,
                level - 1,
                false, false, false
        ));

        if (level >= 6) {
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
            entity.hurtMarked = true;
        }
    }

    private static void applyRotationResistance(LivingEntity entity, int attachedCount) {
        if (attachedCount <= 0) return;

        float ratio = Math.min(1.0F, attachedCount / 100.0F);
        float resistance = 1.0F - ratio * 0.9F;

        float yawDiff = entity.yRot - entity.yRotO;
        float pitchDiff = entity.xRot - entity.xRotO;

        float newYaw = entity.yRotO + yawDiff * resistance;
        float newPitch = entity.xRotO + pitchDiff * resistance;

        entity.yRot = newYaw;
        entity.xRot = newPitch;
        entity.yRotO = newYaw;
        entity.xRotO = newPitch;
        entity.yHeadRot = newYaw;
        entity.yHeadRotO = newYaw;
        entity.yBodyRot = newYaw;
        entity.yBodyRotO = newYaw;
    }

    private static void pullTowardIronBlocks(LivingEntity entity, World world, Vector3d pos, double radius) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int r = (int) radius;

        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    mutable.set(pos.x + x, pos.y + y, pos.z + z);
                    BlockState state = world.getBlockState(mutable);
                    if (isIronBlock(state)) {
                        double dist = pos.distanceToSqr(mutable.getX() + 0.5, mutable.getY() + 0.5, mutable.getZ() + 0.5);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = mutable.immutable();
                        }
                    }
                }
            }
        }

        if (nearest == null) return;

        if (nearestDist <= 1.0D) {
            CompoundNBT nbt = entity.getPersistentData();
            String key = "BastetIronBlockHurt_" + nearest.getX() + "_" + nearest.getY() + "_" + nearest.getZ();

            if (!nbt.getBoolean(key)) {
                entity.hurt(DamageSource.GENERIC, 5.0F);
                nbt.putBoolean(key, true);
            }
            return;
        }

        Vector3d dir = new Vector3d(
                nearest.getX() + 0.5 - pos.x,
                nearest.getY() + 0.5 - pos.y,
                nearest.getZ() + 0.5 - pos.z
        ).normalize().scale(0.2D);
        entity.setDeltaMovement(entity.getDeltaMovement().add(dir));
        entity.hurtMarked = true;
    }

    private static void pullOtherMagnetizedEntities(LivingEntity entity, World world, Vector3d pos, double radius) {
        AxisAlignedBB area = new AxisAlignedBB(pos, pos).inflate(radius);
        List<LivingEntity> others = world.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity other : others) {
            if (other == entity) continue;
            if (!MagnetizedData.isMagnetized(other)) continue;
            if (isStandUser(other)) continue;

            Vector3d dir = other.position().subtract(pos);
            double dist = dir.length();
            if (dist < 0.001 || dist > radius) continue;

            AxisAlignedBB myBox = entity.getBoundingBox().inflate(0.05D);
            if (myBox.intersects(other.getBoundingBox())) {
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.3D, 1.0D, 0.3D));
                other.setDeltaMovement(other.getDeltaMovement().multiply(0.3D, 1.0D, 0.3D));
                continue;
            }

            dir = dir.normalize();

            double force = 0.25D;
            double scaled = force * (1.0D - dist / radius);

            entity.setDeltaMovement(entity.getDeltaMovement().add(dir.scale(scaled)));
            entity.hurtMarked = true;

            other.setDeltaMovement(other.getDeltaMovement().add(dir.scale(-scaled)));
            other.hurtMarked = true;
        }
    }

    public static boolean isIronItem(ItemStack stack, World world) {
        if (stack.isEmpty()) return false;

        if (isDirectIronItem(stack)) return true;

        return recipeContainsIron(stack, world);
    }

    public static boolean isSharpItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == Items.IRON_SWORD
                || stack.getItem() == Items.IRON_PICKAXE
                || stack.getItem() == Items.IRON_AXE
                || stack.getItem() == Items.IRON_SHOVEL
                || stack.getItem() == Items.IRON_HOE
                || stack.getItem() == Items.SHEARS;
    }

    private static boolean isDirectIronItem(ItemStack stack) {
        return stack.getItem() == Items.IRON_INGOT
                || stack.getItem() == Items.IRON_NUGGET
                || stack.getItem() == Items.IRON_BLOCK
                || stack.getItem() == Items.IRON_ORE
                || stack.getItem() == Items.IRON_DOOR
                || stack.getItem() == Items.IRON_BARS
                || stack.getItem() == Items.IRON_SWORD
                || stack.getItem() == Items.IRON_PICKAXE
                || stack.getItem() == Items.IRON_AXE
                || stack.getItem() == Items.IRON_SHOVEL
                || stack.getItem() == Items.IRON_HOE
                || stack.getItem() == Items.IRON_HELMET
                || stack.getItem() == Items.IRON_CHESTPLATE
                || stack.getItem() == Items.IRON_LEGGINGS
                || stack.getItem() == Items.IRON_BOOTS
                || stack.getItem() == Items.IRON_HORSE_ARMOR
                || stack.getItem() == Items.MINECART
                || stack.getItem() == Items.BUCKET
                || stack.getItem() == Items.CAULDRON
                || stack.getItem() == Items.ANVIL
                || stack.getItem() == Items.CHIPPED_ANVIL
                || stack.getItem() == Items.DAMAGED_ANVIL
                || stack.getItem() == Items.HOPPER
                || stack.getItem() == Items.IRON_TRAPDOOR
                || stack.getItem() == Items.HEAVY_WEIGHTED_PRESSURE_PLATE
                || stack.getItem() == Items.LIGHT_WEIGHTED_PRESSURE_PLATE
                || stack.getItem() == Items.SMITHING_TABLE
                || stack.getItem() == Items.STONECUTTER
                || stack.getItem() == Items.GRINDSTONE
                || stack.getItem() == Items.CROSSBOW
                || stack.getItem() == Items.SHIELD
                || stack.getItem() == Items.FLINT_AND_STEEL
                || stack.getItem() == Items.SHEARS
                || stack.getItem() == Items.RAIL
                || stack.getItem() == Items.DETECTOR_RAIL
                || stack.getItem() == Items.ACTIVATOR_RAIL
                || stack.getItem() == Items.POWERED_RAIL
                || stack.getItem() == Items.TRIPWIRE_HOOK
                || stack.getItem() == Items.CHAIN
                || stack.getItem() == Items.CHAINMAIL_HELMET
                || stack.getItem() == Items.CHAINMAIL_CHESTPLATE
                || stack.getItem() == Items.CHAINMAIL_LEGGINGS
                || stack.getItem() == Items.CHAINMAIL_BOOTS
                || stack.getItem() == Items.LANTERN
                || stack.getItem() == Items.SOUL_LANTERN
                || stack.getItem() == Items.LODESTONE;
    }

    private static boolean recipeContainsIron(ItemStack stack, World world) {
        return recipeContainsIron(stack, world, 0);
    }

    private static boolean recipeContainsIron(ItemStack stack, World world, int depth) {
        if (world == null) return false;
        if (depth > 3) return false;

        try {
            for (IRecipe<?> recipe : world.getRecipeManager().getRecipes()) {
                if (!(recipe instanceof ICraftingRecipe)) continue;
                if (recipe.isSpecial()) continue;

                ICraftingRecipe crafting = (ICraftingRecipe) recipe;
                ItemStack result = crafting.getResultItem();
                if (result.isEmpty()) continue;
                if (!ItemStack.isSame(result, stack)) continue;

                NonNullList<Ingredient> ingredients = crafting.getIngredients();
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.isEmpty()) continue;
                    ItemStack[] matching = ingredient.getItems();
                    for (ItemStack match : matching) {
                        if (isDirectIronItem(match)) {
                            return true;
                        }
                        if (recipeContainsIron(match, world, depth + 1)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public static boolean isIronBlock(BlockState state) {
        return state.is(Blocks.IRON_BLOCK)
                || state.is(Blocks.IRON_ORE)
                || state.is(Blocks.IRON_BARS)
                || state.is(Blocks.IRON_DOOR)
                || state.is(Blocks.IRON_TRAPDOOR)
                || state.is(Blocks.ANVIL)
                || state.is(Blocks.CHIPPED_ANVIL)
                || state.is(Blocks.DAMAGED_ANVIL)
                || state.is(Blocks.HOPPER)
                || state.is(Blocks.CAULDRON)
                || state.is(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)
                || state.is(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)
                || state.is(Blocks.RAIL)
                || state.is(Blocks.DETECTOR_RAIL)
                || state.is(Blocks.ACTIVATOR_RAIL)
                || state.is(Blocks.POWERED_RAIL)
                || state.is(Blocks.STONECUTTER)
                || state.is(Blocks.SMITHING_TABLE)
                || state.is(Blocks.GRINDSTONE)
                || state.is(Blocks.CHAIN)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(Blocks.LODESTONE);
    }
}