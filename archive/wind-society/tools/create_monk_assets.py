"""Create the original Java-model UV atlas and spawn egg icon; Python stdlib only."""
import json,random,struct,zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/windcraft'
def png(path,w,h,pixels):
    def ck(t,b):return struct.pack('>I',len(b))+t+b+struct.pack('>I',zlib.crc32(t+b)&0xffffffff)
    raw=b''.join(b'\0'+bytes(c for pixel in row for c in pixel) for row in pixels)
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(b'\x89PNG\r\n\x1a\n'+ck(b'IHDR',struct.pack('>2I5B',w,h,8,6,0,0,0))+ck(b'IDAT',zlib.compress(raw))+ck(b'IEND',b''))
rng=random.Random(620)
brown=(66,45,34);skin=(181,139,104);blue=(104,178,207);beige=(207,185,144)
gold=(175,139,67);rope=(151,117,73);boot=(35,31,29)
pixels=[[tuple(max(0,min(255,c+rng.choice((-3,0,0,3)))) for c in brown)+(255,) for x in range(128)] for y in range(64)]

def rect(x,y,w,h,color):
    assert 0<=x<x+w<=128 and 0<=y<y+h<=64
    for yy in range(y,y+h):
        for xx in range(x,x+w):pixels[yy][xx]=color+(255,)
# Head box (0,0), 8x10x8: front U8..15,V8..17; nose separate U24,V0.
rect(0,0,32,18,skin)
rect(8,11,8,1,(69,49,35))
for x in (9,13):
    rect(x,12,2,2,(224,220,191));rect(x,12,1,2,(58,110,53))
rect(11,16,2,1,(114,74,51))
rect(24,0,8,6,skin)
rect(26,3,2,1,(154,111,80))
# Leg box U0,V22,4x12x4: boots occupy the lowest five pixels on all sides.
rect(0,33,16,5,boot)
rect(0,32,16,1,beige)
# Torso U16,V20 and long outer robe U0,V38; no player-skin UVs.
rect(16,20,28,18,brown)
rect(0,38,28,26,brown)
# Front/back panels at U6..13/U20..27, V44..63.
for u in (6,20):
    rect(u,44,1,20,beige);rect(u+7,44,1,20,beige)
    rect(u+1,44,1,20,blue)
    rect(u,61,8,2,beige);rect(u,63,8,1,gold)
# Rope wraps all four vertical faces, with a knot and hanging ends at front.
rect(0,54,28,2,rope)
for x,y in [(10,54),(11,54),(10,55),(12,56),(12,57),(11,58),(10,57)]:rect(x,y,1,1,beige)
# Crossed sleeves use U44,V22; folded forearms U40,V38.
rect(44,22,16,16,brown)
rect(44,33,16,1,gold);rect(44,34,16,2,blue)
rect(40,38,24,8,brown)
rect(44,42,3,4,beige);rect(53,42,3,4,beige)
rect(47,42,2,4,skin);rect(51,42,2,4,skin)
# Hood uses its own atlas island; trim never overwrites the face or nose.
rect(64,0,40,20,(58,39,30))
rect(73,9,9,1,beige)
# Original wind spirals on visible lower chest and back.
spiral=[(0,0),(1,0),(1,1),(0,1),(-1,1),(-1,0),(-1,-1),(0,-1),(1,-1),(2,-1),(2,0),(2,1),(2,2),(1,2),(0,2),(-1,2),(-2,2)]
for cx,cy in [(10,51),(24,49)]:
    for x,y in spiral:rect(cx+x,cy+y,1,1,beige)
rect(13,47,1,1,gold);rect(27,45,1,1,gold)
png(ASSETS/'textures/entity/wind_monk.png',128,64,pixels)
egg=[]
for y in range(16):
    row=[]
    for x in range(16):
        inside=((x-7.5)/(4.4+(y-7)*.12))**2+((y-8)/6.5)**2<1
        color=brown if y<9 else blue
        if (x*3+y*5)%13<3:color=beige
        row.append(color+(255,) if inside else (0,0,0,0))
    egg.append(row)
png(ASSETS/'textures/item/wind_monk_spawn_egg.png',16,16,egg)
for lang,names in {'pt_br':('Monge do Vento','Ovo de invoca\u00e7\u00e3o de Monge do Vento'),'en_us':('Wind Monk','Wind Monk Spawn Egg')}.items():
    p=ASSETS/'lang'/(lang+'.json');d=json.loads(p.read_text(encoding='utf-8-sig'))
    d.update({'entity.windcraft.wind_monk':names[0],'item.windcraft.wind_monk_spawn_egg':names[1]})
    p.write_bytes((json.dumps(d,ensure_ascii=False,indent=4,separators=(',',':  '))+'\n').encode('utf8'))
print('Created original 128x64 monk atlas, 16px egg icon and translations.')
