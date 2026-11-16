package dev.windcraft.glider;

import dev.windcraft.simulation.WindSample;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GliderBankTest {
    @Test void reachesLimitInEightTicksAndLevelsInEleven() {
        float bank=0;
        for(int i=0;i<8;i++) bank=GliderPhysics.updateBank(bank,1,GliderPhysics.DEFAULT);
        assertEquals(25,bank,.0001);
        bank=GliderPhysics.updateBank(bank,0,GliderPhysics.DEFAULT);
        assertTrue(bank>20 && bank<25);
        for(int i=0;i<10;i++) bank=GliderPhysics.updateBank(bank,0,GliderPhysics.DEFAULT);
        assertEquals(0,bank,.0001);
    }
    @Test void reverseInputAndInvalidInputRemainBounded() {
        float bank=25;
        for(int i=0;i<16;i++) bank=GliderPhysics.updateBank(bank,-1,GliderPhysics.DEFAULT);
        assertEquals(-25,bank,.0001);
        assertEquals(0,GliderPhysics.updateBank(Float.NaN,1,GliderPhysics.DEFAULT));
    }
    @Test void actualRollCurvesVelocityAndReducesVerticalLift() {
        var v=new GliderPhysics.Velocity(0,-.15,.6);
        var c=new GliderPhysics.Conditions(new WindSample(0,0),0,0,1);
        var flat=GliderPhysics.step(v,0,0,GliderPhysics.Controls.IDLE,c,GliderPhysics.DEFAULT,0);
        var right=GliderPhysics.step(v,0,0,GliderPhysics.Controls.IDLE,c,GliderPhysics.DEFAULT,25);
        var left=GliderPhysics.step(v,0,0,GliderPhysics.Controls.IDLE,c,GliderPhysics.DEFAULT,-25);
        assertTrue(right.x()<0 && left.x()>0);
        assertEquals(-right.x(),left.x(),1e-10);
        assertTrue(right.y()<flat.y());
        // Camera yaw does not pull an A/D turn back toward the old view direction.
        assertEquals(right,GliderPhysics.step(v,90,0,GliderPhysics.Controls.IDLE,c,GliderPhysics.DEFAULT,25));
    }
    @Test void prolongedBankedGlideCannotCreateEnergyOrInfiniteSpeed() {
        var v=new GliderPhysics.Velocity(0,-.1,.6);
        double height=0;
        for(int tick=0;tick<10000;tick++) {
            v=GliderPhysics.step(v,0,0,GliderPhysics.Controls.IDLE,
                new GliderPhysics.Conditions(new WindSample(0,0),0,tick,1),GliderPhysics.DEFAULT,25);
            height+=v.y();
            assertTrue(Double.isFinite(v.x()) && Double.isFinite(v.y()) && Double.isFinite(v.z()));
            assertTrue(Math.hypot(v.x(),v.z())<=1.1);
        }
        assertTrue(height < -100);
    }
}
