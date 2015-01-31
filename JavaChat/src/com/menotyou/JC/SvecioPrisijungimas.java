package com.menotyou.JC;

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

import com.menotyou.JC.Klientas;
import com.menotyou.JC.KlientoLangas;

import java.awt.Color;

public class SvecioPrisijungimas extends JFrame {

	private JPanel contentPane;
	private JTextField txtVardas;
	private JButton btnPrisijungti;
	private Klientas klientas;
	private JLabel lblVardas;

	
	public SvecioPrisijungimas(Klientas k) {
		sukurkLanga();
		klientas = k;
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
	
	public void Prisijungimas(){
		String[] vartotojuSarasas = klientas.gaukVartotojuSarasa();
		String vartotojoVardas = txtVardas.getText();
		System.out.println("Tikrinamas vartotojo vardas");
		System.out.println("Vartotojo vardas: " + vartotojoVardas);
		if(gerasVardas(vartotojoVardas, vartotojuSarasas)){
			dispose();
			new KlientoLangas(vartotojoVardas, klientas);
			klientas.siuntejas.siuskZinute("/PS/" + vartotojoVardas);
		} else {
			JOptionPane.showMessageDialog(null, "Toks vardas jau naudojamas", "Klaida!", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	private boolean gerasVardas(String vardas, String[] visiVardai){
		if(visiVardai == null) return true;
		for(int i = 0; i < visiVardai.length; i++){
			if(vardas == visiVardai[i])
				return false;
		}
		return true;
	}
}
