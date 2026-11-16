# Vila dos Ventos — implementação progressiva

> Histórico: vila, monge e bisão foram retirados do código ativo em 16/11/2026.
> Fontes preservadas em `archive/wind-society/`. Estado atual em `GLIDER_FLIGHT_SYSTEM.md`.

Melhorias posteriores de animação do bisão, ovo e roupa do monge estão
documentadas em [BISON_MONK_IMPROVEMENTS.md](BISON_MONK_IMPROVEMENTS.md).

## Estado desta entrega (15/11/2026)

Implementada a fundação da Fase A: Portal das Nuvens (13 × 11), Praça do Vento
(21 × 21), cela (5 × 5, telhado com beiral), Torre de Ascensão (7 × 7, plataforma
a 24 blocos) e pontes de cinco blocos de largura. Há posicionamento real por
comando, apoios de pedra e verificação prévia do terreno. Não é a vila completa
de cinco níveis. A torre tem escada vertical, não escadaria interior.

O pedido independente do bisão também foi implementado: projeto Blockbench
editável, textura original, modelo Java, renderer exclusivo, entidade, ovo,
movimentação e animações básicas. **Não há geração natural do bisão.**

### Situação por fase

| Fase | Implementado e verificado | Pendente |
|---|---|---|
| A | Quatro módulos e conectores; compilação; caminhos por teste; construção no servidor Minecraft | Inspeção visual no cliente e lançamento manual com planador |
| B | Nenhum registro de worldgen introduzido | Geração natural, frequência configurável, biomas, relevo, colisões entre estruturas, testes de sementes |
| C | Monge próprio, renderer/textura exclusivos, ovo, IA pacífica, área de residência e pausa noturna; aldeões vanilla preservados | Inspeção visual no cliente, habitat integrado à geração e spawn natural controlado |
| D | Bisão registrado; modelo editável, textura, renderer, IA, ovo; criação e ticks no servidor | Harpia; curral; santuário; associação de habitantes aos módulos; inspeção visual do bisão no cliente |
| E | Comandos consultam `WindAPI`; plataforma física acessível | Validação manual do voo, indicadores animados, moinhos e integração ambiental adicional |

Não considerar A plenamente aceita antes da verificação visual/voo. As fases
B–E não estão concluídas. A referência Harp.webp permanece como referência para
a etapa da harpia; não foi transformada em um recurso fictício.

## Inspeção e arquitetura

- Base preservada: Forge **26.3-66.1.8**, ForgeGradle 7.0.40, Java **25**,
  mappings oficiais 26.3, namespace `windcraft`. Sem novas dependências do mod.
- Os registros seguem `DeferredRegister`, `RegistryObject` e os eventos Forge
  EventBus 7 utilizados no projeto. Rendering fica no lado cliente.
- `WindAPI.getWind(ServerLevel, BlockPos)` consulta velocidade e direção;
  `getWindField` acrescenta corrente vertical, rajadas e turbulência.
  `WindService`, `WindEngine` e `UpdraftService` permanecem intactos.
- Registro do planador: `WindCraft.WIND_GLIDER`; item `WindGliderItem`;
  funcionamento em `GliderFlightController` e `GliderPhysics`. Nenhuma alteração
  nessas classes ou nos mixins existentes.
- Não havia sistema próprio de geração de estruturas. Esta etapa usa módulos
  procedurais reais para permitir testar a geometria antes de registrar worldgen.
  Não há JSON referenciando templates de vila inexistentes. O NBT incluído é
  uma área vazia real usada exclusivamente pelos testes de integração.
- Atenção à API 26.3: cores são `Blocks.WOOL.pick(DyeColor)` e
  `Blocks.BED.pick(DyeColor)`. O ovo usa `Item.Properties.spawnEgg`.

`VillageBlueprint` contém somente geometria e materiais, sem classes Minecraft.
`VillagePlacement` traduz os materiais em estados de blocos e prepara um plano
sem alterar o mundo. Só depois da aprovação técnica desse plano o comando
escreve os blocos, em ordem vertical, e notifica os vizinhos.

A verificação exige chunks carregados, limites de altura e borda válidos,
ausência de fluidos/obstruções e até 12 blocos até um apoio sólido. Protege
blocos existentes e entidades no volume dos blocos novos. Não carrega chunks
nem corta terreno. Essa política é conservadora: uma montanha irregular poderá
ser recusada; não é uma solução de adaptação natural ao relevo.

Não usar esse posicionador diretamente num evento de carregamento de chunk.
Na Fase B, implementar `Structure`/`StructurePiece` próprios ou templates reais,
com escrita limitada à bounding box de cada chunk, persistência e análise
prévia de relevo. O comando desta entrega não garante afastamento de estruturas
enterradas ou não carregadas de outros mods.

## Testar no Minecraft

Use um mundo descartável com cheats. O `origin` é o bloco de piso, normalmente
o primeiro bloco de ar acima do terreno, e não a posição dos pés do jogador.
Escolha uma área livre aproximadamente de 50 × 40 blocos, com apoio próximo.
Para a composição completa, a origem fica no canto noroeste da praça. A área
vai de X −11 a +37 e de Z 0 a +36 em relação à origem; carregue os chunks.

```mcfunction
/windvillage foundation check <x> <y> <z>
/windvillage foundation place <x> <y> <z>
/windvillage portal check <x> <y> <z>
/windvillage plaza place <x> <y> <z>
/windvillage cell place <x> <y> <z>
/windvillage tower place <x> <y> <z>
/give @s windcraft:wind_glider
/give @s windcraft:anemometer
/give @s windcraft:white_wind_bison_spawn_egg
/summon windcraft:white_wind_bison ~ ~ ~
/summon windcraft:wind_monk ~ ~ ~
/give @s windcraft:wind_monk_spawn_egg
/wind info
```

Todos os módulos aceitam `check` e `place`; requerem permissão de gamemaster.
Não há modo de forçar a substituição de construções nem comando de desfazer.
`check` não constrói nada. A colocação não é transação persistente para sobreviver
a uma interrupção do servidor no meio do comando.

Na composição `foundation`, a torre começa em (+27, 0, +7), a escada em
(+30, +1, +11) e a saída em (+30, +24, +11). Suba pela escada, caminhe para leste
e saia pela ponta do deque em X +37. A direção é fixa nesta etapa; consulte o
anemômetro e observe terreno/vento antes de saltar. Não há impulso artificial,
segunda simulação de vento ou mudança na física do planador. As bandeiras são
ornamentos estáticos, não birutas dinâmicas.

Verificação manual ainda necessária: percorrer portal → praça → cela → torre,
subir em sobrevivência, abrir o planador com o controle existente, testar vento
fraco/forte, observar colisões de asas e pousar. Verificar aparência/UV do bisão,
ovo, caminhada e reação a dano. Conferir que aldeões comuns mantêm aparência.

## Validação automatizada

```powershell
$env:GRADLE_USER_HOME='C:\Users\jairo\.gradle'
.\gradlew.bat test build --offline --no-daemon --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx384m -Xms32m -XX:+UseSerialGC -XX:ActiveProcessorCount=2'
.\gradlew.bat runGameTestServer --offline --no-daemon --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx384m -Xms32m -XX:+UseSerialGC -XX:ActiveProcessorCount=2'
python tools/create_bison.py
python tools/validate_bison.py
```

O gerador do modelo usa apenas Python padrão; o validador/preview usa Pillow,
já disponível no ambiente. Não são dependências do Minecraft.

Os testes de integração são registrados em `Registries.TEST_FUNCTION`, com
instâncias reais em `data/windcraft/test_instance`; as anotações Forge sozinhas
não produziram a descoberta nesta versão. O servidor executa dois testes próprios
e um teste padrão. O primeiro constrói a fundação, verifica escada/lanterna após
atualizações e cria o bisão, mantendo-o vivo durante ticks. O segundo confirma
rejeição de apoio inseguro e preservação de um baú. Não testam renderização GPU,
voo manual, worldgen, multiplayer ou recarga/persistência da entidade.

Resultados observados: `compileJava` passou; `test build` final passou com **41/41**
testes, incluindo o bake/animação do bisão; `runGameTestServer` passou com **3/3**,
incluindo os dois próprios. A configuração inicial sem cache correto falhou
por memória e acesso ao manifesto; a execução com cache existente funcionou.

## Arquivos

Criados:

- `src/main/java/dev/windcraft/village/{VillageBlueprint,VillagePlacement,VillageGameTests}.java`
- `src/main/java/dev/windcraft/commands/VillageCommands.java`
- `src/main/java/dev/windcraft/entity/{VillageEntities,WhiteWindBison}.java`
- `src/main/java/dev/windcraft/rendering/{VillageRenderers,WhiteWindBisonModel,WhiteWindBisonRenderer}.java`
- `src/test/java/dev/windcraft/village/VillageBlueprintTest.java`
- `src/test/java/dev/windcraft/rendering/WhiteWindBisonModelTest.java`
- `src/main/resources/data/windcraft/structure/village_test_space.nbt`
- `src/main/resources/data/windcraft/test_instance/{foundation_and_bison,rejects_unsafe_sites}.json`
- `src/main/resources/assets/windcraft/{items,models/item}/white_wind_bison_spawn_egg.json`
- `src/main/resources/assets/windcraft/textures/entity/white_wind_bison.png`
- `src/main/resources/assets/windcraft/textures/item/white_wind_bison_spawn_egg.png`
- `docs/models/white_wind_bison.{bbmodel,png}`, `white_wind_bison_preview.png`
- `tools/{create_bison,validate_bison}.py`
- Este documento e `docs/WHITE_WIND_BISON.md`.

Modificados: `WindCraft.java` (novos registros, comando e aba de ovos) e os dois
arquivos de idioma `assets/windcraft/lang/{pt_br,en_us}.json`.

Arquivos já não rastreados de publicação/changelog permaneceram intocados.

## Continuação: Monge do Vento (15/11/2026)

Implementada uma entidade `WindMonk` independente de aldeões vanilla, com 24 de
saúde, movimentação pacífica, fuga após dano, retorno à área de residência e pausa
de movimentação entre os ticks 13000 e 23000 do relógio do mundo. A pausa não
possui pose ou animação especial. A residência é definida na primeira atualização
ou por `setTempleHome`; não há associação automática com os módulos construídos.
Persistência da residência após recarregar o mundo ainda requer validação.

Modelo e animação reutilizam as proporções de aldeão, com capuz aberto e atlas
original de 128 × 64. O renderer exclusivo não adiciona camadas de profissão ou
bioma. O ovo tem ícone e traduções próprios. Não há comércio nem spawn natural.
`tools/create_monk_assets.py` regenera os PNGs e as traduções do monge.

Arquivos adicionais: `entity/WindMonk.java`, `rendering/WindMonkModel.java`,
`rendering/WindMonkRenderer.java`, `rendering/WindMonkModelTest.java` (em
`src/test/java/dev/windcraft`) e recursos `wind_monk` de textura/ovo.
O GameTest da fundação também verifica criação, navegação na praça, saúde,
comportamento pacífico e manutenção da residência durante ticks do monge.
A aparência e a caminhada ainda precisam ser observadas no cliente Minecraft.

Validação desta continuação: **43/43** testes JUnit e **3/3** GameTests passaram;
`test build` gerou `build/libs/windcraft-0.6.0.jar`. O teste de navegação aguarda
os primeiros ticks da entidade para que ela estabeleça contato com o piso antes
de calcular a rota. A inspeção do barramento do renderer lê o bytecode da
anotação para evitar inicializar o launcher Forge dentro do JUnit.
