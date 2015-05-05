package com.menotyou.JC;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Užsklandos langas.
 */
public class Uzsklanda extends JWindow {

    private static final long serialVersionUID = -5755668657545305148L;
    private SvecioPrisijungimas svecias;

    /**
     * Sukuriama nauja užsklanda.
     *
     * @param svecias -> SvecioPrisijungimas objektas kuris eis po šios užklandos
     */
    public Uzsklanda(final SvecioPrisijungimas svecias) {
        this.svecias = svecias;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                rodytiUzsklanda();
            }
        });
    }

    /**
     * Rodoma užsklanda, nustatomas ProgressBar kintamasis ir kuris pripildomas
     * per 5 sekudes.
     */
    public void rodytiUzsklanda() {
        JPanel content = (JPanel) getContentPane();
        content.setBackground(SystemColor.controlHighlight);
        getContentPane().setLayout(null);
        Color spalva = new Color(57, 64, 70);

        JLabel logo = new JLabel(null, new ImageIcon(getClass().getResource("/Logo.png")), 0);
        logo.setText("");
        logo.setBounds(10, 128, 680, 256);
        getContentPane().add(logo);

        final JProgressBar progressBar;
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBounds(10, 388, 680, 26);
        progressBar.setForeground(spalva);
        progressBar.setValue(0);
        progressBar.setBorderPainted(false);
        int timerDelay = 100;
        new javax.swing.Timer(timerDelay, new ActionListener() {
            private int index = 0;

            public void actionPerformed(ActionEvent e) {
                if (index < 100) {
                    progressBar.setValue(index);
                    index += 2;
                } else {
                    progressBar.setValue(100);
                    ((javax.swing.Timer) e.getSource()).stop();
                }
            }
        }).start();

        progressBar.setValue(progressBar.getMinimum());
        getContentPane().add(progressBar);

        JLabel autoriusLabel = new JLabel("<html><b>Autorius:</b> Tautvydas Valašinas </html>");
        autoriusLabel.setForeground(Color.DARK_GRAY);
        autoriusLabel.setBounds(30, 78, 187, 14);
        getContentPane().add(autoriusLabel);

        JLabel lblgrupe = new JLabel("<html><b>Grupė:</b> P-3/2V </html>");
        lblgrupe.setForeground(Color.DARK_GRAY);
        lblgrupe.setBounds(30, 103, 89, 14);
        getContentPane().add(lblgrupe);

        JLabel lblBaigiamasisDarbas = new JLabel("Baigiamasis darbas");
        lblBaigiamasisDarbas.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblBaigiamasisDarbas.setForeground(Color.DARK_GRAY);
        lblBaigiamasisDarbas.setBounds(232, 22, 151, 26);
        getContentPane().add(lblBaigiamasisDarbas);

        JLabel lblidjaSukurtiProgram = new JLabel("<html><b>Idėja: </b> Sukurti programą kurios pagalba būtų galima susirašinėti internetu</html>");
        lblidjaSukurtiProgram.setForeground(Color.DARK_GRAY);
        lblidjaSukurtiProgram.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblidjaSukurtiProgram.setBounds(30, 47, 355, 26);
        getContentPane().add(lblidjaSukurtiProgram);

        int width = 700;
        int height = 450;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - width) / 2;
        int y = (screen.height - height) / 2;
        setBounds(x, y, width, height);

        setVisible(true);
        toFront();

        new ResourceLoader().execute();
    }

    /**
     * Klasė skirta vykdyti veiksmą fone. Šiuo atvėju ji tik uždaro
     * ši langą ir padaro matomu sekantį.
     */
    public class ResourceLoader extends SwingWorker<Object, Object> {

        @Override
        protected Object doInBackground() throws Exception {
            try {
                Thread.sleep(5500);
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void done() {
            dispose();
            svecias.setVisible(true);
        }
    }
}
