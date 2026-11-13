# Planador — pose e renderização 0.5.0

O item de inventário mantém o ícone próprio. Fora do voo, o modelo Java de item
`wind_glider_stowed.json` é exibido na mão. Em voo, os estados de item nas mãos
são ocultados apenas na cópia de renderização, e uma camada desenha um único
planador centralizado no tronco. Nenhum ItemStack é removido ou duplicado.

## Âncoras e pose

`GliderAvatarRendererMixin` extrai um frame visual por avatar e aplica ao mesmo
PoseStack utilizado por skin, armaduras e camadas. O corpo inclina aproximadamente
82°, com pitch/rolagem filtrados. Não chama `setPose`, `setPos`, `setYRot` ou qualquer
API de colisão do jogador. A hitbox continua a original.

`GliderPlayerAnimator` ajusta somente modelos com frame de planador visível.
Braços usam rotação X de 2.30 radianos, pernas ficam estendidas e a cabeça tem
limites de rotação. O ponto inferior dos braços clássico e fino tem Y=-4.66 e
Z=7.46 em pixels de modelo, coincidente com a barra em `GliderModel`.
As mangas, calças e jaqueta são filhos das partes nativas, preservando a skin.
Os modelos nativos de armadura passam pelo mesmo ajuste de humanoide.

`GliderModelRenderer` compartilha a transformação corporal. A asa usa a textura
original do projeto, com expansão lateral durante a transição; a estrutura de
bambu tem barra, haste, travessa e suportes. A geometria de voo está em coordenadas
do modelo de personagem, e não presa à mão direita/esquerda.

## Pontos de edição

- Ícone e item normal: `assets/windcraft/items/wind_glider.json`,
  `models/item/wind_glider_icon.json`, `wind_glider_stowed.json`.
- Modelo antigo aberto: `wind_glider_open.json`, mantido como referência de desenho.
- Camada de voo: `rendering/GliderModel.java` e `GliderModelRenderer.java`.
- Texturas compartilhadas: `textures/item/wind_glider_sail.png` e `wind_glider_frame.png`.

Modelos de item podem ser editados como **Java Block/Item** no Blockbench. Para
substituir a camada de voo, exporte geometria compatível com `ModelPart` ou adapte
as coordenadas em `GliderModel`. Preserve a âncora da barra e confira ambas as
skins, armaduras e câmeras. Um JSON de item sozinho não substitui a camada corporal.

Os Mixins de pose são necessários porque 26.3 extrai e submete os modelos antes
da renderização final. Alterar apenas o modelo no evento posterior não garante
que skin e armadura recebam a mesma pose. A câmera permanece totalmente nativa:
o mod não interfere na perspectiva ou FOV. Não há alteração de shaders.
