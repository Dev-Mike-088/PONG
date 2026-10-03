import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

/**
 * Representa a bola. x e y indicam o canto superior esquerdo.
 * As velocidades são deslocamentos em pixels por atualização.
 */
public class Bola {

    public static final int VELOCIDADE_INICIAL = 5;
    public static final int VELOCIDADE_MAXIMA = 9;

    private int x;
    private int y;
    private final int diametro;
    private int velocidadeX;
    private int velocidadeY;

    private final int larguraCampo;
    private final int alturaCampo;
    private final Random aleatorio = new Random();

    public Bola(int diametro, int larguraCampo, int alturaCampo) {
        this.diametro = diametro;
        this.larguraCampo = larguraCampo;
        this.alturaCampo = alturaCampo;
        reiniciar(aleatorio.nextBoolean() ? 1 : -1);
    }

    /**
     * Centraliza a bola e restaura as velocidades de saque.
     * @param direcao 1 para sacar à direita, -1 para sacar à esquerda
     */
    public void reiniciar(int direcao) {
        x = (larguraCampo - diametro) / 2;
        y = (alturaCampo - diametro) / 2;
        velocidadeX = VELOCIDADE_INICIAL * direcao;
        velocidadeY = (aleatorio.nextBoolean() ? 1 : -1) * (2 + aleatorio.nextInt(2));
    }

    public void mover() {
        x += velocidadeX;
        y += velocidadeY;
    }

    public void inverterVelocidadeX() { velocidadeX = -velocidadeX; }
    public void inverterVelocidadeY() { velocidadeY = -velocidadeY; }

    /** Aumenta o módulo da velocidade horizontal até o máximo. */
    public void acelerar() {
        if (Math.abs(velocidadeX) < VELOCIDADE_MAXIMA) {
            velocidadeX += (velocidadeX > 0) ? 1 : -1;
        }
    }

    public void desenhar(Graphics g) {
        g.fillOval(x, y, diametro, diametro);
    }

    public Rectangle getArea() {
        return new Rectangle(x, y, diametro, diametro);
    }

    public double getCentroY() {
        return y + diametro / 2.0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getDiametro() { return diametro; }
    public int getVelocidadeX() { return velocidadeX; }
    public int getVelocidadeY() { return velocidadeY; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setVelocidadeY(int velocidadeY) { this.velocidadeY = velocidadeY; }
}
