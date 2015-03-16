package com.menotyou.JC;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JButton;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Registracija extends JFrame {

	private JPanel contentPane;
	private JTextField VartotojoVardas;
	private JPasswordField Slaptazodis_pirmas;
	private JPasswordField Slaptazodis_antras;
	private Connection con;
	private VartotojoAutentifikacija VA;
	
	public Registracija() {
		sukurkLanga();
		try{
			con = DriverManager.getConnection(
					"jdbc:mysql://shared.fln.lt/tvalasinas",
					"tvalasinas",
					"nOmbtbodfjxJp9ig");
			JOptionPane.showMessageDialog(null, "Su duomenø baze susisiekta", "Praneðimas!", JOptionPane.INFORMATION_MESSAGE);
			VA = VartotojoAutentifikacija.gaukVAValdikli();
		}catch(SQLException e){
			JOptionPane.showMessageDialog(null, "Nepavyko susisiekti su duomenu baze", "Klaida!", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
		}
	}
	private void patikrinkVarda(String vardas) {
		if(VA.vardasUzimtas(con, vardas)){
			JOptionPane.showMessageDialog(null, "Toks vardas jau naudojamas!", "Klaida!", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	void sukurkLanga(){
		setResizable(false);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		VartotojoVardas = new JTextField();
		VartotojoVardas.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				patikrinkVarda(VartotojoVardas.getText());
			}
		});
		VartotojoVardas.setToolTipText("");
		VartotojoVardas.setBounds(167, 65, 86, 20);
		contentPane.add(VartotojoVardas);
		VartotojoVardas.setColumns(10);
		
		JLabel lblVardas = new JLabel("Vardas");
		lblVardas.setBounds(119, 68, 38, 14);
		contentPane.add(lblVardas);
		
		JLabel lblNewLabel = new JLabel("Slapta\u017Eodis");
		lblNewLabel.setBounds(96, 109, 61, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Pakartokite slapta\u017Eod\u012F");
		lblNewLabel_1.setLabelFor(this);
		lblNewLabel_1.setBounds(44, 134, 113, 14);
		contentPane.add(lblNewLabel_1);
		
		Slaptazodis_pirmas = new JPasswordField();
		Slaptazodis_pirmas.setBounds(167, 106, 86, 20);
		contentPane.add(Slaptazodis_pirmas);
		
		Slaptazodis_antras = new JPasswordField();
		Slaptazodis_antras.setBounds(167, 131, 86, 20);
		contentPane.add(Slaptazodis_antras);
		
		JButton btnRegistruotis = new JButton("Registruotis");
		btnRegistruotis.setBounds(164, 182, 89, 23);
		contentPane.add(btnRegistruotis);
	}
}
