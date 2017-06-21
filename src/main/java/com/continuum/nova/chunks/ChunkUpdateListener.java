package com.continuum.nova.chunks;

import com.continuum.nova.NovaNative;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ddubois
 */
public class ChunkUpdateListener implements IWorldEventListener {
    private static final Logger LOG = LogManager.getLogger(ChunkUpdateListener.class);

    private World world;
    private Executor executor = Executors.newFixedThreadPool(10);

    private AtomicLong timeSpentInBlockRenderUpdate = new AtomicLong(0);
    private AtomicInteger numChunksUpdated = new AtomicInteger(0);

    // private NovaNative.mc_chunk updateChunk = new NovaNative.mc_chunk();

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        LOG.debug("Update block at " + pos);
    }

    @Override
    public void notifyLightSet(BlockPos pos) {

    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        // Fire off the chunk building task
        executor.execute(() -> {
            NovaNative.mc_chunk updateChunk = new NovaNative.mc_chunk();
            long startTime = System.currentTimeMillis();
            LOG.debug("Marking blocks in range ({}, {}, {}) to ({}, {}, {}) for render update", x1, y1, z1, x2, y2, z2);

            Chunk mcChunk = world.getChunkFromBlockCoords(new BlockPos(x1, y1, z1));

            for(int x = x1; x <= x2; x++) {
                for(int y = y1; y < y2; y++) {
                    for(int z = z1; z <= z2; z++) {
                        int chunkX = x - x1;
                        int chunkY = y - y1;
                        int chunkZ = z - z1;
                        int idx = chunkX + chunkY * NovaNative.CHUNK_WIDTH + chunkZ * NovaNative.CHUNK_WIDTH * NovaNative.CHUNK_HEIGHT;

                        NovaNative.mc_block curBlock = updateChunk.blocks[idx];
                        copyBlockStateIntoMcBlock(mcChunk.getBlockState(x, y, z), curBlock);
                    }
                }
            }

            int chunkHashCode = x1;
            chunkHashCode = 31 * chunkHashCode + z1;

            updateChunk.x = x1;
            updateChunk.z = z1;
            updateChunk.chunk_id = chunkHashCode;

            NovaNative.INSTANCE.add_chunk(updateChunk);

            long deltaTime = System.currentTimeMillis() - startTime;
            timeSpentInBlockRenderUpdate.addAndGet(deltaTime);
            numChunksUpdated.incrementAndGet();

            if(numChunksUpdated.get() % 10 == 0) {
                LOG.info("It's taken an average of {}ms to update {} chunks",
                        (float)timeSpentInBlockRenderUpdate.get() / numChunksUpdated.get(), numChunksUpdated);
                LOG.info("Updating chunk in thread {}", Thread.currentThread().getId());
            }
        });
    }

    private void copyBlockStateIntoMcBlock(IBlockState blockState, NovaNative.mc_block curBlock) {
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();

        curBlock.name = block.getUnlocalizedName();
        curBlock.is_on_fire = false;
        curBlock.light_value = blockState.getLightValue();
        curBlock.light_opacity = blockState.getLightOpacity();
        curBlock.ao = blockState.getAmbientOcclusionLightValue();
        curBlock.is_opaque = material.isOpaque();
        curBlock.blocks_light = material.blocksLight();

        try {
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(blockState).getQuads(blockState, EnumFacing.UP, 0).get(0).getSprite();

            curBlock.texture_name = sprite.getIconName();
        } catch(IndexOutOfBoundsException e) {
            LOG.error("Apparently there's no up face for block {}", block.getUnlocalizedName());
        }
    }

    @Override
    public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {

    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void onEntityAdded(Entity entityIn) {

    }

    @Override
    public void onEntityRemoved(Entity entityIn) {

    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {

    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

    }
}
