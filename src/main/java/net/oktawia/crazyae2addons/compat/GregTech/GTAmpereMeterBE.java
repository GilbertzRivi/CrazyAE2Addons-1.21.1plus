package net.oktawia.crazyae2addons.compat.GregTech;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.entities.AmpereMeterBE;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class GTAmpereMeterBE extends AmpereMeterBE {
    private long lastTick = -1;
    private int tickAmps = 0;
    private long tickVolt = 0;

    public GTAmpereMeterBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.GT_AMPERE_METER_BE.get(), pos, blockState);
    }

    @Nullable
    private IEnergyContainer getNeighborEnergyContainer(Direction mySide) {
        Level lvl = getLevel();
        if (lvl == null) return null;

        BlockPos nPos = getBlockPos().relative(mySide);
        if (!lvl.isLoaded(nPos)) return null;

        BlockEntity nBe = lvl.getBlockEntity(nPos);
        Direction neighborFace = mySide.getOpposite();

        return GTCapability.CAPABILITY_ENERGY_CONTAINER.getCapability(getLevel(), nBe.getBlockPos(), nBe.getBlockState(), nBe, neighborFace);
    }

    public final IEnergyContainer euLogic = new IEnergyContainer() {
        @Override
        public long acceptEnergyFromNetwork(Direction side, long volt, long amp) {
            Direction outputSide = !GTAmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());

            Direction inputSide = GTAmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());

            if (side != inputSide) {
                return 0;
            }

            IEnergyContainer output = getNeighborEnergyContainer(outputSide);
            if (output == null) return 0;

            AtomicLong transferred = new AtomicLong();
            transferred.set(output.acceptEnergyFromNetwork(outputSide.getOpposite(), volt, amp));

            long currentTick = GTAmpereMeterBE.this.getLevel().getGameTime();
            int transferredAmps = (int) transferred.get();
            if (currentTick == GTAmpereMeterBE.this.lastTick) {
                GTAmpereMeterBE.this.tickAmps += transferredAmps;
                if (volt > GTAmpereMeterBE.this.tickVolt) {
                    GTAmpereMeterBE.this.tickVolt = volt;
                }
            } else {
                GTAmpereMeterBE.this.lastTick = currentTick;
                GTAmpereMeterBE.this.tickAmps = transferredAmps;
                GTAmpereMeterBE.this.tickVolt = volt;
            }
            Map.Entry<Long, String> voltageTier = Utils.voltagesMap.ceilingEntry(GTAmpereMeterBE.this.tickVolt);
            String tierName = voltageTier != null ? voltageTier.getValue() : "???";
            String unitLabel = "A (%s)".formatted(tierName);
            if (!Objects.equals(GTAmpereMeterBE.this.unit, unitLabel)) {
                GTAmpereMeterBE.this.maxTrans.clear();
                GTAmpereMeterBE.this.unit = unitLabel;
            }
            GTAmpereMeterBE.this.maxTrans.put(GTAmpereMeterBE.this.maxTrans.size(), GTAmpereMeterBE.this.tickAmps);
            if (GTAmpereMeterBE.this.maxTrans.size() > 5) {
                GTAmpereMeterBE.this.maxTrans.remove(0);
                HashMap<Integer, Integer> newMap = new HashMap<>();
                int i = 0;
                for (int value : GTAmpereMeterBE.this.maxTrans.values()) {
                    newMap.put(i++, value);
                }
                GTAmpereMeterBE.this.maxTrans = newMap;
            }
            int max = GTAmpereMeterBE.this.maxTrans.values().stream().max(Integer::compare).orElse(0);
            GTAmpereMeterBE.this.transfer = Utils.shortenNumber(max);
            GTAmpereMeterBE.this.numTransfer = max;
            if (GTAmpereMeterBE.this.getMenu() != null) {
                GTAmpereMeterBE.this.getMenu().unit = GTAmpereMeterBE.this.unit;
                GTAmpereMeterBE.this.getMenu().transfer = GTAmpereMeterBE.this.transfer;
                GTAmpereMeterBE.this.getMenu().broadcastChanges();
            }
            return transferred.get();
        }

        @Override public boolean inputsEnergy(Direction direction) {
            Direction inputSide = GTAmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());
            return direction == inputSide;
        }

        @Override public boolean outputsEnergy(Direction direction) {
            Direction outputSide = !GTAmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());
            return direction == outputSide;
        }

        @Override public long changeEnergy( long l ) { return 0; }
        @Override public long getEnergyStored() { return 0; }
        @Override public long getEnergyCapacity() { return Long.MAX_VALUE; }

        @Override public long getInputAmperage() {
            Direction outputSide = !GTAmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            IEnergyContainer output = getNeighborEnergyContainer(outputSide);
            return output != null ? output.getInputAmperage() : 1;
        }
        @Override public long getInputVoltage() {
            Direction outputSide = !GTAmpereMeterBE.this.direction ? Utils.getRightDirection(getBlockState()) : Utils.getLeftDirection(getBlockState());
            IEnergyContainer output = getNeighborEnergyContainer(outputSide);
            return output != null ? output.getInputVoltage() : Long.MAX_VALUE;
        }
    };
}