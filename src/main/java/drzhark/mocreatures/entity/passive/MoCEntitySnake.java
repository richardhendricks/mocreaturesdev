package drzhark.mocreatures.entity.passive;

import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.MoCEntityTameableAnimal;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.network.message.MoCMessageAnimation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * Biome - specific Forest Desert plains Swamp Jungle Tundra Taiga Extreme Hills
 * Ocean
 *
 * swamp: python, bright green, #1 plains: coral, cobra #1, #2, #3, #4 desert:
 * rattlesnake , #2 jungle: all except rattlesnake hills: all except python,
 * bright green, bright orange tundra-taiga: none ocean: leave alone
 *
 */

public class MoCEntitySnake extends MoCEntityTameableAnimal {

    private float fTongue;
    private float fMouth;
    private boolean isBiting;
    private float fRattle;
    private boolean isPissed;
    private int hissCounter;

    private int movInt;
    private boolean isNearPlayer;
    public float bodyswing;

    public static final String snakeNames[] = {"Dark", "Spotted", "Orange", "Green", "Coral", "Cobra", "Rattle", "Python"};

    public MoCEntitySnake(World world) {
        super(world);
        setSize(1.4F, 0.5F);
        //health = 10;
        this.bodyswing = 2F;
        this.movInt = this.rand.nextInt(10);
        setEdad(50 + this.rand.nextInt(50));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
    }

    @Override
    public void selectType() {
        // snake types:
        // 1 small blackish/dark snake (passive)
        // 2 dark green /brown snake (passive)
        // 3 bright orangy snake aggressive venomous swamp, jungle, forest
        // 4 bright green snake aggressive venomous swamp, jungle, forest
        // 5 coral (aggressive - venomous) small / plains, forest
        // 6 cobra (aggressive - venomous - spitting) plains, forest
        // 7 rattlesnake (aggressive - venomous) desert
        // 8 python (aggressive - non venomous) big - swamp
        // 9 sea snake (aggressive - venomous)
        if (getType() == 0) {
            setType(this.rand.nextInt(8) + 1);
        }
    }

    @Override
    public ResourceLocation getTexture() {
        switch (getType()) {
            case 1:
                return MoCreatures.proxy.getTexture("snake1.png");
            case 2:
                return MoCreatures.proxy.getTexture("snake2.png");
            case 3:
                return MoCreatures.proxy.getTexture("snake3.png");
            case 4:
                return MoCreatures.proxy.getTexture("snake4.png");
            case 5:
                return MoCreatures.proxy.getTexture("snake5.png");
            case 6:
                return MoCreatures.proxy.getTexture("snake6.png");
            case 7:
                return MoCreatures.proxy.getTexture("snake7.png");
            case 8:
                return MoCreatures.proxy.getTexture("snake8.png");
            default:
                return MoCreatures.proxy.getTexture("snake1.png");
        }
    }

    @Override
    public float getMoveSpeed() {
        return 0.6F;
    }

    @Override
    public void fall(float f, float f1) {
    }

    @Override
    public boolean isOnLadder() {
        return this.isCollidedHorizontally;
    }

    @Override
    // snakes can't jump
            protected
            void jump() {
    }

    @Override
    protected boolean canDespawn() {
        if (MoCreatures.proxy.forceDespawns) {
            return !getIsTamed();
        } else {
            return false;
        }
    }

    public boolean pickedUp() {
        return (this.ridingEntity != null);
    }

    @Override
    public boolean interact(EntityPlayer entityplayer) {
        //TODO
        //this doesn't work yet, make the player feed the mouse to this snake
        /*
         * if (entityplayer.riddenByEntity != null &&
         * entityplayer.riddenByEntity instanceof MoCEntityMouse) {
         * //System.out.println("player has a mouse"); }
         */
        if (super.interact(entityplayer)) {
            return false;
        }
        if (!getIsTamed()) {
            return false;
        }

        ItemStack itemstack = entityplayer.inventory.getCurrentItem();

        this.rotationYaw = entityplayer.rotationYaw;
        if (this.ridingEntity == null) {
            if (MoCreatures.isServer()) {
                mountEntity(entityplayer);
            }
        } else {
            this.worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F) + 1.0F);
            if (MoCreatures.isServer()) {
                this.mountEntity(null);
            }
        }
        this.motionX = entityplayer.motionX * 5D;
        this.motionY = (entityplayer.motionY / 2D) + 0.5D;
        this.motionZ = entityplayer.motionZ * 5D;

        return true;
    }

    @Override
    public boolean isNotScared() {
        // TODO depending on size!
        if (getType() > 2 && getEdad() > 50) {
            return true;
        }
        return false;
    }

    /**
     * returns true when is climbing up
     *
     * @return
     */
    public boolean isClimbing() {
        return isOnLadder() && this.motionY > 0.01F;
    }

    public boolean isResting() {
        return (!getNearPlayer() && this.onGround && (this.motionX < 0.01D && this.motionX > -0.01D) && (this.motionZ < 0.01D && this.motionZ > -0.01D));
    }

    public boolean getNearPlayer() {
        return this.isNearPlayer;
    }

    public int getMovInt() {
        return this.movInt;
    }

    @Override
    public boolean swimmerEntity() {
        return true;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    public void setNearPlayer(boolean flag) {
        this.isNearPlayer = flag;
    }

    /*
     * @Override public double getYOffset() { // If we are in SMP, do not alter
     * offset on any client other than the player being mounted on if
     * (((ridingEntity instanceof EntityPlayer) && !worldObj.isRemote) ||
     * ridingEntity == MoCreatures.proxy.getPlayer())//MoCProxy.mc().thePlayer)
     * { return(super.getYOffset() - 1.5F); } else { return super.getYOffset();
     * } }
     */

    @Override
    public double getYOffset() {
        if (this.ridingEntity instanceof EntityPlayer && this.ridingEntity == MoCreatures.proxy.getPlayer() && !MoCreatures.isServer()) {
            return (super.getYOffset() - 1.5F);
        }

        if ((this.ridingEntity instanceof EntityPlayer) && !MoCreatures.isServer()) {
            return (super.getYOffset() + 0.1F);
        } else {
            return super.getYOffset();
        }
    }

    public float getSizeF() {
        float factor = 1.0F;

        if (getType() == 1 || getType() == 2)// small shy snakes
        {
            factor = 0.8F;
        } else if (getType() == 5)// coral
        {
            factor = 0.6F;
        }
        if (getType() == 6)// cobra 1.1
        {
            factor = 1.1F;
        }
        if (getType() == 7)// rattlesnake
        {
            factor = 0.9F;
        }
        if (getType() == 8)// python
        {
            factor = 1.5F;
        }

        return this.getEdad() * 0.01F * factor;// */
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (getEdad() < 100 && this.rand.nextInt(500) == 0) {
            setEdad(getEdad() + 1);
        }

        if (pickedUp()) {
            this.movInt = 0;
        }

        if (isResting()) {

            this.prevRenderYawOffset = this.renderYawOffset = this.rotationYaw = this.prevRotationYaw;

        }

        if (!this.onGround && (this.ridingEntity != null)) {
            this.rotationYaw = this.ridingEntity.rotationYaw;// -90F;
        }

        if (getfTongue() != 0.0F) {
            setfTongue(getfTongue() + 0.2F);
            if (getfTongue() > 8.0F) {
                setfTongue(0.0F);
            }
        }

        if (this.worldObj.getDifficulty().getDifficultyId() > 0 && getNearPlayer() && !getIsTamed() && isNotScared()) {

            this.hissCounter++;

            // TODO synchronize and get sound
            // hiss
            if (this.hissCounter % 25 == 0) {
                setfMouth(0.3F);
                this.worldObj
                        .playSoundAtEntity(this, "mocreatures:snakeupset", 1.0F, 1.0F + ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F));
            }
            if (this.hissCounter % 35 == 0) {
                setfMouth(0.0F);
            }

            if (this.hissCounter > 100 && this.rand.nextInt(50) == 0) {
                // then randomly get pissed
                setPissed(true);
                this.hissCounter = 0;
            }
        }
        if (this.hissCounter > 500) {
            this.hissCounter = 0;
        }

        if (getfMouth() != 0.0F && this.hissCounter == 0) //biting
        {
            setfMouth(getfMouth() + 0.1F);
            if (getfMouth() > 0.5F) {
                setfMouth(0.0F);
            }
        }

        if (getType() == 7 && getfRattle() != 0.0F) // rattling
        {
            setfRattle(getfRattle() + 0.2F);
            if (getfRattle() == 1.0F) {
                // TODO synchronize
                this.worldObj.playSoundAtEntity(this, "mocreatures:snakerattle", 1.0F,
                        1.0F + ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F));
            }
            if (getfRattle() > 8.0F) {
                setfRattle(0.0F);
            }
        }
    }

    /**
     * from 0.0 to 4.0F 0.0 = inside mouth 2.0 = completely stuck out 3.0 =
     * returning 4.0 = in.
     *
     * @return
     */
    public float getfTongue() {
        return this.fTongue;
    }

    public void setfTongue(float fTongue) {
        this.fTongue = fTongue;
    }

    public float getfMouth() {
        return this.fMouth;
    }

    public void setfMouth(float fMouth) {
        this.fMouth = fMouth;
    }

    public float getfRattle() {
        return this.fRattle;
    }

    public void setfRattle(float fRattle) {
        this.fRattle = fRattle;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        /**
         * stick tongue
         */
        if (this.rand.nextInt(50) == 0 && getfTongue() == 0.0F) {
            setfTongue(0.1F);
        }

        /**
         * Open mouth
         */
        if (this.rand.nextInt(100) == 0 && getfMouth() == 0.0F) {
            setfMouth(0.1F);
        }

        if (getType() == 7) {
            int chance = 0;
            if (getNearPlayer()) {
                chance = 30;
            } else {
                chance = 100;
            }

            if (this.rand.nextInt(chance) == 0) {
                setfRattle(0.1F);
            }
        }
        /**
         * change in movement pattern
         */
        if (!isResting() && !pickedUp() && this.rand.nextInt(50) == 0) {
            this.movInt = this.rand.nextInt(10);
        }

        /**
         * Biting animation
         */
        if (isBiting()) {
            this.bodyswing -= 0.5F;
            setfMouth(0.3F);

            if (this.bodyswing < 0F) {
                this.worldObj.playSoundAtEntity(this, "mocreatures:snakesnap", 1.0F, 1.0F + ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F));
                this.bodyswing = 2.5F;
                setfMouth(0.0F);
                setBiting(false);
            }
        }

        /**
         * this stops chasing the target randomly
         */
        if (getAttackTarget() != null && this.rand.nextInt(100) == 0) {
            setAttackTarget(null);
        }

        /**
         * Follow player that is carrying a mice
         *
         */
        EntityPlayer entityplayer1 = this.worldObj.getClosestPlayerToEntity(this, 12D);
        if (entityplayer1 != null) {
            double distP = MoCTools.getSqDistanceTo(entityplayer1, this.posX, this.posY, this.posZ);
            if (isNotScared()) {
                if (distP < 5D) {
                    setNearPlayer(true);
                } else {
                    setNearPlayer(false);
                }

                if (entityplayer1.riddenByEntity != null
                        && (entityplayer1.riddenByEntity instanceof MoCEntityMouse || entityplayer1.riddenByEntity instanceof MoCEntityBird)) {
                    PathEntity pathentity = this.navigator.getPathToEntityLiving(entityplayer1);
                    this.navigator.setPath(pathentity, 16F);
                    setPissed(false);
                    this.hissCounter = 0;
                }
            } else {
                setNearPlayer(false);
                // TODO
                /*
                if (distP < 3D && !getIsTamed()) {
                    fleeingTick = 40;
                }*/

            }

        } else {
            setNearPlayer(false);
        }
    }

    /*@Override
    protected void attackEntity(Entity entity, float f) {

        if ((getType() < 3 || getIsTamed()) && entity instanceof EntityPlayer) {
            this.entityLivingToAttack = null;
            return;
        }

        // attack only after hissing/rattling!
        if (!isPissed()) {
            return;
        }

        if (attackTime <= 0 && (f < 2.5D) && (entity.getEntityBoundingBox().maxY > getEntityBoundingBox().minY)
                && (entity.getEntityBoundingBox().minY < getEntityBoundingBox().maxY)) {
            setBiting(true);
            attackTime = 20;

            // venom!
            if (this.rand.nextInt(2) == 0 && entity instanceof EntityPlayer && getType() > 2 && getType() < 8) {
                MoCreatures.poisonPlayer((EntityPlayer) entity);
                ((EntityPlayer) entity).addPotionEffect(new PotionEffect(Potion.poison.id, 120, 0));
            }

            entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2);

            if (!(entity instanceof EntityPlayer)) {
                MoCTools.destroyDrops(this, 3D);
            }
        }
    }*/

    @Override
    public void performAnimation(int i) {
        setBiting(true);
    }

    public boolean isBiting() {
        return this.isBiting;
    }

    public void setBiting(boolean flag) {
        if (flag && MoCreatures.isServer()) {
            MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), 0),
                    new TargetPoint(this.worldObj.provider.getDimensionId(), this.posX, this.posY, this.posZ, 64));
        }
        this.isBiting = flag;
    }

    public boolean isPissed() {
        return this.isPissed;
    }

    public void setPissed(boolean isPissed) {
        this.isPissed = isPissed;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {

        if (getType() < 3) {
            return super.attackEntityFrom(damagesource, i);
        }

        if (super.attackEntityFrom(damagesource, i)) {
            Entity entity = damagesource.getEntity();

            if ((this.riddenByEntity == entity) || (this.ridingEntity == entity)) {
                return true;
            }
            if ((entity != this) && entity instanceof EntityLivingBase && (this.worldObj.getDifficulty().getDifficultyId() > 0)) {
                setPissed(true);
                setAttackTarget((EntityLivingBase) entity);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected Entity findPlayerToAttack() {
        if (this.worldObj.getDifficulty().getDifficultyId() > 0) {
            EntityPlayer entityplayer = this.worldObj.getClosestPlayerToEntity(this, 4D);
            if (!getIsTamed() && (entityplayer != null)) // && getIsAdult() )
            {
                if (isNotScared() && isPissed()) {
                    return entityplayer;
                }
            }
            if ((this.rand.nextInt(100) == 0)) {
                EntityLivingBase entityliving = getClosestEntityLiving(this, 8D);
                return entityliving;
            }
        }
        return null;
    }

    @Override
    protected void dropFewItems(boolean flag, int x) {
        if (getEdad() > 60) {
            int j = this.rand.nextInt(3);
            for (int l = 0; l < j; l++) {

                entityDropItem(new ItemStack(MoCreatures.mocegg, 1, getType() + 20), 0.0F);
            }
        }
    }

    // ignores big entities, everything else is prey!
    @Override
    public boolean entitiesToIgnore(Entity entity) {
        return ((super.entitiesToIgnore(entity)) || (entity instanceof MoCEntitySnake) || (entity.height > 0.5D && entity.width > 0.5D));
    }

    @Override
    protected void playStepSound(BlockPos pos, Block par4) {
        if (isInsideOfMaterial(Material.water)) {
            this.worldObj.playSoundAtEntity(this, "mocreatures:snakeswim", 1.0F, 1.0F);
        }
        // TODO - add sound for slither
        /*
         * else { worldObj.playSoundAtEntity(this, "snakeslither", 1.0F, 1.0F);
         * }
         */
    }

    @Override
    protected String getDeathSound() {
        return "mocreatures:snakedying";
    }

    @Override
    protected String getHurtSound() {
        return "mocreatures:snakehurt";
    }

    @Override
    protected String getLivingSound() {
        return "mocreatures:snakehiss";
    }

    @Override
    public boolean getCanSpawnHere() {
        return (checkSpawningBiome() && getCanSpawnHereCreature() && getCanSpawnHereLiving());
    }

    @Override
    public boolean checkSpawningBiome() {
        BlockPos pos = new BlockPos(MathHelper.floor_double(this.posX), MathHelper.floor_double(getEntityBoundingBox().minY), this.posZ);

        String s = MoCTools.BiomeName(this.worldObj, pos);
        /**
         * swamp: python, bright green, #1 (done) plains: coral, cobra #1, #2,
         * #3, #4 (everyone but 7) desert: rattlesnake , #2 jungle: all except
         * rattlesnake forest: all except rattlesnake hills: all except python,
         * bright green, bright orange, rattlesnake tundra-taiga: none ocean:
         * leave alone
         */

        /**
         * Biome lists: Ocean Plains Desert Extreme Hills Forest Taiga Swampland
         * River Frozen Ocean Frozen River Ice Plains Ice Mountains Mushroom
         * Island Mushroom Island Shore Beach DesertHills ForestHills TaigaHills
         * Extreme Hills Edge Jungle JungleHills
         *
         */
        BiomeGenBase currentbiome = MoCTools.Biomekind(this.worldObj, pos);
        int l = this.rand.nextInt(10);

        if (BiomeDictionary.isBiomeOfType(currentbiome, Type.FROZEN)) {
            return false;
        }

        if (BiomeDictionary.isBiomeOfType(currentbiome, Type.DESERT)) {
            if (l < 5) {
                setType(7); // rattlesnake or spotted brownish ?
            } else {
                setType(2);
            }
        }

        if (getType() == 7 && !(BiomeDictionary.isBiomeOfType(currentbiome, Type.DESERT))) {
            return false;
        }
        if (BiomeDictionary.isBiomeOfType(currentbiome, Type.HILLS)) {
            if (l < 4) {
                setType(1);
            } else if (l < 7) {
                setType(5);
            } else {
                setType(6);
            }
        }
        if (BiomeDictionary.isBiomeOfType(currentbiome, Type.SWAMP)) {
            // python or bright green bright orange
            if (l < 4) {
                setType(8);
            } else if (l < 8) {
                setType(4);
            } else {
                setType(1);
            }
        }

        return true;
    }

    @Override
    public boolean updateMount() {
        return getIsTamed();
    }

    @Override
    public boolean forceUpdates() {
        return getIsTamed();
    }

    @Override
    public int nameYOffset() {
        return -20;
    }

    @Override
    public boolean isMyHealFood(ItemStack par1ItemStack) {
        return par1ItemStack != null && (par1ItemStack.getItem() == MoCreatures.ratRaw);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }
}