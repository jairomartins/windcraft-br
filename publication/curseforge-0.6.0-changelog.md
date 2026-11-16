# WindCraft BR 0.6.0 — Alpha

## Novidades

- Novo planador com asas em leque, estrutura de madeira e pequena cauda traseira.
- Tecido e ícone em 16 cores: use quatro lãs da mesma cor na receita de fabricação.
- Inclinação progressiva com A/D, curvas reais e retorno suave ao soltar os controles.
- Cor e inclinação do planador transmitidas pelo servidor aos outros jogadores.
- Integração com vento dinâmico, deriva lateral, correntes ascendentes, sustentação e arrasto.
- Desenvolvimento concentrado no vento e no planador; vila, monge e bisão foram retirados do código ativo.

## Requisitos

- Minecraft Java 26.3
- Forge 66.1.8 ou posterior compatível com Minecraft 26.3
- Java 25
- Instale o mesmo WindCraft 0.6.0 no cliente e no servidor (protocolo de rede 7).

## Fabricação e controles

A receita mantém 4 lãs, 1 bambu, 2 gravetos e 2 linhas. As quatro lãs precisam
ter a mesma cor; misturar cores não produz resultado. Planadores antigos sem
cor salva permanecem laranja.

Use o item para preparar/fechar, A/D para inclinar e curvar, W/S para controlar
a picada/nariz, Espaço para a manobra breve de sustentação e Shift para fechar.
O mouse permite olhar e contribui para o pitch; não comanda mais a guinada.
Preparar no chão não cria propulsão: é necessário saltar ou partir de uma altura.

## Validação e limitações

Compilação concluída com 41 testes JUnit e 3 GameTests aprovados. As capturas
reais do autor mostram o novo modelo laranja e HUD em jogo. A versão continua
Alpha: testes completos de todas as cores, áudio, pousos/colisões, desempenho
e multiplayer com latência ainda estão pendentes.

Ao usar mundos antigos com entidades de monge ou bisão, preserve uma cópia:
esses registros foram retirados desta versão. Abrir o planador perto do chão
ou colidir em alta velocidade ainda pode causar dano.

Site: https://windcraft-br-site.vercel.app/pt-BR
Código e documentação: https://github.com/jairomartins/windcraft-br
