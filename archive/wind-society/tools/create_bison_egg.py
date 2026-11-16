"""Original 16px ivory bison egg with dark horns and a blue wind band."""
import json
from pathlib import Path
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/windcraft'
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
d.polygon([(6,1),(9,1),(11,3),(13,7),(13,11),(11,14),(4,14),(2,11),(2,7),(4,3)],fill='#555b5e')
d.polygon([(6,2),(9,2),(11,5),(12,8),(12,11),(10,13),(5,13),(3,11),(3,8),(5,4)],fill='#eee9d6')
d.line([(4,10),(6,11),(10,11),(12,10)],fill='#82cbe2',width=2)
# Symmetric upward horns frame an ivory head, charcoal muzzle and eyes.
d.line([(3,4),(3,6),(5,7),(10,7),(12,6),(12,4)],fill='#363e42',width=1)
d.rectangle((5,5,10,8),fill='#fffae8');d.rectangle((6,8,9,9),fill='#555b5e')
d.point((5,7),fill='#252d31');d.point((10,7),fill='#252d31')
d.point((6,3),fill='#ffffff');d.point((11,12),fill='#aaa998')
im.save(ASSETS/'textures/item/white_wind_bison_spawn_egg.png')
p=ASSETS/'lang/pt_br.json';data=json.loads(p.read_text(encoding='utf-8-sig'))
data['item.windcraft.white_wind_bison_spawn_egg']='Invocar Bis\u00e3o Branco dos Ventos'
p.write_text(json.dumps(data,ensure_ascii=False,indent=4,separators=(',',':  '))+'\n',encoding='utf8')
print('Created bison egg icon and Portuguese name.')
