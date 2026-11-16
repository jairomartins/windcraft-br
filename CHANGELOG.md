# WindCraft BR — alterações

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
