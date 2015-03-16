package com.menotyou.JC;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class SvecioPrisijungimas extends JFrame {

	private static final long serialVersionUID = 8952323351211994022L;
	private JPanel contentPane;
	private JTextField txtVardas;
	private JButton btnPrisijungti;
	private JLabel lblVardas;
	private KlientoLangas kl;
	private int atsakymas;

	
	public SvecioPrisijungimas(KlientoLangas kl) {
		sukurkLanga();
		this.kl = kl;
	}
	
	public void sukurkLanga(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Prisijungimas");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(320, 360);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtVardas = new JTextField();
		txtVardas.setBackground(new Color(255, 255, 255));
		txtVardas.setBounds(87, 143, 111, 30);
		contentPane.add(txtVardas);
		txtVardas.setColumns(10);
		
		lblVardas = new JLabel("\u012Eveskite savo vard\u0105");
		lblVardas.setBounds(87, 102, 102, 30);
		contentPane.add(lblVardas);
		
		btnPrisijungti = new JButton("Prisijungti");
		btnPrisijungti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Prisijungimas();
			}
		});
		btnPrisijungti.setBounds(98, 184, 89, 23);
		contentPane.add(btnPrisijungti);
		setVisible(true);
	}
	public void nustatykAtsakyma(int reiksme){
		atsakymas = reiksme;
	}
	
	public void Prisijungimas(){
		String vardas = txtVardas.getText();
		atsakymas = 0;
		NIOKlientas klientas = kl.gaukKlienta();
		if(klientas != null) klientas.siuskZinute(vardas);
		while(atsakymas == 0){
			try{
				Thread.sleep(100);
			} catch (Exception e){
				
			}
		}
		if(atsakymas == 1){
			dispose();
			kl.sukurkKambarioInterfeisa("Pagrindinis");
			kl.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null, "Nepavyko prisijungi, bandykite dar kart�!", "Klaida!", JOptionPane.INFORMATION_MESSAGE);
		}
		atsakymas = 0;
		
	}
}
