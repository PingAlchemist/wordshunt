# Especificação — Palavra Secreta e Dica de Inicial

**Feature**: 002-palavra-secreta · **Status**: Implementada · **Altera**: 001 (US4 Dicas, US7 Palavra Dourada)

## Motivação (pedido do jogador)
> "Tem que ter uma palavra que não tem dica; se eu demorar pra encontrar, aperto o botão e ganho uma dica
> com a inicial da palavra. Não mostra no quadro, só dá a letra inicial."

As dicas da v1 (destacar a letra no quadro / revelar a palavra inteira) tiravam o desafio. A nova mecânica
mantém o jogador procurando: a dica só diz **com que letra começa** a palavra escondida.

## Histórias de usuário

### US11 — Palavra Secreta (P1)
Como jogador, quero que uma das palavras do nível fique escondida na lista, para ter um desafio extra.

**Critérios de aceite**
1. Todo tabuleiro (Jornada, Diário e Contra o Tempo) tem exatamente **uma** Palavra Secreta.
2. Ela é sempre a **última** da lista e aparece só como tracinhos com o número de letras: `🔒 _ _ _ _ _ _`.
3. Ela está no quadro como as outras, nas direções permitidas para o nível, e vale **XP ×2**.
4. Ao ser encontrada, a lista mostra a palavra completa (riscada, em dourado) e o quadro a destaca em dourado.
5. Substitui a "Palavra Dourada" da v1 (que ficava visível e marcada com ⭐).

### US12 — Botão Dica (P1)
Como jogador, quando demoro para achar a Palavra Secreta, quero pedir uma dica com a letra inicial.

**Critérios de aceite**
1. Há **um único** botão `💡 Dica · 25🪙`. Os botões antigos "Letra" e "Palavra" deixam de existir.
2. Ao tocar, a lista passa a mostrar a inicial: `🔒 Q _ _ _ _ _`. **Nada muda no quadro.**
3. A dica só pode ser usada **uma vez** por tabuleiro e só enquanto a Palavra Secreta não foi encontrada;
   depois disso o botão fica desabilitado ("Dica usada" / "Secreta encontrada").
4. Sem moedas suficientes, o botão fica desabilitado mostrando o custo.
5. Usar a dica conta como dica: perde o bônus "Sem dicas" (+30 XP) e 1 estrela.

## Requisitos funcionais
- **FR-010** O gerador marca exatamente uma palavra como secreta em todo tabuleiro; ela é ordenada por último.
- **FR-011** O motor expõe `hintSecretInitial()`, que devolve a inicial da Palavra Secreta, incrementa `hintsUsed`
  uma única vez e não altera nenhuma célula destacada.
- **FR-012** A conquista "Garimpeiro" (10 palavras douradas) passa a ser "Detetive" (10 palavras secretas).

## Fora do escopo
Mais de uma dica por palavra, dicas para as palavras visíveis, dicas gratuitas por tempo.
