package com.menotyou.JC;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class ApiePrograma extends JFrame {
    private static final long serialVersionUID = 1L;

    /**
     * Sukuriamas naujas langas, kuriame pavaziduota informacija apie programą.
     */
    public ApiePrograma() {
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Apie programą");
        getContentPane().setLayout(null);

        JLabel lblprogramosAutoriusTautvydas = new JLabel("<html><b>Programos autorius:</b> Tautvydas Valašinas</html>");
        lblprogramosAutoriusTautvydas.setBounds(10, 55, 216, 14);
        getContentPane().add(lblprogramosAutoriusTautvydas);

        JLabel lblProgramavimoKalba = new JLabel("<html><b>Programavimo kalba:</b> Java</html>");
        lblProgramavimoKalba.setBounds(10, 83, 147, 14);
        getContentPane().add(lblProgramavimoKalba);

        JLabel lblInformacija = new JLabel("Informacija");
        lblInformacija.setFont(new Font("Times New Roman", Font.BOLD, 18));
        lblInformacija.setBounds(96, 22, 92, 22);
        getContentPane().add(lblInformacija);

        JLabel lblDarboApimtis = new JLabel("<html><b>Darbo apimtis: </b> 3400 eilučių</html>");
        lblDarboApimtis.setBounds(10, 108, 141, 14);
        getContentPane().add(lblDarboApimtis);

        JLabel lblProgramosTipas = new JLabel("<html><b>Progamos tipas:</b> atviro kodo<html>");
        lblProgramosTipas.setBounds(10, 133, 148, 14);
        getContentPane().add(lblProgramosTipas);

        JLabel lblProgramosKodas = new JLabel("<html><b>Programos kodas:   </b<a href =\"https://github.com/tvalasinas/Java-Projektai\">https://github.com/tvalasinas/Java-Projektai</html>");
        lblProgramosKodas.setBounds(10, 158, 327, 14);
        getContentPane().add(lblProgramosKodas);

        JLabel lblNewLabel = new JLabel("<html><b>Paskirtis:  </b> Progama sukurta kaip baigiamasi darbas <br> JKM kursiui Programavimas C++ II</html>");
        lblNewLabel.setBounds(10, 183, 246, 28);
        getContentPane().add(lblNewLabel);
        setLocationRelativeTo(null);
        setSize(380, 390);
        setVisible(true);
    }
}
