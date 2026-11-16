"""Original voxel asset source. Regenerates matching Blockbench and Java geometry.
No downloaded models, image generation, external packages or runtime dependencies.
"""
import base64, json, math, random, struct, uuid, zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSET = ROOT / 'src/main/resources/assets/windcraft'
OUT = ROOT / 'docs/models'
OUT.mkdir(parents=True, exist_ok=True)
groups = []

def group(name, pivot, cubes, parent=None):
    groups.append(dict(name=name, pivot=pivot, cubes=cubes, parent=parent))

# Cube tuples: local x,y,z,width,height,depth,atlas u,v. Java coordinates.
group('body', (0,3,1), [(-11,-9,-12,22,18,29,0,0)])
group('hump', (0,0,-8), [(-12,-12,-9,24,14,19,0,0)])
group('head', (0,1,-14), [(-9,-7,-12,18,17,14,0,0),(-10,-3,-8,20,12,8,0,0)])
group('muzzle', (0,5,-24), [(-7,-3,-4,14,7,7,0,84)], 'head')
group('beard', (0,10,-19), [(-5,0,-3,10,8,6,0,0),(-3,7,-2,6,3,4,0,0)], 'head')
for side,sign in [('left',1),('right',-1)]:
    group('horn_'+side,(sign*9,-2,-20),[(0 if sign>0 else -6,-2,-2,6,4,4,64,84),(sign*5-1,-7,-1.5,2,7,3,64,84),(sign*6-1,-10,-1,2,4,2,64,84)],'head')
for name,x,z in [('front_leg_left',8,-8),('front_leg_right',-8,-8),('back_leg_left',7,13),('back_leg_right',-7,13)]:
    group(name,(x,12,z),[(-3,0,-3,6,10,6,0,0),(-3,9,-3,6,3,6,0,100)])
group('tail',(0,3,18),[(-1,0,0,2,10,2,0,0),(-2,8,-1,4,4,4,0,100)])
group('ceremonial_cloth',(0,0,0),[(-12.5,-10,-1,25,1,18,0,49),(-12.5,-9,-1,1,13,18,0,49),(11.5,-9,-1,1,13,18,0,49)])
group('harness',(0,0,0),[(-12.8,-5,-13,25.6,2,2,0,110),(-12.8,-4,-13,2,15,2,0,110),(10.8,-4,-13,2,15,2,0,110),(-12.8,5,0,1,1,16,0,110),(11.8,5,0,1,1,16,0,110)])
for side,x in [('left',-12),('right',12)]:
    group('fringe_'+side,(x,4,0),[(-.6,0,z,1.2,4,1,0,112) for z in (1,5,9,13)],'ceremonial_cloth')
    group('horn_ornament_'+side,((-12 if side=='left' else 12),-2,-20),[(-1.5,-2.2,-2.2,3,4.4,4.4,0,112)],'head')
# Eyes are separate editable cubes, both visible from the sides.
group('eyes',(0,0,0),[(-9.3,-1,-24,.6,2,2,40,100),(8.7,-1,-24,.6,2,2,40,100)],'head')

# Deterministic pixel texture, coherent atlas zones shared by like materials.
rng=random.Random(7181)
pixels=[]
for y in range(128):
    row=[]
    for x in range(128):
        if y>=110: color=(192,171,129)
        elif y<49: color=(230,233,223)
        elif y<84: color=(104,176,206)
        elif y<100: color=(55,49,43) if x<60 else (65,69,69)
        elif x<32: color=(53,59,61)
        elif x<48: color=(24,28,28)
        else: color=(234,234,221)
        noise=rng.choice([-10,-5,0,0,0,5,8]);row.append(tuple(max(0,min(255,c+noise)) for c in color)+(255,))
    pixels.append(row)
# Beige border and a spiral repeated across cloth UV faces.
for y in (49,50,81,82,83):
    for x in range(95): pixels[y][x]=(205,185,145,255)
spiral=[(0,0),(1,0),(1,1),(0,1),(-1,1),(-1,0),(-1,-1),(0,-1),(1,-1),(2,-1),(2,0),(2,1),(2,2),(1,2),(0,2),(-1,2),(-2,2),(-2,1),(-2,0),(-2,-1),(-2,-2)]
for cx in (9,27,48,72):
    for cy in (60,74):
        for x,y in spiral:pixels[cy+y][cx+x]=(245,238,212,255)
# Nostrils on the muzzle's front atlas face.
for x in (9,17):
    for y in (92,93):pixels[y][x]=(22,23,24,255)
def chunk(kind,data):return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data)&0xffffffff)
raw=b''.join(b'\0'+bytes(c for px in row for c in px) for row in pixels)
png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>2I5B',128,128,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')
texture=ASSET/'textures/entity/white_wind_bison.png';texture.parent.mkdir(parents=True,exist_ok=True);texture.write_bytes(png)
(OUT/'white_wind_bison.png').write_bytes(png)

def uid(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'windcraft:bison:'+name))
byname={g['name']:g for g in groups}
elements=[]
def outline(g):
    px,py,pz=g['pivot'];children=[]
    for i,c in enumerate(g['cubes']):
        x,y,z,w,h,d,u,v=c;eid=uid(g['name']+str(i));children.append(eid)
        rects={'east':[u,v+d,u+d,v+d+h],'north':[u+d,v+d,u+d+w,v+d+h],
               'west':[u+d+w,v+d,u+2*d+w,v+d+h],'south':[u+2*d+w,v+d,u+2*d+2*w,v+d+h],
               'up':[u+d,v,u+d+w,v+d],'down':[u+d+w,v+d,u+d+2*w,v]}
        elements.append(dict(name=g['name']+'_'+str(i),type='cube',uuid=eid,box_uv=True,uv_offset=[u,v],
            **{'from':[px+x,24-(py+y+h),pz+z],'to':[px+x+w,24-(py+y),pz+z+d]},
            origin=[px,24-py,pz],faces={f:dict(uv=r,texture=0) for f,r in rects.items()}))
    children += [outline(ch) for ch in groups if ch['parent']==g['name']]
    return dict(name=g['name'],uuid=uid(g['name']),origin=[px,24-py,pz],rotation=[0,0,0],children=children,export=True,isOpen=True)
outliner=[outline(g) for g in groups if not g['parent']]
animations=[]
for name,length in [('idle',4),('walk',1)]:
    animators={}
    names=['head','tail'] if name=='idle' else ['front_leg_left','front_leg_right','back_leg_left','back_leg_right']
    for n in names:
        phase=1 if n in ('front_leg_left','back_leg_right') else -1
        values=[0,2,0,-2,0] if name=='idle' else [25*phase,0,-25*phase,0,25*phase]
        keys=[dict(channel='rotation',data_points=[dict(x=str(value),y='0',z='0')],uuid=uid(name+n+str(i)),time=i*length/4,interpolation='linear') for i,value in enumerate(values)]
        animators[uid(n)]=dict(name=n,type='bone',keyframes=keys)
    animations.append(dict(uuid=uid(name),name=name,loop='loop',length=length,animators=animators))
model=dict(meta=dict(format_version='4.10',model_format='modded_entity',box_uv=True),name='White Wind Bison',model_identifier='white_wind_bison',resolution=dict(width=128,height=128),elements=elements,outliner=outliner,textures=[dict(name='white_wind_bison.png',uuid=uid('texture'),id='0',width=128,height=128,uv_width=128,uv_height=128,source='data:image/png;base64,'+base64.b64encode(png).decode())],animations=animations)
(OUT/'white_wind_bison.bbmodel').write_text(json.dumps(model,indent=2),encoding='utf8')

# Match the editable project's absolute pivots using relative child poses in Java.
lines=['package dev.windcraft.rendering;','', 'import net.minecraft.client.model.EntityModel;', 'import net.minecraft.client.model.geom.PartPose;', 'import net.minecraft.client.model.geom.ModelPart;', 'import net.minecraft.client.model.geom.builders.*;', 'import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;', '', '/** Generated by tools/create_bison.py from the same geometry as the editable project. */', 'public final class WhiteWindBisonModel extends EntityModel<LivingEntityRenderState> {','    public WhiteWindBisonModel() { super(createLayer().bakeRoot()); }','    public static LayerDefinition createLayer() {','        var mesh = new MeshDefinition();','        var root = mesh.getRoot();']
def f(n):return str(float(n))+'f'
for g in groups:
    p=byname[g['parent']]['pivot'] if g['parent'] else (0,0,0)
    delta=[a-b for a,b in zip(g['pivot'],p)]
    lines.append('        var '+g['name']+' = '+(g['parent'] or 'root')+'.addOrReplaceChild("'+g['name']+'", CubeListBuilder.create()')
    for c in g['cubes']:
        x,y,z,w,h,d,u,v=c;lines.append('            .texOffs('+str(u)+','+str(v)+').addBox('+','.join(map(f,(x,y,z,w,h,d)))+')')
    lines.append('            , PartPose.offset('+','.join(map(f,delta))+'));')
lines+=['        return LayerDefinition.create(mesh,128,128);','    }','    @Override public void setupAnim(LivingEntityRenderState s) {','        super.setupAnim(s);','        BisonAnimations.apply(root, s);','    }','}']
(ROOT/'src/main/java/dev/windcraft/rendering/WhiteWindBisonModel.java').write_text('\n'.join(lines)+'\n',encoding='utf8')
print(f'Created {len(elements)} cubes, {len(groups)} groups, embedded 128x128 atlas, idle/walk previews and matching Java model.')
