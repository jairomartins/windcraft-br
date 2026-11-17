package dev.windcraft.rendering;
import dev.windcraft.simulation.WindEngine;
import dev.windcraft.simulation.WindSample;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnvironmentalMotionTest {
    @Test void unitsDirectionsAndLimits() {
        assertEquals(1, new WindSample(20,90).xPerTick(), 1e-9);
        assertEquals(-1, new WindSample(20,0).zPerTick(), 1e-9);
        assertEquals(-1, new WindSample(20,270).xPerTick(), 1e-9);
        assertEquals(Math.tan(Math.toRadians(25)), EnvironmentalMotion.slope(60,20,1,25), 1e-9);
        assertEquals(0, EnvironmentalMotion.slope(0,20,1,25));
        assertTrue(EnvironmentalMotion.slope(2,5,1,35) > EnvironmentalMotion.slope(2,20,1,25));
    }
    @Test void smoothingDoesNotOvershootAndSettles() {
        double value = 0;
        for(int i=0;i<200;i++) { value = EnvironmentalMotion.smooth(value, -20); assertTrue(value >= -20 && value <= 0); }
        assertEquals(-20,value,1e-5);
    }
    @Test void sustainedWindHysteresisAndShelterFade() {
        var storm = new EnvironmentalMotion.Storm();
        for(int i=0;i<4;i++) assertEquals(0,storm.tick(8,true,7,5,5,3,10));
        assertEquals(.1,storm.tick(8,true,7,5,5,3,10),1e-9);
        for(int i=0;i<20;i++) storm.tick(6,true,7,5,5,3,10);
        assertEquals(1,storm.tick(6,true,7,5,5,3,10),1e-9);
        assertEquals(.9,storm.tick(8,false,7,5,5,3,10),1e-9);
        for(int i=0;i<20;i++) storm.tick(8,false,7,5,5,3,10);
        assertEquals(0,storm.tick(8,false,7,5,5,3,10),1e-9);
        assertEquals(0,storm.tick(8,true,7,5,5,3,10));
    }
    @Test void defaultStartIsReachableWithoutRainOrOverrides() {
        int longest=0,run=0;
        for(long tick=0;tick<240000;tick++) {
            double speed=WindEngine.sample(123,tick,(tick%24000)/24000.0,false,false,1,1,1,5,-1,-1).speed();
            run=speed>=7?run+1:0; longest=Math.max(longest,run);
        }
        assertTrue(longest>=100,"Natural dry desert wind must sustain start threshold for five seconds");
    }
}
