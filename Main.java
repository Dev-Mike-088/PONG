import javax.swing.SwingUtilities;

/**
 * Ponto de entrada da aplicação. Apenas inicia a interface gráfica
 * na thread de eventos do Swing; a lógica do jogo fica em CampoJogo.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JanelaJogo());
    }
}
