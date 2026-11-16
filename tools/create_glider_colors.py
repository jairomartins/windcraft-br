"""Generate matching fabric atlases, pixel icons and vanilla colored crafting recipes."""
import json
from pathlib import Path
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/windcraft'
PALETTE={
 'white':(234,236,237),'orange':(229,106,32),'magenta':(189,68,179),
 'light_blue':(58,175,217),'yellow':(249,198,40),'lime':(112,185,25),
 'pink':(237,141,172),'gray':(62,68,71),'light_gray':(142,142,134),
 'cyan':(21,137,145),'purple':(121,42,172),'blue':(53,57,157),
 'brown':(114,71,40),'green':(84,109,27),'red':(161,39,34),'black':(29,29,33)}

def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf8')

def visual(color):
    return {'type':'minecraft:select','property':'minecraft:display_context',
      'cases':[{'when':['gui'],'model':{'type':'minecraft:model','model':'windcraft:item/glider/icon_'+color}}],
      'fallback':{'type':'minecraft:model','model':'windcraft:item/glider/fan_'+color}}

base=Image.open(ASSETS/'textures/entity/wind_glider.png').convert('RGBA')
cases=[]
for name,rgb in PALETTE.items():
    atlas=base.copy()
    for y in range(64,128):
        for x in range(128):
            shade=.82 if x%8==0 or y%8==0 else 1
            atlas.putpixel((x,y),tuple(round(c*shade) for c in rgb)+(255,))
    out=ASSETS/'textures/entity/glider'/f'{name}.png';out.parent.mkdir(parents=True,exist_ok=True);atlas.save(out)
    # Transparent 32px icon: semicircular cloth, wooden radial ribs and a smaller tail.
    icon=Image.new('RGBA',(32,32));draw=ImageDraw.Draw(icon)
    draw.pieslice((1,-9,30,18),0,180,fill=rgb+(255,))
    draw.pieslice((10,0,21,10),0,180,fill=(0,0,0,0))
    draw.pieslice((10,20,21,30),0,180,fill=rgb+(255,))
    wood=(111,69,36,255)
    draw.line((16,0,16,31),fill=wood,width=2)
    for end in [(2,5),(5,12),(10,17),(22,17),(27,12),(30,5)]:draw.line((16,4,*end),fill=wood)
    for end in [(11,25),(13,28),(19,28),(21,25)]:draw.line((16,24,*end),fill=wood)
    p=ASSETS/'textures/item/glider'/f'{name}.png';p.parent.mkdir(parents=True,exist_ok=True);icon.save(p)
    if name=='orange':icon.save(ASSETS/'textures/item/wind_glider.png')
    write(ASSETS/'models/item/glider'/f'icon_{name}.json',{
      'parent':'minecraft:item/generated','textures':{'layer0':'windcraft:item/glider/'+name}})
    write(ASSETS/'models/item/glider'/f'fan_{name}.json',{
      'parent':'windcraft:item/wind_glider_fan','textures':{
      'atlas':'windcraft:entity/glider/'+name,'particle':'windcraft:entity/glider/'+name}})
    recipe={'type':'minecraft:crafting_shaped','group':'windcraft:wind_glider',
      'category':'equipment','pattern':['LBL','LGL','CGC'],
      'key':{'L':'minecraft:'+name+'_wool','B':'minecraft:bamboo','G':'minecraft:stick','C':'minecraft:string'},
      'result':{'id':'windcraft:wind_glider','count':1,'components':{
      'minecraft:custom_model_data':{'strings':[name]}}}}
    filename='wind_glider' if name=='orange' else 'wind_glider_'+name
    write(ROOT/'src/main/resources/data/windcraft/recipe'/f'{filename}.json',recipe)
    cases.append({'when':name,'model':visual(name)})
write(ASSETS/'items/wind_glider.json',{'model':{'type':'minecraft:select',
  'property':'minecraft:custom_model_data','index':0,'cases':cases,'fallback':visual('orange')}})
write(ASSETS/'models/item/wind_glider_icon.json',{'parent':'minecraft:item/generated',
  'textures':{'layer0':'windcraft:item/glider/orange'}})
print('Generated 16 matching glider colors, icons, item models and same-color wool recipes.')
