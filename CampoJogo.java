import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * Campo de jogo. Herda de JPanel (é um painel especializado) e coordena
 * partida, teclado, ciclo de atualização e desenho.
 * Composição: cria e controla uma Bola, duas Raquetes e um Placar.
 */
public class CampoJogo extends JPanel {

    // ---- Constantes de dimensões e regras ----
    public static final int LARGURA = 800;
    public static final int ALTURA = 600;
    private static final int LARGURA_RAQUETE = 15;
    private static final int ALTURA_RAQUETE = 100;
    private static final int MARGEM_RAQUETE = 30;
    private static final int DIAMETRO_BOLA = 15;
    private static final int VELOCIDADE_JOGADOR = 6;
    private static final int PONTOS_PARA_VENCER = 10;
    private static final int INTERVALO_MS = 16;
    private static final int TICKS_SAQUE = 45; // pausa curta antes de cada saque

    private enum Estado { MENU, JOGANDO, PAUSADO, FIM }

    // ---- Objetos da partida (composição) ----
    private final Raquete jogador;
    private final Raquete computador;
    private final Bola bola;
    private final Placar placar;
    private final Timer timer; // único timer do jogo

    // ---- Estado da partida ----
    private Estado estado = Estado.MENU;
    private Dificuldade dificuldade = Dificuldade.FACIL;
    private String vencedor = "";
    private int ticksSaque = 0;

    // ---- Estado dos comandos (consultado pelo timer) ----
    private boolean cimaPressionado = false;
    private boolean baixoPressionado = false;

    public CampoJogo() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(Color.BLACK);
        setFocusable(true);

        jogador = new Raquete(MARGEM_RAQUETE, LARGURA_RAQUETE, ALTURA_RAQUETE,
                VELOCIDADE_JOGADOR, ALTURA);
        computador = new Raquete(LARGURA - MARGEM_RAQUETE - LARGURA_RAQUETE,
                LARGURA_RAQUETE, ALTURA_RAQUETE, dificuldade.getVelocidade(), ALTURA);
        bola = new Bola(DIAMETRO_BOLA, LARGURA, ALTURA);
        placar = new Placar();

        timer = new Timer(INTERVALO_MS, e -> atualizarJogo());

        configurarTeclas();
    }

    // =====================================================================
    //  Teclado (Key Bindings)
    // =====================================================================

    private void configurarTeclas() {
        associar("pressed W", "cimaOn", () -> cimaPressionado = true);
        associar("released W", "cimaOff", () -> cimaPressionado = false);
        associar("pressed UP", "cimaSetaOn", () -> cimaPressionado = true);
        associar("released UP", "cimaSetaOff", () -> cimaPressionado = false);

        associar("pressed S", "baixoOn", () -> baixoPressionado = true);
        associar("released S", "baixoOff", () -> baixoPressionado = false);
        associar("pressed DOWN", "baixoSetaOn", () -> baixoPressionado = true);
        associar("released DOWN", "baixoSetaOff", () -> baixoPressionado = false);

        associar("pressed 1", "facil", () -> escolherDificuldade(Dificuldade.FACIL));
        associar("pressed 2", "dificil", () -> escolherDificuldade(Dificuldade.DIFICIL));
        associar("pressed LEFT", "facilSeta", () -> escolherDificuldade(Dificuldade.FACIL));
        associar("pressed RIGHT", "dificilSeta", () -> escolherDificuldade(Dificuldade.DIFICIL));
        associar("pressed ENTER", "iniciar", this::aoPressionarEnter);
        associar("pressed P", "pausa", this::alternarPausa);
        associar("pressed ESCAPE", "menu", this::voltarAoMenu);
    }

    private void associar(String tecla, String nome, Runnable acao) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), nome);
        getActionMap().put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.run();
            }
        });
    }

    /** Limpa os comandos ativos (usado ao perder o foco ou reiniciar). */
    public void limparComandos() {
        cimaPressionado = false;
        baixoPressionado = false;
    }

    // =====================================================================
    //  Controle de estado da partida
    // =====================================================================

    private void escolherDificuldade(Dificuldade nova) {
        if (estado == Estado.MENU || estado == Estado.FIM) {
            dificuldade = nova;
            computador.setVelocidade(dificuldade.getVelocidade());
            repaint();
        }
    }

    private void aoPressionarEnter() {
        if (estado == Estado.MENU || estado == Estado.FIM) {
            novaPartida();
        }
    }

    /** Restaura tudo para as condições iniciais e (re)inicia o mesmo timer. */
    private void novaPartida() {
        placar.zerar();
        jogador.centralizar();
        computador.centralizar();
        computador.setVelocidade(dificuldade.getVelocidade());
        bola.reiniciar(Math.random() < 0.5 ? 1 : -1);
        limparComandos();
        vencedor = "";
        ticksSaque = TICKS_SAQUE;
        estado = Estado.JOGANDO;
        if (!timer.isRunning()) {
            timer.start();
        }
        repaint();
    }

    private void alternarPausa() {
        if (estado == Estado.JOGANDO) {
            estado = Estado.PAUSADO;
            timer.stop();
            limparComandos();
            repaint();
        } else if (estado == Estado.PAUSADO) {
            estado = Estado.JOGANDO;
            if (!timer.isRunning()) {
                timer.start();
            }
        }
    }

    private void voltarAoMenu() {
        if (estado == Estado.JOGANDO || estado == Estado.PAUSADO || estado == Estado.FIM) {
            timer.stop();
            placar.zerar();
            jogador.centralizar();
            computador.centralizar();
            bola.reiniciar(1);
            limparComandos();
            estado = Estado.MENU;
            repaint();
        }
    }

    // =====================================================================
    //  Ciclo de atualização
    // =====================================================================

    /** Sequência: comandos -> raquetes -> bola -> limites/colisões -> ponto/vitória -> repaint. */
    private void atualizarJogo() {
        if (estado != Estado.JOGANDO) {
            return;
        }

        moverJogador();
        moverComputador();

        if (ticksSaque > 0) { // bola parada logo após o saque/reinício
            ticksSaque--;
            repaint();
            return;
        }

        bola.mover();
        tratarTetoEChao();
        tratarRaquetes();

        if (tratarPonto()) {
            repaint();
            return;
        }
        repaint();
    }

    private void moverJogador() {
        if (cimaPressionado && !baixoPressionado) {
            jogador.moverParaCima();
        } else if (baixoPressionado && !cimaPressionado) {
            jogador.moverParaBaixo();
        }
        // Se ambas estiverem pressionadas, os comandos se anulam.
    }

    /** IA simples: acompanha o centro da bola, com velocidade limitada e tolerância. */
    private void moverComputador() {
        double alvo;
        if (bola.getVelocidadeX() > 0) {
            alvo = bola.getCentroY();   // bola se aproximando: acompanha
        } else {
            alvo = ALTURA / 2.0;        // bola se afastando: volta ao centro
        }
        double diferenca = alvo - computador.getCentroY();
        int tolerancia = dificuldade.getTolerancia();

        if (diferenca < -tolerancia) {
            computador.moverParaCima();
        } else if (diferenca > tolerancia) {
            computador.moverParaBaixo();
        }
    }

    private void tratarTetoEChao() {
        if (bola.getY() < 0) {
            bola.setY(0);
            bola.setVelocidadeY(Math.abs(bola.getVelocidadeY()));
        } else if (bola.getY() + bola.getDiametro() > ALTURA) {
            bola.setY(ALTURA - bola.getDiametro());
            bola.setVelocidadeY(-Math.abs(bola.getVelocidadeY()));
        }
    }

    private void tratarRaquetes() {
        Rectangle areaBola = bola.getArea();

        // Raquete do jogador (esquerda): só rebate se a bola estiver indo para a esquerda.
        if (bola.getVelocidadeX() < 0 && areaBola.intersects(jogador.getArea())) {
            bola.setX(jogador.getX() + jogador.getLargura()); // sai da raquete
            rebater(jogador);
            return;
        }

        // Raquete do computador (direita): só rebate se a bola estiver indo para a direita.
        if (bola.getVelocidadeX() > 0 && areaBola.intersects(computador.getArea())) {
            bola.setX(computador.getX() - bola.getDiametro()); // sai da raquete
            rebater(computador);
        }
    }

    /** Inverte a direção horizontal, ajusta o ângulo pela região atingida e acelera um pouco. */
    private void rebater(Raquete raquete) {
        bola.inverterVelocidadeX();
        bola.acelerar();

        // Região atingida: -1 (extremo superior) ... 0 (centro) ... +1 (extremo inferior)
        double relativo = (bola.getCentroY() - raquete.getCentroY()) / (raquete.getAltura() / 2.0);
        relativo = Math.max(-1.0, Math.min(1.0, relativo));
        bola.setVelocidadeY((int) Math.round(relativo * 5));
        if (bola.getVelocidadeY() == 0) {
            bola.setVelocidadeY(1); // evita trajetória totalmente horizontal
        }
    }

    /**
     * Verifica saída lateral. Retorna true se um ponto foi contabilizado
     * (o evento é tratado uma única vez, pois a bola é reiniciada em seguida).
     */
    private boolean tratarPonto() {
        int direcaoSaque;

        if (bola.getX() + bola.getDiametro() <= 0) {         // saiu pela esquerda
            placar.marcarPontoComputador();
            direcaoSaque = -1;                               // saca em direção a quem perdeu o ponto
        } else if (bola.getX() >= LARGURA) {                 // saiu pela direita
            placar.marcarPontoJogador();
            direcaoSaque = 1;
        } else {
            return false;
        }

        if (placar.getPontosJogador() >= PONTOS_PARA_VENCER) {
            encerrarPartida("Você venceu!");
        } else if (placar.getPontosComputador() >= PONTOS_PARA_VENCER) {
            encerrarPartida("O computador venceu!");
        } else {
            bola.reiniciar(direcaoSaque);
            ticksSaque = TICKS_SAQUE;
        }
        return true;
    }

    /** Para o timer e marca o fim da partida (acontece uma única vez). */
    private void encerrarPartida(String mensagem) {
        timer.stop();
        vencedor = mensagem;
        estado = Estado.FIM;
        limparComandos();
    }

    // =====================================================================
    //  Desenho (apenas lê o estado, nunca o modifica)
    // =====================================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        desenharLinhaCentral(g2);

        g2.setColor(Color.WHITE);
        jogador.desenhar(g2);
        computador.desenhar(g2);
        if (estado != Estado.MENU) {
            bola.desenhar(g2);
        }

        desenharPlacar(g2);
        if (estado == Estado.JOGANDO || estado == Estado.PAUSADO) {
            centralizarTexto(g2, "Dificuldade: " + dificuldade.getNome(),
                    new Font("SansSerif", Font.PLAIN, 14), Color.GRAY, ALTURA - 12);
        }

        switch (estado) {
            case MENU:
                desenharMenu(g2);
                break;
            case PAUSADO:
                desenharMensagem(g2, "PAUSADO", "Pressione P para continuar  |  ESC para o menu");
                break;
            case FIM:
                desenharFim(g2);
                break;
            default:
                break;
        }
    }

    private void desenharLinhaCentral(Graphics2D g2) {
        g2.setColor(Color.GRAY);
        float[] tracejado = {12f, 12f};
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, tracejado, 0f));
        g2.drawLine(LARGURA / 2, 0, LARGURA / 2, ALTURA);
        g2.setStroke(new BasicStroke(1f));
    }

    private void desenharPlacar(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 48));
        FontMetrics fm = g2.getFontMetrics();

        String textoJogador = String.valueOf(placar.getPontosJogador());
        String textoComputador = String.valueOf(placar.getPontosComputador());
        g2.drawString(textoJogador, LARGURA / 4 - fm.stringWidth(textoJogador) / 2, 60);
        g2.drawString(textoComputador, 3 * LARGURA / 4 - fm.stringWidth(textoComputador) / 2, 60);
    }

    private void desenharMenu(Graphics2D g2) {
        escurecer(g2);
        centralizarTexto(g2, "PONG", new Font("Monospaced", Font.BOLD, 72), Color.WHITE, 200);

        centralizarTexto(g2, "Escolha a dificuldade:",
                new Font("SansSerif", Font.PLAIN, 22), Color.WHITE, 280);
        desenharOpcoesDificuldade(g2, 305);
        centralizarTexto(g2, "Teclas 1 / 2  ou  \u2190 / \u2192",
                new Font("SansSerif", Font.PLAIN, 16), Color.LIGHT_GRAY, 400);

        centralizarTexto(g2, "ENTER para jogar no n\u00edvel " + dificuldade.getNome().toUpperCase(),
                new Font("SansSerif", Font.BOLD, 24), Color.YELLOW, 450);
        centralizarTexto(g2, "W / S ou \u2191 / \u2193: mover   |   P: pausar   |   ESC: menu",
                new Font("SansSerif", Font.PLAIN, 16), Color.LIGHT_GRAY, 500);
        centralizarTexto(g2, "Vence quem fizer " + PONTOS_PARA_VENCER + " pontos",
                new Font("SansSerif", Font.PLAIN, 16), Color.LIGHT_GRAY, 528);
    }

    /** Desenha os dois bot\u00f5es de dificuldade; o selecionado fica preenchido em amarelo. */
    private void desenharOpcoesDificuldade(Graphics2D g2, int topo) {
        Dificuldade[] niveis = Dificuldade.values();
        int largura = 190;
        int altura = 56;
        int espaco = 30;
        int total = niveis.length * largura + (niveis.length - 1) * espaco;
        int inicioX = (LARGURA - total) / 2;

        for (int i = 0; i < niveis.length; i++) {
            Dificuldade nivel = niveis[i];
            boolean selecionado = (nivel == dificuldade);
            int x = inicioX + i * (largura + espaco);

            if (selecionado) {
                g2.setColor(Color.YELLOW);
                g2.fillRoundRect(x, topo, largura, altura, 16, 16);
            } else {
                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x, topo, largura, altura, 16, 16);
                g2.setStroke(new BasicStroke(1f));
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2.setColor(selecionado ? Color.BLACK : Color.GRAY);
            String texto = "[" + (i + 1) + "] " + nivel.getNome() + (selecionado ? "  \u2713" : "");
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, x + (largura - fm.stringWidth(texto)) / 2,
                    topo + (altura + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    private void desenharFim(Graphics2D g2) {
        escurecer(g2);
        centralizarTexto(g2, vencedor, new Font("SansSerif", Font.BOLD, 48), Color.YELLOW, 220);
        centralizarTexto(g2, placar.getPontosJogador() + " x " + placar.getPontosComputador(),
                new Font("Monospaced", Font.BOLD, 36), Color.WHITE, 280);

        centralizarTexto(g2, "Dificuldade da pr\u00f3xima partida:",
                new Font("SansSerif", Font.PLAIN, 20), Color.WHITE, 345);
        desenharOpcoesDificuldade(g2, 365);

        centralizarTexto(g2, "ENTER para nova partida   |   ESC para o menu",
                new Font("SansSerif", Font.PLAIN, 22), Color.WHITE, 480);
    }

    private void desenharMensagem(Graphics2D g2, String titulo, String subtitulo) {
        escurecer(g2);
        centralizarTexto(g2, titulo, new Font("SansSerif", Font.BOLD, 48), Color.WHITE, 290);
        centralizarTexto(g2, subtitulo, new Font("SansSerif", Font.PLAIN, 20), Color.LIGHT_GRAY, 340);
    }

    private void escurecer(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, LARGURA, ALTURA);
    }

    private void centralizarTexto(Graphics2D g2, String texto, Font fonte, Color cor, int y) {
        g2.setFont(fonte);
        g2.setColor(cor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, (LARGURA - fm.stringWidth(texto)) / 2, y);
    }
}
