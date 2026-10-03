import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Representa uma raquete: posição, dimensões e velocidade.
 * As duas raquetes do jogo são instâncias desta mesma classe.
 */
public class Raquete {

    private final int x;            // posição horizontal (fixa)
    private int y;                  // posição vertical (canto superior)
    private final int largura;
    private final int altura;
    private int velocidade;         // pixels por atualização
    private final int alturaCampo;  // limite inferior do campo

    public Raquete(int x, int largura, int altura, int velocidade, int alturaCampo) {
        this.x = x;
        this.largura = largura;
        this.altura = altura;
        this.velocidade = velocidade;
        this.alturaCampo = alturaCampo;
        centralizar();
    }

    /** Posiciona a raquete no centro vertical do campo. */
    public void centralizar() {
        y = (alturaCampo - altura) / 2;
    }

    /** Diminuir y faz a raquete subir. */
    public void moverParaCima() {
        y -= velocidade;
        limitar();
    }

    /** Aumentar y faz a raquete descer. */
    public void moverParaBaixo() {
        y += velocidade;
        limitar();
    }

    /** Mantém a raquete inteiramente dentro do campo: 0 <= y <= alturaCampo - altura. */
    private void limitar() {
        if (y < 0) {
            y = 0;
        }
        if (y + altura > alturaCampo) {
            y = alturaCampo - altura;
        }
    }

    /** Apenas lê o estado e desenha; não modifica nada. */
    public void desenhar(Graphics g) {
        g.fillRect(x, y, largura, altura);
    }

    public Rectangle getArea() {
        return new Rectangle(x, y, largura, altura);
    }

    public double getCentroY() {
        return y + altura / 2.0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getLargura() { return largura; }
    public int getAltura() { return altura; }
    public int getVelocidade() { return velocidade; }

    public void setVelocidade(int velocidade) {
        this.velocidade = velocidade;
    }
}
