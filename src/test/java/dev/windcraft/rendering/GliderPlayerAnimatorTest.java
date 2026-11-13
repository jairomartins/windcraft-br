package dev.windcraft.rendering;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GliderPlayerAnimatorTest {
    private PlayerModel model(boolean slim) {
        return new PlayerModel(LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64).bakeRoot(), slim);
    }
    @Test void classicAndSlimArmsReachTheSameBarWithoutChangingSkinParts() {
        for (boolean slim : new boolean[]{false, true}) {
            var model = model(slim); var state = new AvatarRenderState(); state.xRot = 900; state.yRot = -900;
            GliderPlayerAnimator.apply(model, state, new GliderVisualState.Frame(1, 0, 0, 15, GliderVisualState.Phase.GLIDING));
            assertEquals(2.30f, model.rightArm.xRot); assertEquals(2.30f, model.leftArm.xRot);
            assertEquals(GliderModel.BAR_Y, model.rightArm.y + 10 * Math.cos(model.rightArm.xRot), .03);
            assertEquals(GliderModel.BAR_Z, model.rightArm.z + 10 * Math.sin(model.rightArm.xRot), .03);
            assertEquals(-.04f, model.rightLeg.xRot); assertEquals(.04f, model.leftLeg.xRot);
            assertTrue(Math.abs(model.head.yRot) <= Math.toRadians(40) + 1e-6);
            assertNotNull(model.leftSleeve); assertNotNull(model.jacket);
            assertTrue(model.body.visible && model.rightArm.visible);
        }
    }
    @Test void inactivePlayerPoseIsUntouched() {
        var model = model(false); model.rightArm.xRot = .37f; model.head.yRot = .42f;
        GliderPlayerAnimator.apply(model, new AvatarRenderState(), GliderVisualState.Frame.NONE);
        assertEquals(.37f, model.rightArm.xRot); assertEquals(.42f, model.head.yRot);
    }
}
