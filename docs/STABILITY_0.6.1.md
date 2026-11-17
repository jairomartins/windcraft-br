# WindCraft 0.6.1 — diagnóstico e validação

Base: Minecraft 26.3, Forge 66.1.8, Java 25. Sem novas mecânicas; coeficientes e
limites preservados, com correção das forças e da orientação na decolagem.
Nenhum AGENTS.md encontrado na raiz do workspace ou na árvore WindCraft.
Estado inicial do Git: somente changelog.txt não rastreado; preservado.

## Achados e correções

| ID / prioridade | Evidência e causa | Reprodução / observado antes | Esperado / critério de conclusão | Validação |
|---|---|---|---|---|
| S01 / alta | input descartava todo segundo pacote no mesmo tick | Enviar eixo e depois close com sequência crescente no mesmo tick; close ignorado | Fechar imediatamente; manter limite dos eixos e rejeição de sequências antigas | GameTest glider_lifecycle |
| S02 / média | stop removia FLIGHTS antes de construir FlightVisual | Fechar planador azul; pacote de fechamento informava orange e bank 0 | Construir pacote com cor e bank originais antes de remover dados | Revisão da ordem de stop; aparência pendente no cliente |
| S03 / média | sendVisualTo usava isFlying, que exige estar no ar | Observar piloto preparado no chão; snapshot inicial inativo e atualização periódica ativa | Usar estado implantado no snapshot inicial | GameTest do gerador de snapshot; dois clientes pendentes |
| S05 / média / reforço | Logout removia estado sem anunciar fechamento | Desconectar em voo; sem pacote explícito de fechamento (entidade também é removida) | Enviar fechamento antes de remover estado no logout | Revisão do listener; multiplayer pendente |
| S06 / média | stop não encerrava uso; toggle reiniciava startUsingItem | Fechar por Shift/clique durante uso; flag de uso permanecia ativa | Encerrar uso somente do stack gerenciado e não reiniciar no fechamento | GameTest de cancelamento e toggle do item |
| S07 / alta | Modelos de item usavam sprites entity/glider fora do atlas de itens | Cliente isolado registrou Missing textures em todos os 16 modelos fan_* | Incluir tecidos coloridos e textura base no atlas de itens; zero avisos após bake | Carregamento real de cliente e verificação do JAR |
| S08 / alta | Física e pose derivavam rumo do fluxo relativo/velocidade | Abrir sem avanço, rumo sul e vento sul 10 m/s: primeira velocidade Z era −0,00270144 (norte) | Manter rumo do salto; vento deriva sem escolher o nariz; A/D curva | Oito JUnit de rumo, GameTest de ciclo e codecs; jogo real pendente |

O teste de ciclo também verifica rejeição de NaN e sequência antiga, preparação
sem forças, reabertura, remoção do equipamento, retorno ao travel vanilla e
fechamento repetido. Usa jogador simulado do servidor; não representa cliente real.

S04 foi descartado como defeito após revisão: o timeout fica depois do retorno
em solo, mas neutraliza comandos antes da primeira física aérea. O GameTest com
espera de 21 ticks cobre essa garantia existente; não foi necessário alterá-la.
S05 é reforço de limpeza explícita: não foi reproduzido planador preso no logout.

## Suspeitas, riscos e ajustes subjetivos

| ID / prioridade | Classificação / evidência | Reprodução | Critério de conclusão / validação |
|---|---|---|---|
| V01 / alta | Suspeita: colisão e queda dependem da ordem de travel/fall | Abrir em queda rápida, pousar suave, atingir parede/teto e entrar em água/lava | Sem imunidade indevida, estado preso ou força após fechar; jogo real |
| V02 / alta | Risco: previsão e servidor em momentos distintos; snapshots a cada 5 ticks | Dois clientes; curvas/flare, morte, portal e reconexão; repetir com latência | Cor/pose coerentes, controles restaurados e sem correções persistentes |
| V03 / média | Ajuste de sensação: sustentação/arrasto e clareza do HUD | Comparar vento 0/10, W/S, A/D, flare e perda de velocidade | Autor avaliar previsibilidade antes de alterar coeficientes; jogo real |
| V04 / média | Risco: falta de perfil de desempenho atual | Comparar 1 e vários pilotos na mesma rota/clima/distância | Registrar MSPT, FPS, alocações e tráfego |
| V05 / média | Risco visual: capturas antigas comprovam apenas laranja | Conferir 16 cores, F5, primeira pessoa, áudio, menus e opções | Sem textura ausente, loop residual ou HUD preso; cliente real |

## Desempenho — histórico anterior ao protocolo 8

Não houve medição de FPS, MSPT ou tráfego real com pilotos nesta sessão.
Não foi aplicada otimização sem gargalo medido.

Medição executada no codec real durante GameTest: corpo FlightSnapshot com
163 bytes e FlightVisual com 47 bytes, no exemplo overworld, entidade 1, cor blue
e flightTicks 40. A 4 atualizações/s, esse exemplo representa 840 bytes/s para
piloto (snapshot + visual) e 188 bytes/s por observador, somente esses corpos.
É uma extrapolação da cadência, não captura de rede: exclui framing do canal,
TCP, compressão, movimento vanilla, pacotes ambientais e entrada do piloto.
IDs, dimensão, cor e contador podem alterar o tamanho. Não houve comparação
antes/depois porque o formato e a frequência de rede foram preservados.

Inspeção estática: física por travel; vento de voo e snapshots a cada 5 ticks
(4/s a 20 TPS), snapshot ambiental conforme configuração e movimento a cada
20 ticks. Partículas locais: limite 160, até 5 novas/tick, alcance 32 blocos.
Cache de ascendentes: até 1024 células; exposição: até 2048, por dimensão,
com validade de 40 ticks. Esses limites não são medições de custo.

Procedimento: cópias do mesmo mundo, rota de 120 s, clima/vento fixos e mesmas
distâncias de renderização/simulação. Aquecer 60 s; repetir três vezes com 1, 4
e 8 pilotos nas mesmas máquinas. Capturar `/debug start` e `/debug stop`, JFR
quando disponível, FPS/frametime no cliente e bytes/pacotes da conexão. Registrar
mediana e p95 de MSPT/frametime, alocações e tráfego por piloto; comparar os
mesmos cenários na 0.6.0 e na 0.6.1.

## Roteiro manual de liberação

1. Em instância limpa de Minecraft 26.3 / Forge 66.1.8 / Java 25, instalar apenas
   o JAR 0.6.1. No servidor e em todos os clientes usar o mesmo JAR.
2. Preparar no chão, esperar, saltar, abrir no ápice e em queda rápida. Testar
   W/S, A/D, soltar controles, flare e Shift/clique para fechar. Abrir menu durante
   curva e confirmar eixos neutros; repetir com teclas remapeadas.
3. Pousar suave e rápido; colidir com paredes, teto e obstáculos. Entrar/sair de
   água e lava; trocar slot/mão, descartar e quebrar o item. Conferir ausência de
   física residual, som persistente, HUD preso e bloqueio dos controles nativos.
4. Morrer, respawnar, mudar de dimensão, desconectar e reconectar durante voo.
5. Com dois clientes, conferir cor, abertura, inclinação e fechamento. Aproximar
   observador de piloto já voando e de piloto preparado no chão. Repetir morte,
   portal e logout; depois repetir com 100–200 ms de latência e variação de atraso.
6. Fabricar e equipar as 16 cores; conferir ícone e tecido nas duas mãos, primeira
   pessoa e F5, com HUD/sons/transições ligados e desligados. Conferir nomes em
   pt_BR/en_US. HUD atual contém português fixo; localização completa é pendência.
7. Executar o procedimento de desempenho e registrar hardware/opções/resultados.

## Prontidão

Candidata Alpha local. Validações visuais, auditivas, colisões e multiplayer real
continuam necessárias antes de liberação. Não publicada, sem tag ou deploy.

## Artefato anterior à correção do atlas (histórico)

Comando final: `gradlew.bat --offline --no-daemon --no-configuration-cache test build runGameTestServer`, com GRADLE_USER_HOME no workspace. BUILD SUCCESSFUL em 1m19s; 41 JUnit e 4 GameTests aprovados. Log: `build/verification-061-release.log`.

JAR: `dist/windcraft-0.6.1.jar` (181231 bytes). SHA-256: `568dd6ecdaccc81613c47ae39d93eec0790093d7c10bc98b71b0a114f58a31ab`.
Metadados internos conferidos: versão 0.6.1, Forge [66.1.8,67), Minecraft [26.3,26.4).

## Artefato corrigido — atlas de itens

JAR anterior à correção do rumo: `dist/windcraft-0.6.1.jar` (181628 bytes).
SHA-256: `bf965a54a5ac6812919e153c87a907d955686d9c71b1f789c533d41fd84d9ae3`.

S07 corrigido adicionando `assets/minecraft/atlases/items.json`. O atlas vanilla
de itens inclui automaticamente apenas `textures/item/`; o fragmento do mod
acrescenta `entity/glider/` e `windcraft:entity/wind_glider`, sem substituir as
fontes vanilla. Texturas e modelos físicos/visuais de voo permanecem intactos.

Compilação e 41 JUnit aprovados: `build/verification-061-textures.log` (50 s).
Cliente real isolado com somente o JAR corrigido e Forge: atlas de itens criado,
zero avisos Missing textures para os 16 modelos, contra 16 anteriormente.
Antes: `qa/061/client-a/logs/latest.log`; depois: `qa/061/client-textures/logs/latest.log`.
A verificação não atesta percepção de cores, gameplay ou multiplayer.

## Rumo na decolagem — protocolo 8

S08 reproduzido com a física compilada: rumo sul (yaw 0), velocidade (0, −0,15, 0),
vento sul 10 m/s, bank 0. Z era −0,00270144 bloco/tick (contra o vento); no caminho
de voo corrigido é +0,00057380 (a favor). Com vento leste, X mudou de −0,00270144
para +0,00057380, enquanto o nariz permanece rumo sul. Resultado completo em
`build/heading-comparison.log`.

Captura de rumo no início do voo; em solo acompanha a escolha de direção até
saltar. Somente bank de A/D altera o rumo aéreo. Cliente/servidor usam o mesmo
cálculo e o renderizador usa rumo explícito, sem seguir velocidade ou yaw do
olhar em voo. Arrasto mantém deriva e sustentação não se inverte em fluxo de
cauda. A projeção da força evita componente de sustentação paralela ao ar.

Protocolo 8 adiciona um float de rumo a cada pacote: corpo medido no GameTest
agora tem 167 bytes (FlightSnapshot) e 51 bytes (FlightVisual), +4 cada, nas mesmas
condições do exemplo anterior. Cadência periódica mantida em 4/s a 20 TPS, com
envio extra no primeiro tick aéreo. Isso representa 872 bytes/s para piloto e
204 bytes/s por observador, somente corpos desses pacotes: não é captura de rede.
Atualizar cliente e servidor; protocolo 7 é incompatível mesmo se o nome for 0.6.1.

Testes de rumo cobrem cauda em seis direções, deriva lateral, vento extremo e
ascendentes, repouso sem impulso, curvas e wrap ±180°, planeio prolongado e
preservação das forças em voo alinhado. GameTest também cobre escolha de rumo
no chão/salto, olhar livre, câmera preservada e rumo recebido por observador.
Jogo real da correção, latência e percepção de controle permanecem pendentes.

## Artefato atual — rumo corrigido

`dist/windcraft-0.6.1.jar` (183377 bytes), protocolo 8.
SHA-256: `c27705b270d6c7b44831e42934f71d377ed36ba5394663823cb82ed519b43679`.

Compilação final: `gradlew.bat --offline --no-daemon --no-configuration-cache test build runGameTestServer`,
BUILD SUCCESSFUL em 1m30s. 49 JUnit sem falhas, erros ou ignorados; 4 GameTests
aprovados. Log `build/verification-061-takeoff-final.log`. JAR conferido com atlas
de itens, novo caminho de física e campo de rumo no snapshot. Não publicado.
O cliente que já estava aberto precisa ser reiniciado com este JAR para aplicar
o ajuste; não é possível atualizar as classes de voo somente recarregando recursos.
