# Pong em Java

Avaliação final de Programação Orientada a Objetos — jogo entre um jogador e o computador, usando somente a biblioteca padrão do Java (Swing/AWT).

**Identificação:** Mikael Machado Souza — RA 2161392613027
**Versão do JDK:** OpenJDK 21.0.12.1 (compilado e testado com JDK 21)

## Como executar

Todos os `.java` na mesma pasta, sem `package`. Nessa pasta:

```
javac -encoding UTF-8 *.java
java Main
```

Requer um JDK completo e ambiente de desktop (módulo `java.desktop`). Em IDE: abrir a pasta como projeto Java simples e executar `Main`.

## Controles

| Tecla | Ação |
|---|---|
| W ou ↑ | Subir a raquete |
| S ou ↓ | Descer a raquete |
| 1 / 2 ou ← / → | Escolher dificuldade (Fácil / Difícil) — no menu e na tela final; o nível escolhido fica destacado em amarelo |
| ENTER | Iniciar / nova partida |
| P | Pausar / continuar |
| ESC | Voltar ao menu |

Pressionar subir e descer ao mesmo tempo anula o movimento.

## Regras

- O jogador fica à esquerda; o computador, à direita.
- A bola rebate no teto, no chão e nas raquetes. O ângulo depende da região da raquete atingida e a bola acelera a cada rebatida (até um máximo).
- Saída lateral completa da bola dá um ponto ao adversário; a bola volta ao centro e saca em direção a quem perdeu o ponto, após uma pequena pausa.
- Vence quem chegar a **10 pontos**. A partida para e o vencedor é exibido na tela.

## Níveis de dificuldade

| Nível | Velocidade da raquete do computador | Tolerância de alinhamento |
|---|---|---|
| Fácil | 3 px/atualização | 30 px |
| Difícil | 5 px/atualização | 8 px |

O jogador se move a 6 px/atualização. O computador compara o centro vertical da bola com o centro da raquete e só se move se a diferença passar da tolerância; quando a bola se afasta, ele volta ao centro.

## Responsabilidades das classes

| Classe | Responsabilidade |
|---|---|
| `Main` | Inicia a aplicação na thread de eventos (`SwingUtilities.invokeLater`). |
| `JanelaJogo` | `extends JFrame`; configura a janela, adiciona o campo e limpa os comandos ao perder o foco. |
| `CampoJogo` | `extends JPanel`; coordena partida, teclado (Key Bindings), ciclo (`javax.swing.Timer` único) e desenho. |
| `Raquete` | Posição, dimensões, velocidade, movimento e limites. Duas instâncias (jogador e computador). |
| `Bola` | Posição, velocidades, movimento e reinício. |
| `Placar` | Pontos em atributos privados; `marcarPontoJogador`, `marcarPontoComputador`, `zerar`. |
| `Dificuldade` | `enum` com velocidade e tolerância do computador. |

**Herança:** `CampoJogo extends JPanel` e `JanelaJogo extends JFrame` (relação "é um"); `Bola` e `Raquete` não herdam uma da outra.
**Composição:** `CampoJogo` cria e controla duas `Raquete`, uma `Bola`, um `Placar` e o `Timer`.
**Encapsulamento:** atributos `private`; só `Raquete.setVelocidade`, `Bola.setX/setY/setVelocidadeY` são setters, usados na colisão e na dificuldade.

## Fases concluídas e testes

| Fase | Status |
|---|---|
| 01 Janela e campo (800×600) | Concluída |
| 02 Modelagem das raquetes | Concluída |
| 03 Teclado e limites | Concluída |
| 04 Bola e ciclo do jogo | Concluída |
| 05 Colisões | Concluída |
| 06 Pontuação e saque | Concluída |
| 07 Comportamento do computador | Concluída |
| 08 Encerramento e reinício | Concluída |

Testes realizados (preencha com os seus testes manuais também):

- Compilação limpa com `javac -encoding UTF-8 *.java` (JDK 21.0.12.1).
- Simulação automática sem entrada do jogador: 3 partidas seguidas terminaram em 0 x 10, o timer parou ao fim de cada uma e as partidas seguintes reiniciaram normalmente, sem acelerar (um único timer).
- Desenho do campo verificado sem erros.
- **A conferir manualmente na sua máquina:** limites das raquetes, rebotes nas extremidades, as duas dificuldades, pausa e nova partida.

## Extras (desafios opcionais)

Pausa (P), tela inicial com menu, ângulo de rebote conforme a região da raquete e aumento gradual da velocidade da bola.

## Limitações

- Sem som.
- Se W e ↑ forem mantidas juntas e uma delas for solta, o movimento para (os dois comandos compartilham o mesmo estado).

## Reflexão final (rascunho para você adaptar)

- **Teclado → movimento:** o Key Binding associa pressionar/soltar a booleanos (`cimaPressionado`, `baixoPressionado`); a cada tick do `Timer`, `moverJogador()` consulta esses booleanos e chama `moverParaCima()`/`moverParaBaixo()`, que alteram `y` respeitando os limites.
- **Colisão → velocidade:** se a área da bola intersecta a raquete e ela se aproxima (sinal de `velocidadeX`), a bola é reposicionada fora da raquete e `velocidadeX` tem o sinal invertido.
- **Ponto → estado:** a saída lateral chama `Placar.marcarPonto...`, reinicia a bola e, se atingir 10 pontos, para o timer e muda o estado para `FIM`.
- **Problema e solução:** ao perder o foco da janela, o jogo ainda recebia teclas e o estado de movimento ficava inconsistente; a correção foi limpar os comandos do teclado e pausar/reinicializar o estado do jogador quando a janela saiu do foco, evitando ações fantasma e garantindo que a partida retomasse corretamente.
