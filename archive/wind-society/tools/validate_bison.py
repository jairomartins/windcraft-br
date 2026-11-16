"""Structural asset validation and an orthographic geometry preview (Pillow for preview)."""
import base64, json, math
from pathlib import Path
from PIL import Image, ImageDraw

root=Path(__file__).resolve().parents[1]
d=json.loads((root/'docs/models/white_wind_bison.bbmodel').read_text())
elements=d['elements']; ids={e['uuid'] for e in elements}; groups={}
def visit(items):
    for item in items:
        if isinstance(item,str): assert item in ids
        else:
            groups[item['name']]=item
            visit(item['children'])
visit(d['outliner'])
required='body hump head muzzle beard horn_left horn_right front_leg_left front_leg_right back_leg_left back_leg_right tail ceremonial_cloth harness'.split()
assert all(n in groups for n in required)
assert len(ids)==len(elements)
png=base64.b64decode(d['textures'][0]['source'].split(',')[1])
assert png==(root/'src/main/resources/assets/windcraft/textures/entity/white_wind_bison.png').read_bytes()
for e in elements:
    assert all(b>a for a,b in zip(e['from'],e['to'])),e['name']
    for f in e['faces'].values():assert all(0<=v<=128 for v in f['uv']),(e['name'],f)
assert {a['name'] for a in d['animations']}=={'idle','walk'}

texture=Image.open(root/'docs/models/white_wind_bison.png').convert('RGB')
faces=[]
for e in elements:
    x,y,z=e['from'];X,Y,Z=e['to']
    for name,pts,shade in [('north',[(x,y,z),(X,y,z),(X,Y,z),(x,Y,z)],.88),
                          ('east',[(X,y,z),(X,y,Z),(X,Y,Z),(X,Y,z)],.72),
                          ('up',[(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)],1)]:
        u,v,U,V=e['faces'][name]['uv']; region=texture.crop((math.floor(min(u,U)),math.floor(min(v,V)),max(math.ceil(max(u,U)),math.floor(min(u,U))+1),max(math.ceil(max(v,V)),math.floor(min(v,V))+1)))
        color=region.resize((1,1)).getpixel((0,0));color=tuple(int(c*shade) for c in color)
        faces.append((sum(a*.8+b*.65-c for a,b,c in pts)/4,pts,color))
im=Image.new('RGB',(1000,850),(28,36,44));draw=ImageDraw.Draw(im)
def project(p):
    x,y,z=p;return (500+(x+z)*10,570-y*11+(x-z)*4)
for _,pts,color in sorted(faces):draw.polygon([project(p) for p in pts],fill=color,outline=tuple(max(0,c-15) for c in color))
draw.text((25,25),'WINDCRAFT | BISON - GEOMETRY AND ATLAS PREVIEW',fill=(220,230,235))
im.save(root/'docs/models/white_wind_bison_preview.png')
print(f'PASS: {len(elements)} cubes, {len(groups)} editable groups, all UV faces within 128px, matching embedded texture, idle/walk keyframes.')
