package dev.windcraft.village;

import java.util.*;

/** Engine-independent module geometry. Coordinates use the floor as y=0. */
public final class VillageBlueprint {
    public enum Material { STONE, WOOD, LOG, SLAB, STAIR, FENCE, BLUE, WHITE, BEIGE, LIGHT, HANGING_LIGHT, LADDER, BED_FOOT, BED_HEAD }
    public record Cell(int x, int y, int z) {}
    private final Map<Cell, Material> blocks = new LinkedHashMap<>();
    public Map<Cell, Material> blocks() { return Collections.unmodifiableMap(blocks); }
    private void put(int x, int y, int z, Material m) { blocks.put(new Cell(x,y,z),m); }
    private void box(int x, int y, int z, int w, int h, int d, Material m) {
        for (int a=x;a<x+w;a++) for(int b=y;b<y+h;b++) for(int c=z;c<z+d;c++) put(a,b,c,m);
    }
    private void deck(int x,int z,int w,int d) { box(x,0,z,w,1,d,Material.STONE); }
    private void flag(int x,int y,int z) {
        box(x,y,z,1,5,1,Material.FENCE);
        box(x+1,y+2,z,3,2,1,Material.BLUE);
        put(x+2,y+3,z,Material.WHITE); put(x+3,y+2,z,Material.BEIGE);
    }
    private void roof(int x,int y,int z,int size) {
        for(int tier=0;tier<3;tier++) {
            int s=size-2*tier;
            box(x+tier,y+tier,z+tier,s,1,s,Material.SLAB);
            for(int a=0;a<s;a++) { put(x+tier+a,y+tier,z+tier,Material.STAIR); put(x+tier+a,y+tier,z+tier+s-1,Material.STAIR); }
        }
    }
    private void portal(int x,int z) {
        deck(x,z,13,11);
        box(x+1,1,z+4,2,7,2,Material.LOG); box(x+10,1,z+4,2,7,2,Material.LOG);
        box(x,8,z+3,13,1,4,Material.WOOD);
        box(x,9,z+2,13,1,6,Material.SLAB); box(x+1,10,z+3,11,1,4,Material.SLAB);
        put(x+3,7,z+4,Material.HANGING_LIGHT); put(x+9,7,z+4,Material.HANGING_LIGHT);
        flag(x+1,10,z+4); flag(x+10,10,z+4);
    }
    private void plaza(int x,int z) {
        deck(x,z,21,21);
        // Walkable spiral inlaid in the paving.
        int px=x+10,pz=z+10,dx=1,dz=0,length=1;
        for(int segment=0;segment<12;segment++) {
            for(int i=0;i<length;i++) { put(px,0,pz,Material.BLUE); px+=dx;pz+=dz; }
            int old=dx;dx=-dz;dz=old;if(segment%2==1)length++;
        }
        for(int a:new int[]{1,19}) for(int b:new int[]{1,19}) {
            box(x+a,1,z+b,1,3,1,Material.FENCE); put(x+a,4,z+b,Material.LIGHT);
        }
        flag(x+2,1,z+10);flag(x+17,1,z+10);
    }
    private void cell(int x,int z) {
        deck(x,z,5,5);
        for(int a=0;a<5;a++) for(int y=1;y<=3;y++) {
            put(x+a,y,z,Material.WOOD); put(x+a,y,z+4,Material.WOOD);
            put(x,y,z+a,Material.WOOD);put(x+4,y,z+a,Material.WOOD);
        }
        blocks.remove(new Cell(x+4,1,z+2));blocks.remove(new Cell(x+4,2,z+2));
        put(x+1,1,z+1,Material.BED_HEAD);put(x+1,1,z+2,Material.BED_FOOT);
        put(x+3,3,z+2,Material.HANGING_LIGHT);roof(x-1,4,z-1,7);
    }
    private void tower(int x,int z) {
        deck(x,z,7,7);
        for(int a:new int[]{0,6}) for(int b:new int[]{0,6}) box(x+a,1,z+b,1,23,1,Material.LOG);
        box(x+3,1,z+3,1,23,1,Material.STONE);
        box(x+3,1,z+4,1,24,1,Material.LADDER);
        box(x,24,z,7,1,7,Material.WOOD);put(x+3,24,z+4,Material.LADDER);
        for(int a=0;a<7;a++) { put(x+a,25,z,Material.FENCE);put(x+a,25,z+6,Material.FENCE);put(x,25,z+a,Material.FENCE); }
        // East side intentionally opens onto a broad launch deck.
        box(x+7,24,z+1,4,1,5,Material.WOOD);
        for(int a=7;a<11;a++) {put(x+a,25,z+1,Material.FENCE);put(x+a,25,z+5,Material.FENCE);}
        flag(x,25,z);put(x+3,23,z+2,Material.HANGING_LIGHT);
    }
    private void bridge(int x,int z,int w,int d,boolean eastWest) {
        deck(x,z,w,d);
        if(eastWest) for(int a=0;a<w;a++){put(x+a,1,z,Material.FENCE);put(x+a,1,z+d-1,Material.FENCE);}
        else for(int a=0;a<d;a++){put(x,1,z+a,Material.FENCE);put(x+w-1,1,z+a,Material.FENCE);}
    }
    public static VillageBlueprint create(String module) {
        var b=new VillageBlueprint();
        switch(module) {
            case "portal" -> b.portal(0,0);
            case "plaza" -> b.plaza(0,0);
            case "cell" -> b.cell(0,0);
            case "tower" -> b.tower(0,0);
            case "foundation" -> {
                b.plaza(0,0);b.portal(4,26);b.bridge(8,21,5,5,false);
                b.cell(-10,8);b.bridge(-5,8,5,5,true);
                b.tower(27,7);b.bridge(21,8,6,5,true);
            }
            default -> throw new IllegalArgumentException("Unknown village module: "+module);
        }
        return b;
    }
}
