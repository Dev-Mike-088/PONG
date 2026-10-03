/**
 * Controla a pontuação da partida. Os atributos são privados;
 * outras classes só alteram os pontos pelos métodos de ação.
 */
public class Placar {

    private int pontosJogador;
    private int pontosComputador;

    public void marcarPontoJogador() {
        pontosJogador++;
    }

    public void marcarPontoComputador() {
        pontosComputador++;
    }

    public void zerar() {
        pontosJogador = 0;
        pontosComputador = 0;
    }

    public int getPontosJogador() { return pontosJogador; }
    public int getPontosComputador() { return pontosComputador; }
}
