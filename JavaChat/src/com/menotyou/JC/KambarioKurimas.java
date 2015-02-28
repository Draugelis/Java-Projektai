package com.menotyou.JC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class KambarioKurimas extends JFrame {

	private JPanel contentPane;
	private JTextField pavadinimas;

	public KambarioKurimas(final KlientoLangas kl) {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 304, 318);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		pavadinimas = new JTextField();
		pavadinimas.setBounds(86, 67, 86, 20);
		contentPane.add(pavadinimas);
		pavadinimas.setColumns(10);
		
		JButton btnNewButton = new JButton("Kurti kambar\u012F");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				kl.sukurkKambarioInterfeisa(pavadinimas.getText());
			}
		});
		btnNewButton.setBounds(79, 98, 115, 23);
		contentPane.add(btnNewButton);
		
		JLabel lblPavadinimas = new JLabel("Pavadinimas");
		lblPavadinimas.setBounds(101, 42, 71, 14);
		contentPane.add(lblPavadinimas);
		
		setContentPane(contentPane);
		setVisible(true);
	}
}
