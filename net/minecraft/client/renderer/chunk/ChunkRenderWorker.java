/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumWorldBlockLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkRenderWorker
implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ChunkRenderDispatcher chunkRenderDispatcher;
    private final RegionRenderCacheBuilder regionRenderCacheBuilder;

    public ChunkRenderWorker(ChunkRenderDispatcher p_i46201_1_) {
        this(p_i46201_1_, null);
    }

    public ChunkRenderWorker(ChunkRenderDispatcher chunkRenderDispatcherIn, RegionRenderCacheBuilder regionRenderCacheBuilderIn) {
        this.chunkRenderDispatcher = chunkRenderDispatcherIn;
        this.regionRenderCacheBuilder = regionRenderCacheBuilderIn;
    }

    @Override
    public void run() {
        try {
            while (true) {
                this.processTask(this.chunkRenderDispatcher.getNextChunkUpdate());
            }
        } catch (InterruptedException var3) {
            LOGGER.debug("Stopping due to interrupt");
            return;
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Batching chunks");
            Minecraft.getMinecraft().crashed(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void processTask(final ChunkCompileTaskGenerator generator) throws InterruptedException {
        generator.getLock().lock();
        try {
            if (generator.getStatus() != ChunkCompileTaskGenerator.Status.PENDING) {
                if (!generator.isFinished()) {
                    LOGGER.warn("Chunk render task was " + (Object)((Object)generator.getStatus()) + " when I expected it to be pending; ignoring task");
                }
                return;
            }
            generator.setStatus(ChunkCompileTaskGenerator.Status.COMPILING);
        } finally {
            generator.getLock().unlock();
        }
        Entity lvt_2_1_ = Minecraft.getMinecraft().getRenderViewEntity();
        if (lvt_2_1_ == null) {
            generator.finish();
        } else {
            generator.setRegionRenderCacheBuilder(this.getRegionRenderCacheBuilder());
            float f = (float)lvt_2_1_.posX;
            float f1 = (float)lvt_2_1_.posY + lvt_2_1_.getEyeHeight();
            float f2 = (float)lvt_2_1_.posZ;
            ChunkCompileTaskGenerator.Type chunkcompiletaskgenerator$type = generator.getType();
            if (chunkcompiletaskgenerator$type == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK) {
                generator.getRenderChunk().rebuildChunk(f, f1, f2, generator);
            } else if (chunkcompiletaskgenerator$type == ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY) {
                generator.getRenderChunk().resortTransparency(f, f1, f2, generator);
            }
            generator.getLock().lock();
            try {
                if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING) {
                    if (!generator.isFinished()) {
                        LOGGER.warn("Chunk render task was " + (Object)((Object)generator.getStatus()) + " when I expected it to be compiling; aborting task");
                    }
                    this.freeRenderBuilder(generator);
                    return;
                }
                generator.setStatus(ChunkCompileTaskGenerator.Status.UPLOADING);
            } finally {
                generator.getLock().unlock();
            }
            final CompiledChunk lvt_7_1_ = generator.getCompiledChunk();
            ArrayList<ListenableFuture<Object>> lvt_8_1_ = Lists.newArrayList();
            if (chunkcompiletaskgenerator$type == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK) {
                for (EnumWorldBlockLayer enumworldblocklayer : EnumWorldBlockLayer.values()) {
                    if (!lvt_7_1_.isLayerStarted(enumworldblocklayer)) continue;
                    lvt_8_1_.add(this.chunkRenderDispatcher.uploadChunk(enumworldblocklayer, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(enumworldblocklayer), generator.getRenderChunk(), lvt_7_1_));
                }
            } else if (chunkcompiletaskgenerator$type == ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY) {
                lvt_8_1_.add(this.chunkRenderDispatcher.uploadChunk(EnumWorldBlockLayer.TRANSLUCENT, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT), generator.getRenderChunk(), lvt_7_1_));
            }
            final ListenableFuture listenablefuture = Futures.allAsList(lvt_8_1_);
            generator.addFinishRunnable(new Runnable(){

                @Override
                public void run() {
                    listenablefuture.cancel(false);
                }
            });
            Futures.addCallback(listenablefuture, new FutureCallback<List<Object>>(){

                @Override
                public void onSuccess(List<Object> p_onSuccess_1_) {
                    block6: {
                        ChunkRenderWorker.this.freeRenderBuilder(generator);
                        generator.getLock().lock();
                        try {
                            if (generator.getStatus() == ChunkCompileTaskGenerator.Status.UPLOADING) {
                                generator.setStatus(ChunkCompileTaskGenerator.Status.DONE);
                                break block6;
                            }
                            if (!generator.isFinished()) {
                                LOGGER.warn("Chunk render task was " + (Object)((Object)generator.getStatus()) + " when I expected it to be uploading; aborting task");
                            }
                        } finally {
                            generator.getLock().unlock();
                        }
                        return;
                    }
                    generator.getRenderChunk().setCompiledChunk(lvt_7_1_);
                }

                @Override
                public void onFailure(Throwable p_onFailure_1_) {
                    ChunkRenderWorker.this.freeRenderBuilder(generator);
                    if (!(p_onFailure_1_ instanceof CancellationException) && !(p_onFailure_1_ instanceof InterruptedException)) {
                        Minecraft.getMinecraft().crashed(CrashReport.makeCrashReport(p_onFailure_1_, "Rendering chunk"));
                    }
                }
            });
        }
    }

    private RegionRenderCacheBuilder getRegionRenderCacheBuilder() throws InterruptedException {
        return this.regionRenderCacheBuilder != null ? this.regionRenderCacheBuilder : this.chunkRenderDispatcher.allocateRenderBuilder();
    }

    private void freeRenderBuilder(ChunkCompileTaskGenerator taskGenerator) {
        if (this.regionRenderCacheBuilder == null) {
            this.chunkRenderDispatcher.freeRenderBuilder(taskGenerator.getRegionRenderCacheBuilder());
        }
    }
}

