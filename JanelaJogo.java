import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Janela da aplicação. Herda de JFrame (relação "é um": é uma janela
 * especializada) e contém o CampoJogo.
 */
public class JanelaJogo extends JFrame {

    private final CampoJogo campo;

    public JanelaJogo() {
        super("Pong em Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        campo = new CampoJogo();
        add(campo);

        setResizable(false);
        pack(); // usa a dimensão preferida do campo (800 x 600 de área útil)
        setLocationRelativeTo(null);

        // Ao perder o foco da janela, limpa os comandos para evitar movimento preso.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                campo.limparComandos();
            }
        });

        setVisible(true);
        campo.requestFocusInWindow();
    }
}
