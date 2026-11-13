# Verificação — WindCraft 0.5.0

Ambiente: Windows 11, Java 25, Minecraft 26.3 e Forge 66.1.8.

## Compilação e testes

`gradlew --no-daemon --no-configuration-cache build` executado com sucesso.
37 testes, sem falhas: motor de vento (5), interpolação ambiental (4), física do
planador (15), estado (2), transições visuais (6), HUD (3) e pose clássico/fino (2).

Os testes novos simulam curvas usando somente yaw do olhar, incluindo ±179°,
e abertura sem queda prévia no ápice de um salto. Verificam convergência da direção,
valores finitos e ausência de sustentação gratuita em ar parado. Os quatro testes
da câmera automática foram removidos juntamente com essa funcionalidade.

A função de forças GliderPhysics foi preservada. O yaw/pitch nativo do jogador
é usado na previsão e no servidor. A câmera que seguia a velocidade foi removida,
permitindo olhar na direção desejada sem sobrescrever a vista renderizada.
O Minecraft já sincroniza a orientação, sem novos pacotes de rotação.

## Mudanças verificadas no código

- Removida a exigência de queda de 1,5 bloco. Pode preparar no chão; a física
  começa ao saltar e o estado fecha ao pousar depois do primeiro voo.
- Preparado no chão não aplica movimento, fome nem desgaste periódico. A ativação
  ainda consome um ponto de durabilidade. Restrições de água, lava, montaria,
  escada, voo criativo e elytra permanecem.
- Removidos controlador, configurações e Mixins de câmera. O mod não troca
  perspectiva, rotação de câmera ou FOV; F5 mantém seu funcionamento nativo.
- Um loop local de SoundEvents.ELYTRA_FLYING acompanha a velocidade relativa
  do ar com volume/pitch suaves. Respeita volume de jogadores e as preferências
  gliderWindSound/gliderWindVolume; encerra ao fechar, morrer ou desconectar.
- Novo ícone transparente 64×64, com fonte e prompt preservados em docs/assets.
- UVs explícitas no modelo compacto corrigem o erro de bake de textura observado
  ao retomar a execução da versão anterior.

## Execução real

O cliente de desenvolvimento foi aberto com heap máximo de 2 GiB. Carregou os
recursos corrigidos e entrou no mundo de testes. Foi observado planeio ativo em
primeira pessoa, HUD com velocidade/vento/altitude e novo ícone na hotbar.
Não houve repetição do erro de UV nem da falha de alocação nativa nessa retomada.
Autenticação de Realms do usuário Dev sem conta não impede o mundo local.

Havia entrada do usuário na janela; ela foi deixada sob seu controle. Não foram
concluídos por automação: comparação auditiva do loop e suas opções, preparação
no chão/salto/cancelamento, F5 durante voo, pouso, colisões, armaduras, skin fina,
portais, quebra do item e observação em dois clientes multiplayer. Testes unitários
de direção/pose não substituem esses cenários de integração.

## Roteiro de conferência

Num mundo de testes com comandos, use /give @s windcraft:wind_glider.
Prepare no chão e salte, ou use no ar imediatamente; mova o mouse, teste W/S,
A/D, Espaço e Shift. Alterne F5 e confirme que o mod não muda sua escolha.
Compare /wind set speed 0 e 10, nas quatro direções, e finalize com /wind reset.
Confira o áudio durante voo, fechamento, pouso e retorno ao menu. Teste também
água, troca de item, morte e desconexão. Em dois clientes 0.5.0, confira trajetória,
pose e encerramento visual do outro jogador. Mods de câmera/armadura próprios
e latência elevada ainda precisam de avaliação conjunta.
