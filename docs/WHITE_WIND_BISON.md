# Bisão Branco dos Ventos

Modelo original criado por cubos, sem copiar geometria ou textura da referência.
O link do Sketchfab retornou HTTP 403; foi usada a especificação e a imagem anexa
para orientar a silhueta. O PNG anexo não é o modelo entregue.

## Arquivos para edição

- Abrir `docs/models/white_wind_bison.bbmodel` no Blockbench.
- A textura de 128 × 128 está incorporada e também em `white_wind_bison.png`.
- São 43 cubos em 19 grupos, incluindo todos os 14 grupos solicitados.
- Manto, arreio, franjas e adornos dos chifres são editáveis separadamente.
- O preview PNG é uma projeção auxiliar da geometria e das cores médias do atlas;
  não é captura do Minecraft nem confirmação de abertura no Blockbench.

O arquivo usa projeto Java Entity (`modded_entity`), box UV explícito e pivôs.
O modelo Java é gerado da mesma especificação do `.bbmodel`, sem dependência
GeckoLib nem uso direto de um arquivo Bedrock como renderer Java.

O script `tools/create_bison.py` é a fonte de geometria/UV para regeneração dos
dois formatos. **Regenerar sobrescreve o `.bbmodel`**; preserve uma cópia se editar
manualmente. Alterações feitas no Blockbench não são importadas automaticamente
pelo script ou pelo jogo. Para integrá-las, atualize a especificação no script
ou adapte a classe Java, verificando UVs e pivôs.

## Animação e comportamento

No jogo: respiração discreta, movimento da cauda, orientação da cabeça e caminhada
com pares diagonais opostos são implementados em `setupAnim`. Os chifres, barba,
focinho e olhos seguem a cabeça por hierarquia. O projeto contém keyframes
de referência `idle` e `walk`; sua reprodução depende do modo de animação
suportado pelo Blockbench para Java Entity e ainda precisa ser verificada na UI.
O renderer Java usa animação por código, não lê esses keyframes em runtime.

O jogo agora mistura poses procedurais `idle`, `walk` e `run`, com caminhada
pesada em `BisonAnimations`. Veja `BISON_MONK_IMPROVEMENTS.md` para limites,
textura do ovo, validação e testes manuais. Os keyframes do Blockbench continuam
sendo referências idle/walk, sem controlar a animação do jogo.

`run`, `attack`, `rest`, `head_move` e `tail_move` não são clipes independentes
implementados. A entidade reage a dano com IA de ataque corpo a corpo, mas não
tem animação de investida. Não há montaria, cargas, reprodução ou domesticação.

Hitbox: 1,5 × 2,25 blocos; saúde: 60; velocidade de atributo: 0,16; resistência
a empurrão: 0,8. O comprimento visual é aproximadamente 2,7 blocos, e os chifres
ultrapassam lateralmente a hitbox quadrada padrão. Avaliar colisões em corredores.
A navegação aceita no máximo uma queda de bloco, inclusive em combate; isso
reduz escolhas de trajetos perigosos, mas não impede empurrões ou acidentes.

Sem spawn natural. Invocar por `/summon windcraft:white_wind_bison` ou usar o ovo
na aba de ovos criativos. Não há alteração em vacas, aldeões ou outras entidades
vanilla. A entidade permanece no mundo por persistência padrão de Mob.

## O que foi verificado

O validador confere unicidade dos cubos, grupos obrigatórios, referências da
hierarquia, dimensões positivas, textura incorporada idêntica ao recurso do jogo,
UVs de todas as faces dentro de 128 × 128 e keyframes idle/walk presentes.
O GameTest cria e mantém o bisão vivo no servidor com atributos registrados.
Um teste cliente sem janela faz bake da geometria e verifica animações diagonais.

Ainda necessário: abrir efetivamente o arquivo no Blockbench, conferir UVs por
todos os lados, testar pivôs na UI e observar textura/animação no cliente Minecraft.
Compilação e teste de servidor não comprovam qualidade visual ou renderização GPU.
