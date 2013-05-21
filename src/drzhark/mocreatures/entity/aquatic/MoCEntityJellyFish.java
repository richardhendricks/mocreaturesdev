package drzhark.mocreatures.entity.aquatic;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.MoCEntityAquatic;

public class MoCEntityJellyFish extends MoCEntityAquatic {
    public float pulsingSize;
    private int poisoncounter;

    public MoCEntityJellyFish(World world)
    {
        super(world);
        setSize(0.3F, 0.5F);
        health = getMaxHealth();
        setEdad(50 + (rand.nextInt(50)));
    }

    @Override
    public void selectType()
    {
        if (getType() == 0)
        {
            int i = rand.nextInt(100);
            if (i <= 20)
            {
                setType(1);
            }
            else if (i <= 40)
            {
                setType(2);
            }
            else if (i <= 65)
            {
                setType(3);
            }
            else if (i <= 80)
            {
                setType(4);
            }
            else
            {
                setType(5);
            }
        }
    }
    
    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(22, Byte.valueOf((byte) 0)); // glow: 0 no; 1 yes
    }
    
    public void setGlowing(boolean flag)
    {
        byte input = (byte) (flag ? 1 : 0);
        dataWatcher.updateObject(22, Byte.valueOf(input));
    }

      public boolean isGlowing()
      {
          if (dataWatcher.getWatchableObjectByte(22) == 1)
          {
              EntityPlayer entityplayer = worldObj.getClosestPlayer(posX, posY, posZ, 12D);
              return (entityplayer != null);
          }
          return false;
      }

    @Override
    public int getMaxHealth()
    {
        return 6;
    }

    @Override
    public float getMoveSpeed()
    {
        return 0.1F;
    }

    @Override
    public String getTexture()
    {
        switch (getType())
        {
        case 1:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfisha.png";
        case 2:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishb.png";
        case 3:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishc.png";
        case 4:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishd.png";
        case 5:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishe.png";
        case 6:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishf.png";
        case 7:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishg.png";
        case 8:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishh.png";
        case 9:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishi.png";
        case 10:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishj.png";
        case 11:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishk.png";
        case 12:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfishl.png";

        default:
            return MoCreatures.proxy.MODEL_TEXTURE + "jellyfisha.png";
        }
    }

    @Override
    public void onLivingUpdate()
    {
        
        if (rand.nextInt(2) == 0)
        {
            pulsingSize += 0.01F;
        }
        
        if (pulsingSize > 0.4F)
        {
            pulsingSize = 0.0F;
        }
        super.onLivingUpdate();
        if (MoCreatures.isServer())
        {
            
            if(rand.nextInt(200) == 0)
            {
                setGlowing(!worldObj.isDaytime()); 
            }
            
            
            if (!getIsAdult() && (rand.nextInt(200) == 0))
            {
                setEdad(getEdad() + 1);
                if (getEdad() >= 100)
                {
                    setAdult(true);
                }
            }

            if (!getIsTamed() && ++poisoncounter > 250 && (worldObj.difficultySetting > 0)  && rand.nextInt(30) == 0)
            {
            	EntityPlayer entityplayertarget = worldObj.getClosestPlayer(posX, posY, posZ, 3D);
            	if (entityplayertarget != null)
                {
            		System.out.println("attempting poisioning" + this);
            	}
            	
            	if (MoCTools.findNearPlayerAndPoison(this, true))
                {
                    poisoncounter = 0;
                }
            }
        }
    }

    @Override
    public void floating()
    {
        float distY = MoCTools.distanceToSurface(this);

        if (motionY < -0.004)
        {
            motionY = -0.004;
        }

        if (distY > 1 && pulsingSize == 0.0F)
        {
            motionY += 0.15D;
        }
    }

    @Override
    protected int getDropItemId()
    {
        boolean flag = rand.nextInt(2) == 0;
        if (flag) return Item.slimeBall.itemID;
        return 0;
    }
    
    
  
    
    @Override
	public int pitchRotationOffset()
	{
    	if (!this.isInsideOfMaterial(Material.water))
    	{
    		return 90;
    	}
		return 0;
	}
    
    @Override
    public boolean renderName()
    {
        return getDisplayName() && (riddenByEntity == null);
    }
    
    @Override
    public int nameYOffset()
    { 	int yOff = (int) (getEdad() * -1 /2.3);
	 	return yOff;
    }
      
    @Override
	public float getAdjustedZOffset()
	{
    	if (!this.isInsideOfMaterial(Material.water))
    	{
    		return -0.6F;
    	}
		return 0F;
	}
    
    @Override
    public float getAdjustedYOffset()
    {
    	if (!this.isInsideOfMaterial(Material.water))// && this.health > 0)
    	{
    		return -0.3F;
    	}
    	return 0.4F;
    }
    
    @Override
    public float getSizeFactor() 
    {  
    	float pulseSize = 0F;
    	if (this.isInsideOfMaterial(Material.water))
    	{
    		pulseSize = this.pulsingSize;
            if (pulseSize > 0.2F)
            {
                pulseSize = 0.2F - (pulseSize - 0.2F);
            }
    	}
    	
    	return (float)getEdad() * 0.01F + (pulseSize/4);
    }
    
    @Override
    protected boolean canBeTrappedInNet() 
    {
	return true;
    }
}