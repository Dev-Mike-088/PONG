/**
 * Níveis de dificuldade do computador.
 * Cada nível define a velocidade da raquete (pixels por atualização)
 * e a tolerância de alinhamento (faixa em pixels sem movimento).
 */
public enum Dificuldade {
    FACIL("Fácil", 3, 30),
    DIFICIL("Difícil", 5, 8);

    private final String nome;
    private final int velocidade;
    private final int tolerancia;

    Dificuldade(String nome, int velocidade, int tolerancia) {
        this.nome = nome;
        this.velocidade = velocidade;
        this.tolerancia = tolerancia;
    }

    public String getNome() { return nome; }
    public int getVelocidade() { return velocidade; }
    public int getTolerancia() { return tolerancia; }
}
