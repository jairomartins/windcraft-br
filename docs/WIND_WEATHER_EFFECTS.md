# Precipita??o e poeira com vento ? primeira vers?o

## Investiga??o e viabilidade

Investigado no c?digo local em 16/11/2026. N?o foram encontrados AGENTS.md no projeto ou nos diret?rios ancestrais consultados. Altera??es locais anteriores de voo, publica??o, documenta??o e QA foram preservadas.

- `build.gradle`: Minecraft **26.3**, Forge **66.1.8**, ForgeGradle **7.0.40**, toolchain **Java 25**. O Mavenizer informa **official-26.3**; n?o h? mappings Yarn/NeoForge. A distribui??o 26.3 usa os nomes oficiais diretamente (mapeamento gerado vazio).
- Fonte prim?ria da investiga??o: `D:/Programacao/mods/.gradle/caches/minecraftforge/forgegradle/mavenizer/caches/forge/net/minecraftforge/forge/26.3-66.1.8/injected-sources.jar`, especialmente `WeatherEffectRenderer`, `WeatherRenderState`, `ViewportEvent`, `ForgeHooksClient`, `Camera`, `Options`, `DustParticleOptions` e `ParticleStatus`.
- A vers?o tamb?m consta no ?ndice oficial: https://files.minecraftforge.net/net/minecraftforge/forge/ . N?o foram usados nomes de APIs de outras vers?es como evid?ncia.

`WeatherEffectRenderer.extractRenderState(ClientLevel,float,Vec3,WeatherRenderState)` consulta intensidade global, `options.weatherRadius()`, altura `MOTION_BLOCKING` e `level.getPrecipitationAt(pos)` (inclui a decis?o local de chuva/neve pela posi??o). Cria listas separadas de `ColumnInstance`. `prepareInstances(VertexConsumer,List,Vec3,float,int,float)` desenha quatro v?rtices por coluna; `prepare` envia a malha ? GPU. H? caminhos normal e OIT compartilhando essa prepara??o. Texturas vanilla, transpar?ncia, luz e raio s?o preservados.

N?o foi encontrado evento p?blico do Forge 66.1.8 para deformar os v?rtices de precipita??o. **Chuva inclinada e neve com deriva s?o vi?veis via mixin**, com risco de conflito se outro mod substituir esse renderizador. **Areia ? vi?vel com part?culas locais**; neblina usa eventos p?blicos `ViewportEvent.RenderFog` e `ComputeFogColor`.

A documenta??o do evento RenderFog ainda menciona cancelamento, mas nesta implementa??o o evento ? um record `Cancellable` sem `setCanceled`; `ForgeHooksClient` publica o evento com `FogData` por refer?ncia e n?o condiciona a aplica??o ? resposta de cancelamento. A integra??o altera os dados diretamente, sem cancelar os pr?ximos ouvintes.

## Vento existente confirmado

`WindEngine` recebe chuva e trov?es como intensidades: multiplicador `1 + 0.3*chuva + 0.6*trov?o`. N?o altera o ciclo meteorol?gico. Dire??o vem do ru?do temporal ou comando, sem influ?ncia direta do clima. Bioma, altura, ciclo diurno, rajadas e abrigo modulam a velocidade.

`ExposureCache` usa c?lulas 8?, cinco amostras de altura e validade de 40 ticks. Sem c?u aberto nas amostras resulta em exposi??o 0.05, n?o zero. Biomas s?o classificados pelo nome no sistema existente: forest/jungle 0.65, ocean/beach 1.3, peak/savanna 1.15. Isso n?o foi alterado. `UpdraftService` usa temperatura, sol, chuva, relevo e lava; chuva reduz a parcela solar. Dimens?es com teto ou sem skylight n?o recebem vento horizontal.

`WindService` envia `WindNetwork.Snapshot` por jogador (padr?o a cada 20 ticks): dimens?o, velocidade local, dire??o, velocidade atmosf?rica e op??o de part?culas. Os efeitos novos usam **velocidade local**, j? modulada pelo servidor. O snapshot n?o cont?m um campo espacial de vento; todas as colunas pr?ximas usam essa refer?ncia. Sob abrigo, a precipita??o ao redor pode parecer menos inclinada por esse motivo.

## Implementa??o e unidades

- `EnvironmentalMotion`: fun??es puras de suaviza??o, inclina??o e m?quina de estado com histerese.
- `EnvironmentalEffects`: vetor suavizado e interpolado, estado cosm?tico da areia, cache de obst?culos, part?culas e neblina.
- `WeatherEffectRendererMixin`: intercepta apenas as quatro chamadas `VertexConsumer.addVertex(FFF)` em `prepareInstances`, preservando UVs e renderiza??o vanilla. Identifica a lista de neve pelo par?metro vanilla `maxAlpha=0.8`; chuva usa 1.0. Depend?ncia espec?fica desta vers?o, a revisar ao atualizar.
- `WindClient`: chama os efeitos s? com snapshot v?lido e fora da pausa; reinicia em troca de mundo/dimens?o ou timeout de snapshot (120 ticks).
- `WindClientSetup`: registra eventos somente no cliente. Configura??es ficam em `WindVisualConfig`; inclus?o do mixin na lista `client` de `windcraft.mixins.json`.

Conven??o confirmada em `WindSample`: **0? norte = ?Z; 90? leste = +X**. `xPerTick/zPerTick` dividem m/s por 20. Recuperamos blocos/s multiplicando por 20 (1 bloco = 1 metro nesta simula??o). Inclina??o = `min(velocidade*for?a/velocidadeVerticalVisual, tan(?nguloM?ximo))`. Refer?ncias visuais: chuva 20 blocos/s, neve 5 blocos/s; n?o s?o novas velocidades f?sicas do Minecraft.

O p? da coluna fica ancorado na posi??o exposta vanilla e o topo se desloca **contra** o vento. Assim, a textura que cai do topo ao p? percorre a dire??o **para a qual** sopra o vento. Vetor suavizado por tick (`8%` da diferen?a) e interpolado entre ticks, sem salto em 359?/0?. Neve mant?m UVs vanilla (queda mais lenta e deriva irregular), acrescentando modula??o senoidal por coluna de 0.76?1.0 na inclina??o, inclusive quando o vento atinge o limite angular. A oscila??o depende do tempo e posi??o, sem aleatoriedade por frame.

### Oclus?o

A sele??o local e corte inferior continuam vanilla. O caminho horizontal inclinado ? amostrado a cada no m?ximo meio bloco no eixo dominante; qualquer coluna cuja altura `MOTION_BLOCKING` ultrapasse o p? cancela a inclina??o. Chunks ausentes tamb?m cancelam. Cache limitado a 2048 caminhos, invalidado a cada cinco ticks e na troca de mundo. N?o h? carregamento deliberado de chunks nem varredura de volume.

? uma prote??o conservadora: paredes altas pr?ximas podem deixar faixas verticais, telhados podem causar transi??o vis?vel e constru??es modificadas t?m at? cinco ticks de atraso no cache. O teste usa o centro da faixa, n?o a largura completa do quad; bordas de telhado, diagonais, formas parciais e beirais ainda precisam de inspe??o visual. N?o h? colis?o f?sica por gota/floco: o vanilla desenha faixas, n?o entidades de chuva. Sons e respingos vanilla continuam verticais. Alterar geometria n?o altera onde chove para agricultura, fogo ou outros sistemas.

## Areia

Elegibilidade consultada a cada cinco ticks: jogador em `Biomes.DESERT` ou tag vanilla `IS_BADLANDS`, dimens?o com c?u, c?u vis?vel, superf?cie abaixo de areia/areia vermelha e diferen?a de altura inferior a 12 blocos. Cada emiss?o revalida chunk, bioma, superf?cie, c?u e espa?o livre. Aus?ncia de chuva sozinha n?o ativa nada. Poeira transportada ? removida sob cobertura, longe do jogador ou ao invalidar o mundo.

In?cio exige velocidade local >=7 m/s durante 100 ticks consecutivos; fim exige <=5 m/s (ou ambiente ineleg?vel) durante 60 ticks. Valores entre os limiares mant?m o estado atual. O limiar efetivo de t?rmino ? limitado a in?cio?0.1. Intensidade sobe/desce linearmente em 80 ticks; abrigo/sa?da do bioma inicia fade imediatamente. Ao desligar a op??o, part?culas somem e neblina fica inativa imediatamente.

Esses valores s?o alcan??veis sem chuva: com base 5, bioma de deserto ~1 e altitude pr?xima ao mar, a f?rmula admite aproximadamente 1.6?16.3 m/s, dependendo do ru?do e hor?rio. Um teste percorre 240000 ticks com seed 123, sem chuva e sem override, exigindo >=100 ticks consecutivos acima de 7 m/s. N?o significa ocorr?ncia garantida em cinco segundos: depende da seed e fase do vento.

O evento ? **cosm?tico e local**, derivado do vento autoritativo sincronizado. N?o precisa de estado adicional nem pacote por part?cula. Dois clientes podem ter fases de ativa??o diferentes ao conectar, configura??es e exposi??o diferentes. Um evento de gameplay compartilhado exigiria autoridade/estado no servidor; est? fora desta primeira vers?o.

Part?culas `DUST` vanilla tingidas de bege (`D8BB80`) ou terracota (`BF7545`), com amostras baixas e suspensas at? cinco blocos acima da superf?cie. M?ximo seis tentativas de emiss?o por tick e or?amento de 120 part?culas novas por padr?o. Dura??o 35?59 ticks; velocidade atualizada com o vetor existente. Raio m?ximo 20 blocos, limitado pela dist?ncia efetiva do cliente. Op??o DECREASED divide or?amento por tr?s; MINIMAL zera estas part?culas. `Snapshot.particles=false` tamb?m as desativa. Neblina continua independente da op??o de part?culas; pode ser zerada separadamente.

A redu??o da dist?ncia de neblina ? multiplicativa e moderada (at? 35% por padr?o), com mistura parcial da cor da poeira. N?o ? aplicada em ?gua, lava ou neve fofa. N?o h? dano, cegueira, entidades, mudan?as em blocos, ac?mulo de neve ou som novo. Som foi omitido nesta vers?o; nenhum recurso inexistente ? referenciado.

## Configura??o

Todas as op??es abaixo s?o **CLIENT** em `config/windcraft-client.toml`. Nenhuma nova configura??o compartilhada ? necess?ria porque n?o h? simula??o de gameplay nova.

| Chave | Padr?o | Fun??o |
|---|---:|---|
| windRain | true | Inclina??o da chuva |
| windSnow | true | Deriva da neve |
| rainWindStrength / snowWindStrength | 1.0 / 1.0 | Escala de influ?ncia visual |
| rainMaxAngle / snowMaxAngle | 25 / 35 graus | Limites da inclina??o |
| sandstorms | true | Poeira e neblina local |
| sandstormStartSpeed / sandstormStopSpeed | 7 / 5 m/s | Histerese |
| sandstormStartTicks / sandstormStopTicks | 100 / 60 | Sustenta??o para ligar/desligar |
| sandstormFadeTicks | 80 | Transi??o da intensidade |
| sandstormMaxParticles | 120 | Or?amento vivo |
| sandstormDensity | 1.0 | Densidade de emiss?o |
| sandstormVisibilityReduction | 0.35 | Redu??o m?xima da dist?ncia de neblina; 0 desativa |

`windRain=false`, `windSnow=false`, `sandstorms=false` restauram os caminhos ambientais anteriores. Nuvens/folhas existentes continuam com suas op??es pr?prias. Nenhum codec ou protocolo de rede foi alterado.

## Compatibilidade e limites

Os pontos de inje??o usam `require=0`: aus?ncia do alvo de v?rtices em renderizador substitu?do pode deixar apenas precipita??o vanilla sem impedir a areia. Isso n?o resolve conflitos fortes entre mixins ou mudan?as de assinatura; n?o h? detec??o gen?rica confi?vel de shaderpacks. Outros mods que substituem renderiza??o, c?u, biomas ou fog podem sobrepor os efeitos. Shaders, OIT em jogo e mods meteorol?gicos **n?o foram validados**. Usu?rio pode desligar chuva, neve ou fog isoladamente. N?o foi adicionada lista especulativa de incompatibilidades por identificadores de mods.

## Roteiro manual (pendente de execu??o visual)

Usar o JAR rec?m-compilado em cliente e servidor, com permiss?es de operador. A sintaxe real em `WindCommands` ? `/wind set speed <0..60>`, `/wind set direction <0..360>`, `/wind reset`, `/wind info` e `/wind debug`. Velocidade fixada ignora clima/bioma/altitude, mas continua reduzida pelo abrigo. Reset restaura velocidade E dire??o. Comandos `/weather` abaixo apenas montam o teste; o mod n?o os executa.

1. Plan?cie exposta: `/weather rain`, `/wind set speed 12`, `/wind set direction 90`; conferir chuva caindo para +X. Trocar para 270 e observar suaviza??o para ?X; repetir 0/180 (?Z/+Z).
2. No in?cio/fim gradual da chuva, manter vento 20: transpar?ncia/intensidade devem acompanhar o vanilla, sem criar precipita??o em bioma seco. Repetir sob baixa intensidade antes da chuva plena.
3. Bioma frio: `/weather rain`, vento 6; girar lentamente 0?90?180 e 359?0. Flocos devem cair mais lentamente que chuva, com oscila??o cont?nua e limites est?veis; testar altura que muda o tipo de precipita??o.
4. Construir casa, telhado isolado, parede fina, beiral, escada e acesso a caverna. Entrar/sair e observar de dentro para fora. Sem emiss?o interna de poeira; chuva/neve devem respeitar corte vanilla e retornar a faixas verticais quando o guard detectar obst?culo. Inspecionar cantos/bordas e documentar vazamentos residuais.
5. Deserto sobre areia: `/weather clear`, vento 4 (sem evento); depois 9 por mais de nove segundos (5 de sustenta??o +4 de fade). Variar entre 6 e 8: evento n?o deve piscar. Baixar a 4 por mais de sete segundos para fim e fade. Repetir em badlands sobre areia vermelha; n?o deve emitir sobre pedra, telhado ou superf?cie subterr?nea.
6. Atravessar a borda deserto/plan?cie, ?gua e mudan?as de superf?cie: intensidade deve diminuir gradualmente, emiss?o apenas nos locais eleg?veis. Entrar em abrigo durante a tempestade.
7. Pausar por 10 segundos: vetor, timer e poeira devem congelar. Retomar; trocar dimens?o (incluindo Nether/End), desconectar e reconectar: sem estado/part?culas antigas ou fase de tempestade herdada.
8. Dois clientes no mesmo servidor: conferir dire??o do vento, diferen?as justificadas por abrigo e op??es locais; reconectar um cliente durante evento. Iniciar servidor dedicado com JAR e sem classes de cliente carregadas. Observar logs de mixin tamb?m no cliente.
9. ALL/DECREASED/MINIMAL: or?amento 120/40/0. Desligar cada efeito isoladamente e todos juntos; zerar fog; reduzir dist?ncia de renderiza??o. Conferir aus?ncia de efeito residual, part?culas limitadas e custo em floresta/deserto na borda de chunks.
10. `/wind set speed 0`, 9, 20 e 60; repetir dire??es opostas. `/wind info` confirma valor local. `/wind reset` devolve varia??o natural e permite observar evento sem chuva. Verificar novamente shader/mod meteorol?gico individualmente, sem declarar compatibilidade antecipada.

## Valida??o automatizada

Executado:

1. `gradlew.bat build`: tentativas iniciais bloqueadas ao consultar manifesto de launcher; repeti??o com permiss?o de execu??o externa tamb?m encontrou o bloqueio. O processo offline em novo daemon com o cache resolvido prosseguiu. A primeira compila??o encontrou `setCanceled` inexistente no record RenderFog; corrigido ap?s consultar bytecode da API e fonte real do hook.
2. `gradlew.bat --offline --no-daemon --no-configuration-cache test build runGameTestServer`: **BUILD SUCCESSFUL em 1m21s**, 53 JUnit sem falhas/erros/ignorados e **4/4 GameTests existentes aprovados**, ambiente SERVER sem carregamento indevido de classes cliente. Log de servidor copiado para `build/weather-gametest.log`. Avisos de acesso nativo, esquema de assets, consulta de atualiza??o bloqueada e preparo atrasado do servidor n?o impediram os testes.
3. Ap?s refinamento exclusivamente visual da oscila??o da neve, `gradlew.bat --offline --no-daemon --no-configuration-cache test build`: **BUILD SUCCESSFUL em 39s**, **53/53 JUnit**, zero falhas/erros/ignorados. Relat?rios em `build/test-results/test` e `build/reports/tests/test/index.html`. Quatro testes novos cobrem eixos/unidades/limites, converg?ncia sem overshoot, histerese/sustenta??o/fade e ativa??o sem chuva com a simula??o natural.
4. JAR final `build/libs/windcraft-0.6.1.jar` iniciado em cliente isolado `qa/weather-smoke-20261116-225611`, somente Forge/WindCraft e depend?ncias. Cria??o dos atlas e configura??o CLIENT confirmadas; encerrado pelo harness ap?s essa etapa. **Nenhum erro de aplica??o/inje??o do mixin**. Houve falhas de autentica??o/consulta Realms pela identidade local sem conta e erro de op??o de teste `simulationDistance:4` abaixo do m?nimo 5 (fallback vanilla); n?o representam teste de gameplay.
5. Classe transformada exportada em `.mixin.out/class/net/minecraft/client/renderer/WeatherEffectRenderer.class` nesse diret?rio: `javap -p -c` confirmou as duas inje??es de in?cio e **quatro chamadas ao redirect de v?rtices**. Evid?ncia textual: `build/weather-renderer-transformed.txt`. JAR conferido para inclus?o das tr?s classes novas.

N?o foram executados testes visuais de precipita??o/areia em um mundo, ensaio de FPS, multiplayer com dois jogadores, shaderpacks ou instala??o limpa do JAR em servidor dedicado convencional. O GameTestServer comprova carregamento no lado SERVER, n?o substitui essa matriz. Os dez cen?rios manuais acima continuam pendentes. O smoke do cliente comprova transforma??o/carregamento, n?o apar?ncia, orienta??o observada, fog em gameplay ou oclus?o perfeita.

`git diff --check` nos quatro arquivos rastreados alterados por esta tarefa passou. A verifica??o global apontou whitespace j? presente na altera??o local de `build.gradle:12`; essa altera??o anterior n?o foi modificada. Novos arquivos da tarefa permanecem sem commit.
