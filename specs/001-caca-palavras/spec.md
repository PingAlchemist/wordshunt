# Especificação — Caça-Palavras com Progressão de XP

**Feature**: 001-caca-palavras · **Status**: Implementada (v1.0.0) · **Plataforma**: Android 8.0+ (API 26)

## Visão
Um caça-palavras em português para Android, offline e sem anúncios, que combina o melhor do gênero
(ver `research.md`) com um sistema de progressão de experiência: cada palavra encontrada dá XP,
o jogador sobe de nível, ganha títulos, moedas e desbloqueia modos e temas visuais.

## Histórias de usuário

### US1 — Jogar um nível (P1) 🎯 MVP
Como jogador, quero encontrar palavras escondidas numa grade arrastando o dedo, para me divertir e treinar o vocabulário.

**Critérios de aceite**
1. Dado um tabuleiro, quando arrasto de uma letra a outra, então a seleção segue a linha reta mais próxima
   entre as 8 direções (snap), destacada em tempo real.
2. Quando a seleção forma uma palavra da lista (em qualquer sentido permitido), então ela fica marcada com
   uma cor exclusiva no tabuleiro, riscada na lista, o aparelho vibra e um "+XP" aparece.
3. Quando a seleção não forma palavra, então o destaque some sem penalidade.
4. Como alternativa ao arrasto, posso tocar a primeira letra e depois a última.
5. Quando todas as palavras são encontradas, então vejo a tela de resultado.

### US2 — Ganhar XP e subir de nível (P1) 🎯 MVP
Como jogador, quero ganhar experiência e subir de nível para sentir que estou evoluindo.

**Critérios de aceite**
1. Cada palavra vale XP segundo `contracts/progressao-xp.md` (tamanho × combo × dourada).
2. Encontrar palavras em sequência rápida (≤ 6 s entre elas) aumenta o combo até ×2,0, exibido na tela.
3. Ao concluir, vejo o detalhamento: XP de palavras, conclusão, bônus sem dicas, bônus de velocidade,
   multiplicador diário e total.
4. A barra de XP do perfil anima até o novo valor; se passar de nível, aparece a celebração de **Level Up**
   com o novo título, moedas ganhas e o que foi desbloqueado.
5. O nível de jogador, título e barra de XP ficam visíveis na tela inicial.

### US3 — Jornada com dificuldade progressiva e estrelas (P1)
Como jogador, quero uma sequência de níveis que fica mais difícil aos poucos.

**Critérios de aceite**
1. O nível N da Jornada é sempre o mesmo tabuleiro (determinístico).
2. Grade começa 6×6 e cresce até 12×12; número de palavras vai de 4 a 12.
3. Direções: níveis 1–5 só → e ↓; 6–14 incluem diagonais ↘ ↗; 15+ todas as 8 (inclusive invertidas).
4. Cada nível dá 1–3 estrelas: começa com 3, perde 1 se usou dica, perde 1 se passou do tempo-alvo.
5. Posso rejogar níveis concluídos para melhorar estrelas; XP de conclusão de rejogadas vale 50%.

### US4 — Moedas e dicas (P2) — ⚠️ dicas substituídas por `specs/002-palavra-secreta` (US12)
1. Começo com 100 moedas. Ganho moedas ao concluir níveis, subir de nível e em conquistas.
2. Dica **Letra** (25 🪙): destaca a primeira letra de uma palavra ainda não encontrada.
3. Dica **Palavra** (60 🪙): revela uma palavra inteira (ela não dá XP de palavra).
4. Sem moedas suficientes, o botão fica desabilitado mostrando o custo.

### US5 — Desafio Diário com sequência (P2)
1. Um tabuleiro 10×10 por dia, igual para todos (seed = data), tema rotativo.
2. A primeira conclusão do dia vale XP ×1,5 + bônus de sequência (10 XP × dias seguidos, máx. 7) + 30 🪙.
3. A sequência aumenta se joguei ontem, mantém se já joguei hoje e volta a 1 se pulei um dia.
4. Depois de concluído, o card mostra "Concluído ✓" e não pode ser rejogado no mesmo dia.

### US6 — Modo Contra o Tempo (P2)
1. Desbloqueado no nível de jogador 5.
2. Começa com 90 s; cada palavra dá +5 s; limpar o tabuleiro gera outro e dá +15 s.
3. Fim do tempo → resultado com palavras encontradas, tabuleiros limpos, XP e recorde pessoal.

### US7 — Palavra Dourada (P2) — ⚠️ substituída pela Palavra Secreta em `specs/002-palavra-secreta` (US11)
A partir do nível 3 da Jornada (e no diário), uma palavra da lista é dourada ⭐ e vale XP ×2.

### US8 — Conquistas (P3)
12 conquistas (ex.: primeira palavra, 100 palavras, 10 níveis sem dica, sequência de 7 dias).
Cada uma dá 50 XP e 25 🪙 uma única vez e aparece num aviso ao ser desbloqueada.

### US9 — Temas visuais desbloqueáveis (P3)
Temas de cores (Clássico, Oceano, Floresta, Noite, Pôr do Sol, Dourado) liberados por nível de jogador
e selecionáveis numa tela própria.

### US10 — Perfil e estatísticas (P3)
Mostra nível, título, XP total, palavras encontradas, níveis concluídos, estrelas, recorde do Contra o Tempo,
maior sequência diária e opção de ligar/desligar vibração.

## Requisitos funcionais
- **FR-001** Gerar tabuleiros N×N (6 ≤ N ≤ 12) com palavras do tema, sem conflito de letras, preenchendo o
  restante com letras aleatórias ponderadas pela frequência do português.
- **FR-002** Mesmo seed ⇒ mesmo tabuleiro (Constituição III).
- **FR-003** Normalizar palavras: maiúsculas, sem acentos/cedilha, sem espaços ou hífens.
- **FR-004** Seleção com snap em 8 direções, limitada às bordas da grade.
- **FR-005** Aceitar palavra se a sequência de letras selecionada for igual à palavra ou ao seu reverso.
- **FR-006** Persistir progresso localmente e restaurá-lo ao abrir o app.
- **FR-007** Calcular XP, nível, título, estrelas e moedas exatamente como em `contracts/progressao-xp.md`.
- **FR-008** Banco de palavras com ≥ 18 temas e ≥ 20 palavras por tema, todas com 3–12 letras após normalizar.
- **FR-009** Funcionar sem internet; nenhuma permissão perigosa (apenas `VIBRATE`).

## Requisitos não funcionais
- **NFR-001** Geração de um tabuleiro 12×12 em < 50 ms num aparelho médio.
- **NFR-002** 60 fps durante o arrasto (tabuleiro desenhado num único Canvas).
- **NFR-003** APK debug < 20 MB.

## Fora do escopo (v1)
Ranking online, login, compras, anúncios, sons/música, outros idiomas.

## Casos de borda
- Palavra mais longa que a grade → descartada para aquele tabuleiro.
- Gerador não consegue encaixar palavras suficientes → tenta de novo com seed derivado (até 30 vezes).
- Letras de preenchimento formam por acaso uma palavra da lista em outro lugar → aceita (FR-005).
- Jogador sai no meio do nível → nível não conta; nada é perdido além do tempo.
- Relógio do aparelho volta no tempo → sequência diária não aumenta (só data estritamente posterior).
