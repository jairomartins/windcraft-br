# Sistema de voo — reformulação por marcos

## Correção 0.6.1 — rumo próprio e protocolo 8

O rumo é capturado ao abrir no ar ou no salto após preparar no chão. A câmera
continua livre; A/D inclinam e alteram o rumo. A física usa a orientação da asa
em vez de escolher a direção do vento relativo. Arrasto mantém deriva; fluxo
por trás não gera sustentação invertida que lance o piloto de volta à encosta.
O vento forte ainda pode produzir recuo no solo; não há propulsão ou imunidade.

Servidor e previsão local guardam rumo; FlightSnapshot e FlightVisual o enviam
ao piloto e aos observadores. A pose usa esse rumo, separado da trajetória.
Primeiro estado aéreo enviado no tick inicial, depois a cada 5 ticks. Protocolo 8:
atualizar todos os clientes e o servidor, inclusive builds anteriores da 0.6.1.
O restante deste documento descreve o desenvolvimento histórico da base 0.6.0.

## Inspeção e dependências antes da remoção

Base: Minecraft 26.3, Forge 66.1.8, ForgeGradle 7.0.40, Java 25,
mappings oficiais. Item preservado: `windcraft:wind_glider`.
O estado anterior, incluindo arquivos não rastreados e diferenças Git, está
no ZIP em `recovery/pre-glider-rework-*`. Alterações de publicação e versão
preexistentes não são parte desta reformulação.

Dependências observadas:

- `WindCraft` registra itens, entidades, comandos e testes da vila.
- `VillageCommands` usa `VillageBlueprint`, `VillagePlacement` e `WindAPI`.
- `VillagePlacement` escreve os módulos apenas por comando; não há worldgen.
- `VillageGameTests` depende da vila, monge e bisão; não é utilitário de voo.
- `VillageEntities` registra bisão e monge no mesmo registrador. Bisão não
  depende de monge, vila, profissões ou comércio. Não existe harpia no código.
- `VillageRenderers` registra ambos os renderers; o do bisão deve permanecer.
- `GliderFlightController` e `GliderClient` compartilham `GliderPhysics`.
- `WindAPI.getWindField` fornece vento horizontal e correntes ascendentes.
- Mixins de travel cancelam travel vanilla somente em voo gerenciado.
- `WindNetwork` envia e valida eixos de controle; não recebe velocidade desejada.
- A camada `GliderModelRenderer` e os mixins de avatar compartilham pose de
  corpo e armadura. A câmera não é modificada.

## Marcos

1. Limpeza: arquivar módulos exclusivos de vila/monge, remover registros ativos,
   preservar bisão e recursos compartilhados. Compilar antes do próximo marco.
2. Visual: leques laranja segmentados, haste longitudinal e cauda menor.
3. Controle: roll persistente, retorno suave e sincronização explícita.
4. Física: curva proporcional ao roll, sustentação e limites numéricos.
5. Vento: manter WindAPI e a física relativa ao ar existente.
6. Estabilidade: testes automatizados; registrar separadamente validações manuais.

## Alteração de escopo confirmada durante o trabalho

O usuário também pediu a remoção do bisão. Seus arquivos de entidade, registro,
renderer, animação, texturas, ovo, testes, geradores e modelos editáveis foram
movidos para `archive/wind-society/`, junto aos módulos de monge/vila. O mod
ativo não registra entidades ou ovos próprios. Os documentos anteriores sobre
vila/bisão/monge são históricos; este documento descreve o estado atual.
Ao abrir mundos antigos, use uma cópia: os IDs de monge e bisão não existem
mais nesta versão, e essas entidades não serão mantidas pelo carregamento.

## Modelo e renderização

`tools/create_fan_glider.py` é a fonte de 740 segmentos sólidos. Gera:

- `rendering/GliderModel.java`: `ModelPart` com haste, varetas, leques e barra.
- `assets/windcraft/textures/entity/wind_glider.png`: atlas original 128 × 128,
  madeira na metade superior e tecido laranja na inferior.
- `assets/windcraft/models/item/wind_glider_fan.json`: mesma geometria reduzida,
  para inventário e mãos, com dimensões nos limites do formato de item.
- `assets/windcraft/items/wind_glider.json`: aponta ao novo modelo do item.
- `docs/models/wind_glider_fan.bbmodel`: geometria editável com box UV explícito.
- `docs/models/wind_glider_fan_preview.png`: projeção superior auxiliar;
  não é captura do jogo.

O eixo X é lateral, Y longitudinal e Z aponta acima do jogador na pose de voo.
O corpo continua usando a pose suspensa/prona já existente. A barra mantém
Y=-4,66 e Z=7,46 pixels, preservando a âncora das mãos clássicas e finas.
Todo o equipamento usa um único root e acompanha a mesma transformação da
skin/armadura. A abertura expande a largura, sem soltar as varetas. A asa deixa
de ser um único plano retangular: os leques são volumes voxel com espessura.
O item é ocultado nas mãos durante o voo; em primeira pessoa o evento já
existente oculta a mão equipada para preservar visibilidade. Não há mudança
de câmera/FOV. Verificação GPU e clipping continuam manuais.

Regenerar sobrescreve Java, JSONs, atlas e projeto Blockbench: preserve cópias
de edições manuais antes de executar o gerador. Edições no Blockbench não são
importadas automaticamente para o runtime.

## Controles e inclinação

Uso do item prepara/fecha; Shift fecha; W/S preservam a lógica de picada/nariz;
Espaço mantém o flare curto confirmado pelo servidor. A/D agora comandam roll
persistente, em vez de deslocar uma direção-alvo temporária. Mouse mantém
olhar e contribuição de pitch, mas não comanda mais a guinada no voo ativo:
isso evita desfazer a curva para voltar ao rumo da câmera ao soltar A/D.
Tooltips pt-BR/en-US foram atualizados.

`GliderPhysics.updateBank` aproxima o ângulo-alvo com velocidade angular
limitada por tick. Defaults: ±25°, 8 ticks (0,4 s) para atingir o limite e
11 ticks (0,55 s) para nivelar a partir do limite. A inversão completa exige
duas rampas. A/D simultâneos se anulam; menus produzem eixos neutros. As teclas
vanilla não são substituídas fora do voo gerenciado.

Configurações de servidor em `windcraft-glider-server.toml`:

| Parâmetro | Default | Função |
|---|---:|---|
| maxBankDegrees | 25 | Ângulo máximo, configurável de 5 a 40° |
| bankSeconds | 0,4 | Tempo de entrada a partir do nível |
| levelSeconds | 0,55 | Tempo de retorno ao nível |
| controlSensitivity | 1 | Escala do comando lateral |
| maxSpeed | 1,1 | Limite horizontal em blocos/tick |
| airDensity / wingArea | 1,225 / 12 | Pressão dinâmica e área |
| lift / drag | 1 / 0,08 | Sustentação e arrasto |
| windInfluence / turbulence | 0,35 / 1 | Acoplamento e oscilações |
| thermals / thermalStrength | true / 8 | Ascendentes, limite em m/s |

## Física e vento

`GliderPhysics` permanece independente do Minecraft. O overload com roll
explícito é usado pelo voo ativo; o overload anterior permanece para chamadas
legadas e testes. O roll rotaciona a sustentação: componente lateral curva a
velocidade; componente vertical é reduzida por cos(roll), gerando maior perda
de altitude na curva. Não é impulso lateral de caminhada. Gravidade, arrasto,
stall ao puxar demais o nariz e limitação de aceleração permanecem ativos.

Velocidade relativa = velocidade do jogador menos vetor do vento. Por isso
vento de cauda/contrário alteram alcance em relação ao solo sem uma penalidade
artificial fixa na sustentação. Vento lateral cria deriva; ascendentes mudam
a componente vertical relativa. `WindAPI.getWindField` continua sendo a única
fonte, com influências de terreno, calor, altitude e hora já existentes em
`UpdraftService`. Rajadas já entram na amostragem horizontal; turbulência é
derivada da intensidade pelo mesmo critério do campo existente. O tempo e a
seed enviados pelo servidor tornam a oscilação reproduzível, sem outro gerador.
Oscilações de trajetória também aparecem no pitch visual filtrado.

Limites: pressão avaliada até 35 m/s, aceleração horizontal ±0,06 por tick,
vertical de -0,06 a +0,08, velocidade vertical de -4 a +0,25 blocos/tick,
vento até 60 m/s e campo vertical entre -16 e +16 m/s. Entradas não finitas
são rejeitadas; limites e tempos de banking são validados. A simulação não
adiciona propulsão, e os testes prolongados em ar parado continuam descendo.

## Sincronização

`GliderPlayerData.bank` é estado efêmero independente por piloto, calculado no
servidor a 20 Hz a partir de eixos validados. `GliderClient` prevê o mesmo cálculo
e recebe correção a cada snapshot. Somente eixos/ações e sequência entram no
servidor; cliente não escolhe vento, velocidade nem roll autoritativo.

`WindNetwork` passa ao protocolo **7**: os snapshots levam configurações de
banking e roll, e os pacotes para observadores levam o roll do servidor.
Cliente e servidor precisam desta mesma versão do mod. `GliderVisuals` usa o
roll previsto para o piloto local e o recebido para outros jogadores, com
filtro por tick e interpolação por frame em `GliderVisualState`.

`GliderAvatarRendererMixin` continua aplicando roll somente ao estado visual
e PoseStack do avatar, sem alterar câmera ou yaw real. A física substitui
travel vanilla uma vez em cada lado: previsão no cliente, validação no servidor.
A reconciliação existente usa movimento vanilla e correções periódicas. O
controle de deslocamento anormal existente é conservador, não é um sistema
completo de anticheat. Morte, logout, dimensões e fechamento descartam o estado.

## Registro dos marcos e pendências

Resultado final desta entrega: **41/41 testes JUnit e 2/2 GameTests** (um smoke
próprio e um teste padrão) passaram. `test build runGameTestServer` terminou
com sucesso. O smoke confirma ausência de registros do bisão/monge, preservação
do planador, voo limitado com WindAPI e round-trip dos codecs de snapshot e
roll para observadores. Foram conferidos no JAR a ausência dos módulos
arquivados, referências do item/PNG, dimensões legais dos 740 elementos e UVs
do Blockbench dentro do atlas. Integridade do ZIP de recuperação verificada.

Arquivos ativos alterados nesta reformulação: `WindCraft`, `GliderConfig`,
`GliderPhysics`, `GliderPlayerData`, `GliderFlightController`, `WindNetwork`,
`GliderClient`, `GliderVisuals`, `GliderVisualState`, `GliderModel` e
`GliderModelRenderer`; novos `FlightGameTests`, `GliderBankTest`, gerador e
recursos do leque. `GliderVisualStateTest` teve o limite atualizado para a
faixa configurável de 40°. Idiomas perderam as entradas de entidades suspensas
e receberam controles atualizados. A lista de recursos gerados está acima;
fontes removidas do build ficam em `archive/wind-society`.

| Marco | Resultado automatizado | Pendente |
|---|---|---|
| 1 — limpeza | test/build passou; fontes e recursos arquivados | Conferir cópia de mundo antigo |
| 2 — visual | test/build passou; projeção inspecionada | Primeira/terceira pessoa, inventário e GPU |
| 3 — controle | compilação e testes passaram | Sensação de A/D, retorno e câmera em jogo |
| 4 — física | testes de curva, descida, stall, flare e limites passaram | Pousos e manobras reais |
| 5 — vento | testes de cauda/contrário/lateral, térmica e turbulência passaram | Voo em diferentes terrenos/ventos |
| 6 — estabilidade | JUnit e servidor GameTest; codecs e registros inspecionados | Dois clientes, latência, FPS e reinício real |

Não considerar os critérios visuais e multiplayer plenamente aceitos só por
compilar. Não foram executadas sessões com dois clientes, teste GPU, medição
de FPS ou reinicialização de servidor com pilotos conectados. O modelo tem
740 cubos; custo com muitos jogadores visíveis requer medição e pode motivar
fusão de segmentos coplanares no futuro.

Para testar em cópia descartável de mundo com cheats:

```mcfunction
/give @s windcraft:wind_glider
/give @s windcraft:anemometer
/wind info
```

Prepare o planador com o uso do item, salte de uma altura segura e teste A/D,
retorno ao nível, W/S, flare e fechamento; repita com vento fraco, forte,
lateral e corrente ascendente. Confira ambas as mãos, skins clássica/fina,
armaduras e câmeras. Em servidor, use dois pilotos e confira o roll remoto,
fechamento, perda de item, morte, troca de dimensão e desconexão/reconexão.

Futuros: QA visual, ajuste da suspensão/pose conforme gameplay, malha mais
econômica, suavização/replay de previsão sob latência e telemetria de desempenho.

## Cor da lã e ícone (16/11/2026)

As 16 receitas shaped exigem quatro lãs da mesma cor, com os demais materiais
e posições preservados. `wind_glider.json` é a receita laranja; as outras
quinze usam sufixos de cor. O resultado contém `minecraft:custom_model_data`
com `strings[0]` igual ao nome vanilla da cor. Esse componente é salvo e
sincronizado pelo ItemStack; `GliderColors` valida a paleta e usa laranja para
itens antigos ou valores desconhecidos. Misturar lãs não produz resultado.

`tools/create_glider_colors.py` gera 16 atlas com madeira idêntica e apenas
o tecido recolorido, ícones pixel art 32 × 32 e modelos correspondentes.
PNG de voo: `textures/entity/glider/<cor>.png`; ícone:
`textures/item/glider/<cor>.png`. A definição do item seleciona a cor pelo
componente e usa ícone em GUI e modelo 3D nas mãos/mundo. O ícone legado
`textures/item/wind_glider.png` também foi atualizado para o leque laranja.
O gerador principal chama o gerador de cores para não perder as variantes.

O servidor lê a cor do ItemStack da mão que realmente ativou o planador e
envia-a no FlightVisual (protocolo 7), inclusive para novos observadores.
`GliderVisuals` e o estado extraído do avatar preservam a cor por piloto;
o renderer seleciona o atlas correspondente. Assim, dois itens de cores
diferentes nas mãos não confundem a cor do equipamento ativo.

Conferir visualmente as variantes no inventário, mãos e voo com dois clientes
continua pendente. Cliente e servidor precisam deste mesmo JAR/protocolo.

Validação das cores: **41/41 JUnit e 3/3 GameTests** passaram. O novo GameTest
fabrica cada uma das 16 variantes pelas receitas carregadas no servidor,
confere o resultado, serializa/restaura o ItemStack e rejeita lãs misturadas.
O teste de codec também cobre a cor do pacote para observadores. Conferidos
os 16 modelos/ícones/atlas e a identidade dos pixels de madeira entre cores.
