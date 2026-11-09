# Contrato — Regras de Progressão (XP, nível, estrelas, moedas)

Fonte única da verdade para `domain/Progression.kt`. Cada regra tem teste em `ProgressionTest.kt`.

## 1. XP por palavra
```
xpPalavra = arred((10 + 2 × letras) × multCombo) × (secreta ? 2 : 1)
```
| combo (palavras seguidas, ≤ 6 s entre elas) | 1 | 2 | 3 | ≥ 4 |
|---|---|---|---|---|
| multCombo | 1,0 | 1,2 | 1,5 | 2,0 |

A Palavra Secreta (002) vale o dobro. A dica (25 🪙, uma por tabuleiro) só mostra a inicial da secreta e conta como dica (§2, §5).

## 2. XP de conclusão (Jornada e Diário)
| Componente | Valor |
|---|---|
| Conclusão | 40 + 5 × faixaDificuldade (0 a 5) |
| Sem dicas | +30 |
| Velocidade (tempo ≤ tempo-alvo) | +20 |
| Rejogada de nível já concluído | conclusão × 0,5 (bônus mantidos) |

`tempoAlvo (s) = 20 × palavras + 5 × tamanhoGrade`
`faixaDificuldade = min(5, (nível − 1) / 5)`

## 3. Diário
`total = arred((palavras + conclusão + bônus) × 1,5) + 10 × min(sequência, 7)` · +30 🪙

## 4. Contra o Tempo
`total = Σ xpPalavra + 25 × tabuleirosLimpos` · moedas = palavras encontradas

## 5. Estrelas
`estrelas = 3 − (usouDica ? 1 : 0) − (tempo > tempoAlvo ? 1 : 0)` (mínimo 1)

## 6. Moedas por nível concluído
`10 + 5 × estrelas` (+30 no Diário). Saldo inicial: 100.

## 7. Curva de nível do jogador
`xpParaSubir(n) = piso(100 × n^1,4)` — XP necessário para ir do nível n ao n+1.

| n | 1 | 2 | 3 | 4 | 5 | 10 | 20 |
|---|---|---|---|---|---|---|---|
| xpParaSubir | 100 | 263 | 465 | 696 | 951 | 2511 | 6628 |

Recompensa ao atingir o nível n: `20 + 5 × n` 🪙.

## 8. Títulos
| Níveis | Título |
|---|---|
| 1–4 | Aprendiz |
| 5–9 | Explorador |
| 10–19 | Caçador de Palavras |
| 20–34 | Mestre das Letras |
| 35–49 | Grão-Mestre |
| 50+ | Lenda |

## 9. Desbloqueios por nível de jogador
| Nível | Desbloqueio |
|---|---|
| 1 | Tema Clássico, Jornada, Desafio Diário |
| 3 | Tema Oceano |
| 5 | Modo Contra o Tempo |
| 8 | Tema Floresta |
| 12 | Tema Noite |
| 15 | Tema Pôr do Sol |
| 20 | Tema Dourado |

## 10. Conquistas (50 XP + 25 🪙 cada, uma vez)
| id | Nome | Condição |
|---|---|---|
| first_word | Primeiros Passos | 1 palavra encontrada |
| words_100 | Vocabulário | 100 palavras |
| words_1000 | Dicionário Ambulante | 1000 palavras |
| journey_10 | Viajante | 10 níveis da Jornada |
| journey_50 | Desbravador | 50 níveis da Jornada |
| three_stars_10 | Perfeccionista | 10 níveis com 3 estrelas |
| no_hint_10 | Purista | 10 níveis concluídos sem dica |
| combo_max | Em Chamas | atingir combo ×2,0 |
| secret_10 | Detetive | 10 palavras secretas (002) |
| streak_7 | Constância | sequência diária de 7 |
| speed_45 | Relâmpago | concluir um nível difícil (faixa ≥ 2: Jornada 11+ ou Diário) em ≤ 45 s |
| level_10 | Veterano | nível de jogador 10 |

XP de conquistas pode causar nova subida de nível (avaliado em laço até estabilizar).
