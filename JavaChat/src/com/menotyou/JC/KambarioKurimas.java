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

public class KambarioKurimas extends JFrame {

	private static final long serialVersionUID = -3747377694043578560L;
	private JPanel contentPane;
	private JTextField pavadinimas;
	private JTextArea pradineZinute;
	private NIOKlientas klientas;

	public KambarioKurimas(final KlientoLangas klientoLangas) {
		setTitle("Kambario kūrimas");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 304, 318);
		klientas = klientoLangas.gaukKlienta();
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		
		pavadinimas = new JTextField();
		pavadinimas.setBounds(78, 67, 112, 20);
		contentPane.add(pavadinimas);
		pavadinimas.setColumns(10);
		
		JButton btnNewButton = new JButton("Kurti kambar\u012F");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kambarioUzklausa(pavadinimas.getText(), pradineZinute.getText());
				klientoLangas.sukurkKambarioInterfeisa(pavadinimas.getText());
			}
		});
		btnNewButton.setBounds(78, 218, 115, 23);
		contentPane.add(btnNewButton);
		
		JLabel lblPavadinimas = new JLabel("Pavadinimas");
		lblPavadinimas.setBounds(101, 42, 71, 14);
		contentPane.add(lblPavadinimas);
		
		setContentPane(contentPane);
		
		pradineZinute = new JTextArea();
		pradineZinute.setBounds(35, 116, 231, 91);
		contentPane.add(pradineZinute);
		
		JLabel lblkamprad = new JLabel("Kambario pradinė žinutė");
		lblkamprad.setBounds(76, 98, 132, 14);
		contentPane.add(lblkamprad);
		setVisible(true);
	}
	private void kambarioUzklausa(String pavadinimas, String pradineZinute){
		if(!pradineZinute.isEmpty())
			klientas.siuskZinute("<NK>" + pavadinimas + "<KZ>" + pradineZinute);
		else 
			klientas.siuskZinute("<NK>" + pavadinimas);
	}
}
