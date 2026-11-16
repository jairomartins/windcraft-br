package dev.windcraft.rendering;

import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.objectweb.asm.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WindMonkModelTest {
    @Test void villagerProportionsAndWalkingRemainRecognizable() {
        var model=new WindMonkModel();var s=new VillagerRenderState();
        s.walkAnimationSpeed=1;s.yRot=30;model.setupAnim(s);
        assertNotNull(model.root().getChild("head").getChild("nose"));
        assertNotNull(model.root().getChild("head").getChild("hat"));
        assertEquals(.5235988f,model.getHead().yRot,.0001);
        float right=model.root().getChild("right_leg").xRot;
        assertEquals(-right,model.root().getChild("left_leg").xRot,.0001);
    }
    @Test void rendererRegistrationUsesForgeBusForThisForgeVersion() throws Exception {
        // Reading the enum through reflection initializes Forge's launcher bindings,
        // which are unavailable in plain JUnit. Inspect the class metadata instead.
        var bus=new String[1];
        try(var bytes=VillageRenderers.class.getResourceAsStream("VillageRenderers.class")) {
            assertNotNull(bytes);
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public AnnotationVisitor visitAnnotation(String descriptor,boolean visible) {
                    if(!descriptor.equals("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;")) return null;
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override public void visitEnum(String name,String descriptor,String value) {
                            if(name.equals("bus")) bus[0]=value;
                        }
                    };
                }
            },ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        assertEquals("FORGE",bus[0]);
        assertFalse(net.minecraftforge.fml.event.IModBusEvent.class.isAssignableFrom(EntityRenderersEvent.RegisterRenderers.class));
        assertNotNull(VillageRenderers.class.getMethod("register",EntityRenderersEvent.RegisterRenderers.class));
    }
}
