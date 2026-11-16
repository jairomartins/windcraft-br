package dev.windcraft.village;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static dev.windcraft.village.VillageBlueprint.Material.*;
import dev.windcraft.village.VillageBlueprint.Cell;

class VillageBlueprintTest {
    @Test void entrancesAndTowerCanBeReachedFromPortal() {
        var b=VillageBlueprint.create("foundation").blocks();
        var seen=new HashSet<Cell>();var queue=new ArrayDeque<Cell>();
        queue.add(new Cell(10,1,36));
        while(!queue.isEmpty()) {
            var p=queue.remove();if(!seen.add(p))continue;
            for(int[] d:new int[][]{{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}}) {
                var q=new Cell(p.x()+d[0],p.y()+d[1],p.z()+d[2]);
                if(q.x()<-12||q.x()>40||q.z()<-2||q.z()>38||q.y()<1||q.y()>25||seen.contains(q))continue;
                if(d[1]!=0 && b.get(p)!=LADDER && b.get(q)!=LADDER)continue;
                var foot=b.get(q);var head=b.get(new Cell(q.x(),q.y()+1,q.z()));
                var floor=b.get(new Cell(q.x(),q.y()-1,q.z()));
                if((foot==null||foot==LADDER)&&(head==null||head==LADDER)&&(floor!=null||foot==LADDER))queue.add(q);
            }
        }
        assertTrue(seen.contains(new Cell(10,1,10)),"plaza");
        assertTrue(seen.contains(new Cell(-8,1,11)),"cell interior");
        assertTrue(seen.contains(new Cell(30,25,11)),"ladder exit");
        assertTrue(seen.contains(new Cell(37,25,10)),"launch edge");
    }
    @Test void launchHasWingClearanceAndLadderBacking() {
        var b=VillageBlueprint.create("tower").blocks();
        for(int y=1;y<24;y++)assertNotNull(b.get(new Cell(3,y,3)));
        for(int x=7;x<=10;x++)for(int z=2;z<=4;z++) {
            assertEquals(WOOD,b.get(new Cell(x,24,z)));
            for(int y=25;y<=28;y++)assertNull(b.get(new Cell(x,y,z)));
        }
    }
    @Test void allHangingLanternsHaveCeiling() {
        var b=VillageBlueprint.create("foundation").blocks();
        b.forEach((p,m)->{if(m==HANGING_LIGHT)assertNotNull(b.get(new Cell(p.x(),p.y()+1,p.z())));});
    }
}
