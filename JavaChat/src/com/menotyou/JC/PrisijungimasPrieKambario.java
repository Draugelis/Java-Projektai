package com.menotyou.JC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;

public class PrisijungimasPrieKambario extends JFrame {
	private static final long serialVersionUID = 8979555935226577804L;
	private JPanel contentPane;
	private JTextField pavadinimas;
	private NIOKlientas klientas;

	public PrisijungimasPrieKambario(final KlientoLangas klientoLangas) {
		setTitle("Prisijungimas prie kambario");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 304, 318);
		klientas = klientoLangas.gaukKlienta();

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);

		pavadinimas = new JTextField();
		pavadinimas.setBounds(75, 140, 112, 20);
		contentPane.add(pavadinimas);
		pavadinimas.setColumns(10);

		JButton btnNewButton = new JButton("Jungtis");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kambarioUzklausa(pavadinimas.getText(), pradineZinute.getText());
				klientoLangas.sukurkKambarioInterfeisa(pavadinimas.getText());
			}
		});
		btnNewButton.setBounds(75, 188, 115, 23);
		contentPane.add(btnNewButton);

		JLabel lblPavadinimas = new JLabel("Pavadinimas");
		lblPavadinimas.setBounds(87, 103, 71, 14);
		contentPane.add(lblPavadinimas);

		setContentPane(contentPane);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(0, 253, 298, 14);
		contentPane.add(progressBar);

		JLabel LoadingLbl = new JLabel("Kraunasi...");
		LoadingLbl.setBounds(100, 233, 64, 20);
		contentPane.add(LoadingLbl);
		setVisible(true);
	}

	private void kambarioUzklausa(String pavadinimas, String pradineZinute) {
		if (!pradineZinute.isEmpty()) klientas.siuskZinute("<NK>" + pavadinimas + "<KZ>" + pradineZinute);
		else
			klientas.siuskZinute("<NK>" + pavadinimas);
	}
}
