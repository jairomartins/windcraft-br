# Verificação — WindCraft 0.6.1

## Rumo da decolagem — protocolo 8 — 16/11/2026

Resultado final: **49/49 JUnit e 4/4 GameTests aprovados**, compilação concluída.
Comando: `gradlew.bat --offline --no-daemon --no-configuration-cache test build runGameTestServer`.
Log: `build/verification-061-takeoff-final.log`, BUILD SUCCESSFUL em 1m30s.

O cálculo anterior foi reproduzido sem cliente: piloto sem velocidade horizontal,
caindo a 0,15 bloco/tick, apontado para sul, vento de 10 m/s para sul e sem curva.
A primeira velocidade Z calculada era **−0,00270144 bloco/tick**, isto é, norte:
força artificial contra o vento. Com vento lateral leste, o primeiro X também
era negativo, contra o vento. A orientação visual seguia essa trajetória.

O controlador agora captura o rumo ao abrir no ar ou ao sair do chão. Enquanto
preparado no chão, ainda é possível escolher outra direção. Em voo, somente a
inclinação de A/D altera o rumo; o yaw da câmera não entra nesse cálculo.
Forças usam o rumo próprio da asa, com deriva pelo arrasto e sem sustentação
invertida em fluxo de cauda. A projeção da sustentação elimina força paralela ao
fluxo. Coeficientes e limites não foram alterados.

Servidor e previsão local compartilham o cálculo; poses usam rumo explícito em
vez da direção da velocidade. O servidor envia o estado no primeiro tick aéreo
e depois a cada 5 ticks. Os codecs acrescentam rumo ao snapshot e ao visual;
**protocolo 8, incompatível com JARs antigos de protocolo 7, inclusive 0.6.1**.

Oito testes novos cobrem cauda em seis rumos, vento lateral, manutenção da
orientação, curvas e passagem por ±180°, voo prolongado, ausência de impulso
em repouso, vento extremo com ascendentes e voo normal alinhado. O GameTest
de ciclo verifica escolha do rumo no chão/salto, olhar livre em voo, ausência de
alteração da câmera e rumo do observador; codecs fazem round-trip também do rumo.

Validação dentro do Minecraft da mudança de direção ainda pendente: as tentativas
de observação foram interrompidas pelo controle da janela e não comprovam o ajuste.
Roteiro: reiniciar com o novo JAR, preparar olhando para longe da encosta, saltar
sem A/D com vento de cauda/lateral, mover o olhar e então testar A/D. Conferir em
segunda conexão com o mesmo JAR. Vento forte ainda pode arrastar o piloto; manter
o rumo não garante avanço contra qualquer vento nem imunidade a colisões.

## Correção do atlas de itens — 16/11/2026

Falha reproduzida no cliente isolado: 16 avisos `Missing textures in model`
para `windcraft:item/glider/fan_*`. Os PNGs existiam, mas estavam em
`textures/entity/glider/`, fora do diretório carregado pelo atlas de itens.

Adicionado `assets/minecraft/atlases/items.json` com a fonte dos tecidos
coloridos e a textura base. Nenhum PNG, UV ou modelo de voo foi modificado.

`gradlew.bat --offline --no-daemon --no-configuration-cache test build` passou
em 50 s; 41 testes JUnit aprovados, sem falhas, erros ou ignorados. O JAR foi
conferido: todas as 16 variantes apontam para sprites registrados no atlas.

O JAR corrigido iniciou num novo diretório `qa/061/client-textures`, somente
com WindCraft e Forge 66.1.8. O log confirma criação do atlas de itens e **zero
avisos de texturas ausentes**, contra 16 no cliente anterior. Evidências locais:
`build/verification-061-textures.log`, `qa/061/client-a/logs/latest.log` (antes)
e `qa/061/client-textures/logs/latest.log` (depois). A consulta de Realms falhou
por usar identidade local de teste; não impediu o carregamento dos recursos.

Esta verificação cobre carregamento e bake dos modelos. Não equivale à revisão
visual das 16 cores durante voo nem encerra os testes de gameplay e multiplayer.

## Candidata local — 16/11/2026

Executado no Windows 11 com Java 25, Minecraft 26.3 e Forge 66.1.8:

```powershell
$env:GRADLE_USER_HOME='D:\Programacao\mods\.gradle'
.\gradlew.bat --offline --no-daemon --no-configuration-cache test build runGameTestServer
```

Resultado: compilação e **41/41 testes JUnit, zero falhas, erros ou ignorados;
4/4 GameTests aprovados**. O servidor de GameTests iniciou em ambiente SERVER
com os mixins do mod; isso verifica carregamento nesse ambiente, mas não equivale
à instalação limpa do JAR em um servidor dedicado de produção.

O novo GameTest cobre cancelamento no mesmo tick, uso e fechamento por item,
reabertura, comandos antigos/NaN, preparação, timeout, decolagem, remoção do
equipamento e retorno ao movimento vanilla. Os testes de receitas verificam
as 16 cores, persistência e rejeição de lã misturada. Auditoria estática confirmou
JSONs válidos, conjuntos de modelos/texturas/receitas das 16 cores e mesmas
chaves de tradução pt_br/en_us. Não comprova aparência nem áudio.

Medição do corpo dos codecs no GameTest: FlightSnapshot 163 bytes e FlightVisual
47 bytes no exemplo usado, sem framing do canal. Não foram medidos FPS, MSPT
ou tráfego de multiplayer real; não houve otimização nem alteração de balanceamento.
O log de inicialização contém aviso de atraso no preparo dos testes e bloqueio
da consulta de atualização do Forge pela rede; a suíte concluiu apesar deles.

As tentativas iniciais falharam por cache padrão em C:\.gradle e restrição de
rede ao resolver metadados do Minecraft. Após resolver metadados em processo
novo com rede, a execução offline completou. Relatórios antigos não foram
usados como prova de nova aprovação. Logs locais: build/verification-061-fixed.log,
build/verification-061-final.log e build/verification-061-release.log.

**Ainda não liberada como estável.** O carregamento limpo do cliente foi verificado
na correção do atlas acima. Dois clientes reais, latência, testes visuais/auditivos
e matriz de colisões permanecem pendentes. O servidor instalado limpo iniciou no
teste anterior com o JAR anterior à correção do atlas; não constitui nova validação
do JAR corrigido. HUD ainda tem texto português fixo. O diagnóstico distingue
falhas, reforços de limpeza e suspeitas descartadas. Roteiro manual e método de
medição em [STABILITY_0.6.1.md](docs/STABILITY_0.6.1.md).

## Histórico — 0.6.0

## Resultado atual — 16/11/2026

`test build runGameTestServer` passou com **41/41 testes JUnit e 3/3 GameTests**.
Foram verificadas as 16 receitas por cor, rejeição de lãs misturadas, persistência
da cor no ItemStack, codecs de vento/roll/cor e ausência dos registros suspensos.
As capturas enviadas pelo autor mostram o leque laranja, pose, HUD e diferentes
ângulos em jogo. Elas não comprovam todas as cores, pousos, latência ou dois clientes.

Ainda pendentes: teste visual das 16 cores, primeira pessoa, pousos/colisões,
áudio, FPS com vários pilotos e multiplayer com dois clientes reais.
Detalhes em [GLIDER_FLIGHT_SYSTEM.md](docs/GLIDER_FLIGHT_SYSTEM.md).

## Histórico da validação 0.5.0

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
