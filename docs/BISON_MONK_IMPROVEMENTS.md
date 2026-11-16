# Bisão Branco e Monge do Vento — 15/11/2026

> Histórico: bisão e monge foram retirados do código ativo em 16/11/2026.
> Arquivos preservados em `archive/wind-society/`; foco atual: vento e planador.

## Implementação

Compatibilidade preservada: Minecraft 26.3, Forge 66.1.8 e Java 25. Nenhuma
alteração na física, atributos de velocidade, IA, vento ou planador.

`rendering/BisonAnimations.java` aplica poses de idle/walk/run. Os pesos usam
smoothstep sobre a velocidade de animação interpolada do Minecraft. Essa
velocidade vem do deslocamento horizontal observado da entidade; a fase vem
da distância acumulada, sem temporizador de renderização para mover os pés.
Não há estado mutável no modelo compartilhado entre entidades.

`WhiteWindBisonModel.setupAnim` restaura a pose pelo método da superclasse e
delega a animação. Pernas diagonais alternadas, cadência 0,42 (antes 0,55),
ângulos limitados a 0,50 radianos. O balanço vertical máximo é 0,24 pixels do
modelo (0,015 bloco), com roupa e arreio acompanhando o tronco. Cabeça, corcova
e cauda recebem oscilações menores. Run aumenta a amplitude gradualmente,
sem alterar velocidade, AI ou pacotes de rede.

Refinamento das pernas: apoio ocupa 64% do ciclo andando e 53% correndo.
A recuperação usa uma curva suave e levanta os cascos em até 0,8/1,4 pixels.
A altura do pivô compensa o encurtamento vertical causado pela rotação da
perna rígida, mantendo a extremidade do casco no plano do chão durante o apoio.
Os pares diagonais continuam defasados em meio ciclo, com sobreposição de
apoio em vez de oscilar como dois pêndulos perfeitamente opostos. Não há IK
para inclinações do terreno nem articulação de joelhos nesta geometria.
Validação do refinamento: `test build` passou com **45/45 testes**, incluindo
altura dos cascos durante apoio/recuperação e restauração da pose parada.
JAR 0.6.0 atualizado; inspeção visual no Minecraft ainda pendente.

Multiplayer usa o fluxo vanilla de movimento replicado e interpolação por cliente.
Não se promete uma fase numericamente idêntica entre clientes conectados em
momentos diferentes. Fluidez com latência e troca de tracking requer teste real.
Os clipes de referência do Blockbench permanecem idle/walk; os três estados
do jogo são procedurais, não dependem desses clipes.

O ovo mantém o único registro `windcraft:white_wind_bison_spawn_egg`, a classe
vanilla `SpawnEggItem`, o componente `Item.Properties.spawnEgg(BISON.get())`
e a aba `SPAWN_EGGS`. A definição em `items/` aponta ao modelo existente em
`models/item/`, com parent `minecraft:item/generated` e PNG próprio. A arte
16 × 16 usa marfim, cinza, faixa azul e cabeça com chifres. Nome pt-BR:
**Invocar Bisão Branco dos Ventos**. Não foram criados registros adicionais.

O monge mantém cabeça, nariz, braços cruzados, corpo, pernas e animação de
`VillagerModel`. O atlas original foi redesenhado nos UVs de aldeão: cabeça
U0/V0, nariz U24/V0, torso U16/V20, túnica U0/V38, pernas U0/V22,
mangas U44/V22, braços dobrados U40/V38 e capuz na região U64+.
Rosto com olhos verdes, roupa marrom-escura, bordas bege, faixas azuis,
corda na cintura, detalhes dourados e espirais no peito/costas. O capuz
aberto mantém o rosto exposto; a aba larga herdada do chapéu vanilla foi
esvaziada explicitamente. Não há camada de armadura metálica.

A referência remota de templário não pôde ser acessada pelo navegador nem por
download. A interpretação foi feita a partir da descrição textual fornecida,
sem aplicar ou copiar uma skin de jogador.

## Arquivos desta alteração

- `src/main/java/dev/windcraft/rendering/BisonAnimations.java` (novo).
- `src/main/java/dev/windcraft/rendering/WhiteWindBisonModel.java`.
- `src/main/java/dev/windcraft/rendering/WindMonkModel.java`.
- `src/main/java/dev/windcraft/village/VillageGameTests.java`.
- `src/test/java/dev/windcraft/rendering/WhiteWindBisonModelTest.java`.
- `src/main/resources/assets/windcraft/lang/pt_br.json`.
- `src/main/resources/assets/windcraft/textures/entity/wind_monk.png`.
- `src/main/resources/assets/windcraft/textures/item/white_wind_bison_spawn_egg.png`.
- `tools/create_bison.py` (regeneração Java preserva a delegação de animação).
- `tools/create_monk_assets.py` (fonte do novo atlas; também regenera o ovo do monge).
- `tools/create_bison_egg.py` (novo; PNG e nome pt-BR, usa Pillow).
- `docs/models/{wind_monk_atlas_preview,white_wind_bison_egg_preview}.png`.
- Este relatório, `WHITE_WIND_BISON.md` e `WIND_VILLAGE_IMPLEMENTATION.md`.

Textura corporal do bisão e renderer permanecem nos caminhos existentes.
Os previews são ampliações dos PNGs com nearest-neighbor, não capturas do jogo.

## Validação e teste manual

Resultado: `test build runGameTestServer` offline passou com **44/44 testes
JUnit e 3/3 GameTests**. JAR gerado: `build/libs/windcraft-0.6.0.jar`.
Verificados no JAR o encadeamento definição/modelo/textura do ovo, dimensões
dos PNGs e o nome pt-BR exato. Os avisos de APIs depreciadas/nativas do
toolchain não impediram a compilação.

JUnit verifica bake, quatro pernas em pares opostos, limites de amplitude,
pesos de estados e ausência de acumulação das transformações entre frames.
O GameTest verifica o tipo resolvido pelo ovo e a criação pelo mesmo caminho
`EntityType.spawn` utilizado pelo ovo, além de navegação e atributos do monge.
Isso não simula o clique de um jogador nem renderização GPU.

No cliente com o novo `build/libs/windcraft-0.6.0.jar`, use mundo descartável:

```mcfunction
/give @s windcraft:white_wind_bison_spawn_egg
/summon windcraft:white_wind_bison ~ ~ ~
/summon windcraft:wind_monk ~ ~ ~
```

Em criativo, conferir nome, ícone na aba de ovos e invocação ao clicar no solo.
Observar o bisão parado, andando e perseguindo um jogador em sobrevivência
após receber dano. Conferir transições, pés, cauda, cabeça, roupa e ausência
de saltos visuais; testar dois bisões para excluir interferência entre poses.
Inspecionar o monge de frente, lados e costas, capuz ao girar a cabeça, olhos,
nariz, túnica, símbolos, braços e botas durante a caminhada. Comparar com um
aldeão vanilla. Repetir com dois clientes em servidor dedicado e latência.
Essas verificações visuais e multiplayer permanecem manuais e não foram realizadas.
