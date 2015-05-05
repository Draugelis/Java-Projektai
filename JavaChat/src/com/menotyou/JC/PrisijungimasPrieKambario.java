package com.menotyou.JC;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Langas skirtas leisti klientui prisijungti prie kambario.
 */
public class PrisijungimasPrieKambario extends JFrame {

    private static final long serialVersionUID = 8979555935226577804L;

    private JPanel contentPane;
    private NIOKlientas klientas;
    private JButton btnNewButton;
    private JLabel lblPavadinimas;
    private JProgressBar progressBar;
    /** kintamasis kuriame saugomi visi galimi klambariai */
    private JComboBox<String> m_kambariai;

    /**
     * Sukuriamas naujas langas
     *
     * @param klientoLangas -> Langas kuris iškvietė ši objektą.
     * Per kliento langą, šis langas gali pasiekti klientą.
     */
    public PrisijungimasPrieKambario(final KlientoLangas klientoLangas) {
        setTitle("Prisijungimas prie kambario");
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(250, 250);
        klientas = klientoLangas.gaukKlienta();

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);

        btnNewButton = new JButton("Jungtis");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kambarioUzklausa((String) m_kambariai.getSelectedItem());
            }
        });
        btnNewButton.setBounds(59, 124, 115, 23);
        contentPane.add(btnNewButton);

        lblPavadinimas = new JLabel("Galimi kambariai:");
        lblPavadinimas.setHorizontalAlignment(SwingConstants.CENTER);
        lblPavadinimas.setBounds(35, 41, 164, 32);
        contentPane.add(lblPavadinimas);

        progressBar = new JProgressBar();
        progressBar.setBounds(0, 276, 298, 14);
        progressBar.setVisible(false);
        contentPane.add(progressBar);

        m_kambariai = new JComboBox<String>();
        m_kambariai.setLocation(70, 73);
        m_kambariai.setSize(100, 20);
        m_kambariai.setMaximumRowCount(2000);
        m_kambariai.setPreferredSize(new Dimension(100, 20));
        contentPane.add(m_kambariai);

        setContentPane(contentPane);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Metodas skiratas pranešti vartotojui apie klaidą.
     * 
     * @param klaida -> klaidos tekstas.
     */
    public void klaida(String klaida) {
        JOptionPane.showMessageDialog(null, klaida, "Klaida!", JOptionPane.INFORMATION_MESSAGE);
        progressBar.setVisible(false);
        btnNewButton.setEnabled(true);
    }

    /**
     * Nustatomi galimi prisijungti kambariai.
     * Jei kambarių nėra, vartotojas įspėjamas.
     *
     * @param kambariai -> kambarių pavadinimų masyvas.
     */
    public void nustatykKambarius(String[] kambariai) {
        m_kambariai.removeAllItems();
        for (int i = 0; i < kambariai.length; i++) {
            if (!klientas.jauAtidarytasKambarys(kambariai[i])) m_kambariai.addItem(kambariai[i]);
        }
        if (m_kambariai.getItemCount() == 0) {
            lblPavadinimas.setText("Šiuo metu jokių kambarių nėra.");
            m_kambariai.setVisible(false);
            btnNewButton.setEnabled(false);
        } else {
            lblPavadinimas.setText("Galimi kambariai:");
            m_kambariai.setVisible(true);
            btnNewButton.setEnabled(true);
        }
    }

    /**
     * Metodas skirtas pašalinti šį langą.
     */
    public void pasalink() {
        progressBar.setVisible(false);
        btnNewButton.setEnabled(true);
        dispose();
    }

    /**
     * Metodas skirtas siusti kambario užklausą.
     *
     * @param pavadinimas ->kambario pavadinimas.
     */
    private void kambarioUzklausa(String pavadinimas) {
        klientas.siuskZinute("<K+>" + pavadinimas);
        progressBar.setVisible(false);
        progressBar.setValue(50);
        btnNewButton.setEnabled(true);
    }
}
