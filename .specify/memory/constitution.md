# Constituição do Projeto — Caça-Palavras

Princípios não negociáveis que guiam todas as specs, planos e implementações deste repositório.
Qualquer decisão que viole um princípio precisa ser registrada como exceção no `plan.md` da feature.

## I. Spec antes de código
Toda funcionalidade nasce em `specs/NNN-nome/spec.md` (o QUÊ e o PORQUÊ), passa por `plan.md` (o COMO),
`tasks.md` (a ORDEM) e só então vira código. Mudou o comportamento? Atualize a spec no mesmo commit.

## II. Domínio puro e testável
Regras de jogo (geração do tabuleiro, seleção, pontuação, XP, conquistas) vivem em Kotlin puro,
sem dependência de Android, em `domain/`. Toda regra de progressão descrita em
`contracts/progressao-xp.md` tem teste unitário correspondente.

## III. Determinismo
Dado o mesmo seed, o gerador produz exatamente o mesmo tabuleiro. Isso garante que o Desafio Diário
seja igual para todos os jogadores e que bugs sejam reproduzíveis.

## IV. Pronto para online, sem anúncios *(emendado em 08/11/2026 — 003)*
Hoje o progresso fica no aparelho (DataStore) para testes via APK; o app **será online**, com servidor
como fonte da verdade. Por isso o estado é serializável, com ids estáveis, e o acesso passa por repositórios.
Sem anúncios. Sem SDKs de rastreamento.

## V. Progressão justa e monetização ética *(emendado em 08/11/2026 — 003)*
- **V-a** Todo XP, nível, estrela e nível de passe vêm de jogar. Nada disso é vendido.
- **V-b** O que é pago é só cosmético, conveniência ou conteúdo extra. Grátis e premium avançam na mesma velocidade.
- **V-c** Proibido: caixas aleatórias pagas, anúncios, pressão de tempo em ofertas, retirar recompensa grátis.
- **V-d** A trilha grátis do passe é completável jogando normalmente (verificado por teste).
- **V-e** Premium é sempre derivado de compras verificadas, nunca de um booleano editável (exceto no modo de teste atual).
A curva de nível deve dar a primeira subida de nível já na primeira partida e manter o ritmo de
1 nível a cada ~3–6 partidas no meio do jogo.

## VI. Toque primeiro, acessível sempre
Seleção por arrasto em 8 direções com "snap" e alternativa por dois toques (início → fim).
Alvos de toque ≥ 40dp, contraste AA no tema padrão, feedback háptico configurável.

## VII. Português do Brasil
Interface, palavras e documentação em PT-BR. Acentos aparecem na lista de palavras, mas o tabuleiro
usa letras maiúsculas sem acento (Á→A, Ç→C), como nos jogos de referência.

## VIII. Inspiração, não cópia
Mecânicas comuns do gênero podem ser usadas; nomes, arte, níveis e identidade visual são originais.
