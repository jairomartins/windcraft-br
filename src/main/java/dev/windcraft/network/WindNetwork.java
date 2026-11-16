package dev.windcraft.network;
import dev.windcraft.simulation.WindSample;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
public final class WindNetwork {
    public record Snapshot(String dimension, double speed, double direction, double atmosphericSpeed, boolean particles) {}
    /** Installed only by client setup, keeping client classes off the dedicated server. */
    public static Consumer<Snapshot> receiver = p -> {};
    public record FlightSnapshot(String dimension, boolean active, double speed, double direction, double updraft,
                                 long tick, long seed, int flightTicks, dev.windcraft.glider.GliderPhysics.Settings settings,
                                 boolean flare, double altitude, float bank) {}
    public record GliderInput(float forward, float turn, boolean flare, boolean close, long sequence) {}
    public static Consumer<FlightSnapshot> flightReceiver = p -> {};
    public record FlightVisual(int entityId, java.util.UUID player, String dimension, boolean active, float bank, String color) {}
    public static Consumer<FlightVisual> visualReceiver = p -> {};
    private static SimpleChannel channel;
    public static StreamCodec<RegistryFriendlyByteBuf, FlightSnapshot> flightCodec() {
        return new StreamCodec<>() {
            public FlightSnapshot decode(RegistryFriendlyByteBuf b) {
                return new FlightSnapshot(b.readUtf(256), b.readBoolean(), b.readDouble(), b.readDouble(), b.readDouble(), b.readLong(), b.readLong(), b.readVarInt(),
                    new dev.windcraft.glider.GliderPhysics.Settings(b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble()), b.readBoolean(), b.readDouble(), b.readFloat());
            }
            public void encode(RegistryFriendlyByteBuf b, FlightSnapshot p) {
                b.writeUtf(p.dimension, 256); b.writeBoolean(p.active); b.writeDouble(p.speed); b.writeDouble(p.direction);
                b.writeDouble(p.updraft); b.writeLong(p.tick); b.writeLong(p.seed); b.writeVarInt(p.flightTicks);
                var s = p.settings;
                b.writeDouble(s.density()); b.writeDouble(s.area()); b.writeDouble(s.lift()); b.writeDouble(s.drag());
                b.writeDouble(s.wind()); b.writeDouble(s.maxSpeed()); b.writeDouble(s.sensitivity()); b.writeDouble(s.turbulence());
                b.writeDouble(s.maxBank()); b.writeDouble(s.bankSeconds()); b.writeDouble(s.levelSeconds());
                b.writeBoolean(p.flare); b.writeDouble(p.altitude); b.writeFloat(p.bank);
            }
        };
    }
    public static StreamCodec<RegistryFriendlyByteBuf, FlightVisual> visualCodec() {
        return new StreamCodec<>() {
            public FlightVisual decode(RegistryFriendlyByteBuf b) { return new FlightVisual(b.readVarInt(), b.readUUID(), b.readUtf(256), b.readBoolean(), b.readFloat(), b.readUtf(32)); }
            public void encode(RegistryFriendlyByteBuf b, FlightVisual p) { b.writeVarInt(p.entityId); b.writeUUID(p.player); b.writeUtf(p.dimension, 256); b.writeBoolean(p.active); b.writeFloat(p.bank); b.writeUtf(p.color,32); }
        };
    }
    public static void init() {
        channel = ChannelBuilder.named("windcraft:wind").networkProtocolVersion(7).simpleChannel();
        StreamCodec<RegistryFriendlyByteBuf, Snapshot> codec = new StreamCodec<>() {
            public Snapshot decode(RegistryFriendlyByteBuf b) { return new Snapshot(b.readUtf(256), b.readDouble(), b.readDouble(), b.readDouble(), b.readBoolean()); }
            public void encode(RegistryFriendlyByteBuf b, Snapshot p) { b.writeUtf(p.dimension, 256); b.writeDouble(p.speed); b.writeDouble(p.direction); b.writeDouble(p.atmosphericSpeed); b.writeBoolean(p.particles); }
        };
        var flightCodec = flightCodec();
        StreamCodec<RegistryFriendlyByteBuf, GliderInput> inputCodec = new StreamCodec<>() {
            public GliderInput decode(RegistryFriendlyByteBuf b) { return new GliderInput(b.readFloat(), b.readFloat(), b.readBoolean(), b.readBoolean(), b.readLong()); }
            public void encode(RegistryFriendlyByteBuf b, GliderInput p) { b.writeFloat(p.forward); b.writeFloat(p.turn); b.writeBoolean(p.flare); b.writeBoolean(p.close); b.writeLong(p.sequence); }
        };
        var visualCodec = visualCodec();
        channel.protocol(NetworkProtocol.PLAY).clientbound()
            .addMain(Snapshot.class, codec, (p, c) -> receiver.accept(p))
            .addMain(FlightSnapshot.class, flightCodec, (p, c) -> flightReceiver.accept(p))
            .addMain(FlightVisual.class, visualCodec, (p, c) -> visualReceiver.accept(p));
        channel.protocol(NetworkProtocol.PLAY).serverbound().addMain(GliderInput.class, inputCodec, (p, c) -> {
            if (c.getSender() != null) dev.windcraft.glider.GliderFlightController.input(c.getSender(), p);
        }).build();
    }
    public static void send(ServerPlayer player, WindSample sample, WindSample atmosphere, boolean particles) {
        channel.send(new Snapshot(player.level().dimension().identifier().toString(), sample.speed(), sample.direction(), atmosphere.speed(), particles), PacketDistributor.PLAYER.with(player));
    }
    public static void sendInput(GliderInput input) { channel.send(input, PacketDistributor.SERVER.noArg()); }
    public static void sendFlight(ServerPlayer player, boolean active, dev.windcraft.glider.GliderPhysics.Conditions conditions, int flightTicks,
                                  dev.windcraft.glider.GliderPhysics.Settings settings, boolean flare) {
        channel.send(visual(player, active), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player));
        channel.send(new FlightSnapshot(player.level().dimension().identifier().toString(), active,
            active ? conditions.wind().speed() : 0, active ? conditions.wind().direction() : 0,
            active ? conditions.updraft() : 0, player.level().getGameTime(), active ? conditions.seed() : 0, flightTicks, settings, flare,
            player.getY() - player.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.blockPosition().getX(), player.blockPosition().getZ()), dev.windcraft.glider.GliderFlightController.bank(player)),
            PacketDistributor.PLAYER.with(player));
    }
    private static FlightVisual visual(ServerPlayer player, boolean active) {
        return new FlightVisual(player.getId(), player.getUUID(), player.level().dimension().identifier().toString(), active, dev.windcraft.glider.GliderFlightController.bank(player), dev.windcraft.glider.GliderFlightController.color(player));
    }
    public static void sendVisualTo(ServerPlayer observer, ServerPlayer pilot) {
        channel.send(visual(pilot, dev.windcraft.glider.GliderFlightController.isFlying(pilot)), PacketDistributor.PLAYER.with(observer));
    }
}
