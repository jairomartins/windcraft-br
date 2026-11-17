# WindCraft BR 0.6.1 — Alpha

Atualização de estabilidade do vento e do planador, preservando coeficientes,
limites, modelo em leque e 16 cores, com correção do rumo na decolagem.

- Efeitos visuais de chuva, neve, areia e neblina respondem ao vento.
- Rumo da decolagem preservado: não vira automaticamente contra o vento.
- Curvas por A/D e deriva do vento mantidas; olhar livre não muda o rumo.
- Rumo sincronizado entre física e modelo para piloto e observadores.
- Corrigido o registro das texturas das 16 cores no atlas de itens, eliminando
  texturas ausentes nos modelos de planador segurados na mão.
- Corrigido cancelamento por Shift quando comandos chegam agrupados no mesmo tick.
- Cor e inclinação preservadas no pacote de fechamento.
- Estado inicial de observadores coerente com as atualizações do planador preparado.
- Fechamento encerra o uso do item; logout anuncia fechamento antes da limpeza.
- Novo teste de integração de ciclo do planador no servidor.

Minecraft Java 26.3, Forge 66.1.8 e Java 25. Cliente e servidor devem usar o
mesmo JAR atualizado do WindCraft 0.6.1 (protocolo 8). Builds anteriores da 0.6.1
com protocolo 7 são incompatíveis. Remova JARs antigos da pasta mods.
Não requer novas dependências.

Testes automatizados não substituem jogo real: instalação limpa, 16 cores em
tela, áudio, pousos/colisões, multiplayer com latência e desempenho continuam
pendentes. Consulte VALIDATION.md e docs/STABILITY_0.6.1.md no código-fonte.

Validação automatizada: compilação, 53 testes JUnit e 4 GameTests aprovados.

SHA-256 do JAR local: `949b0ed14aeffdf65a0b38adb81d3320e81a6cd1f64b2a6ff318170898e68753`.

Carregamento real dos recursos do JAR corrigido: atlas de itens criado, zero avisos
de texturas ausentes, contra 16 antes da correção. Essa verificação foi feita antes do ajuste de rumo; gameplay completo ainda pendente.
