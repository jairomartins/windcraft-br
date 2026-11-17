# WindCraft BR — alterações

## 0.6.1 Alpha — candidata local — 16/11/2026

- Corrigida a inversão na decolagem: rumo capturado no salto e alterado por A/D,
  independente da direção do vento e do olhar. Deriva preservada; fluxo de cauda
  não produz mais sustentação com lançamento artificial contra o vento.
- Piloto e observadores usam rumo sincronizado também na pose; protocolo 8.
- Corrigidas texturas ausentes nos modelos de item das 16 cores: os tecidos usados
  também pelo renderizador de voo agora são incluídos no atlas de itens.
- Shift para fechar deixa de ser descartado quando outro comando chega no mesmo tick.
- Fechamento transmite cor e inclinação originais antes de remover o estado do piloto.
- Snapshot inicial do observador passa a usar o mesmo estado de preparação das atualizações periódicas.
- Fechar por comando ou uso do item encerra o uso do planador gerenciado.
- Logout anuncia fechamento antes da limpeza do estado.
- Novo GameTest cobre preparação, cancelamento agrupado, entradas inválidas/antigas,
  timeout já existente, reabertura, toggle, remoção do item e retorno ao movimento vanilla.
- Coeficientes físicos, controles, receitas, cores e dependências preservados.

Validação automatizada e limitações em [VALIDATION.md](VALIDATION.md).
Diagnóstico, critérios de conclusão e roteiro em [STABILITY_0.6.1.md](docs/STABILITY_0.6.1.md).
Continuam pendentes cliente limpo, aparência/áudio, colisões, desempenho real e
multiplayer com dois clientes e latência. Não declarada estável; sem publicação.

## 0.6.0 Alpha — 16/11/2026

- Planador original com asas voxel em leque, varetas de madeira e cauda menor.
- Tecido e ícone em 16 cores, definidos pelas quatro lãs da mesma cor no craft.
- Inclinação progressiva em A/D, curvas físicas e retorno suave ao nível.
- Roll validado no servidor e transmitido para os observadores; protocolo 7.
- Vento relativo, deriva, ascendentes, arrasto e limites de velocidade mantidos.
- Vila, monge e bisão suspensos; fontes arquivadas fora do código ativo.
- Seis capturas reais de gameplay e documentação atualizada.

Validação: 41 testes JUnit e 3 GameTests passaram. As capturas do autor mostram
o modelo laranja e HUD em jogo. Testes completos de todas as cores, áudio,
colisões, desempenho e dois clientes com latência continuam pendentes.

Use o mesmo JAR no cliente e no servidor. Ao testar mundos antigos que
contenham monge/bisão, preserve uma cópia: seus registros foram retirados.

Download: [windcraft-0.6.0.jar](dist/windcraft-0.6.0.jar).
SHA-256: `8645e0591a5ec15a3001794a8ffcfb57efe2ef5fe41659bf2756060fe6c33540`.
