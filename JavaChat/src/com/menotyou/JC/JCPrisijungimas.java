package com.menotyou.JC;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class JCPrisijungimas extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private String SERVERIO_PAVADINIMAS = "localhost";
	private int SERVERIO_PORTAS = 8192;
	private BufferedReader skaityti;
	private PrintWriter rasyti;
	private Klientas klientas;

	public JCPrisijungimas() {
		sukurkLanga();
		try{
			Socket prieiga = new Socket(SERVERIO_PAVADINIMAS, SERVERIO_PORTAS);
			klientas = new Klientas(prieiga);
			klientas.gavejas = new Gavejas(klientas);
			klientas.siuntejas = new Siuntejas(klientas);
			klientas.gavejas.start();
			klientas.siuntejas.start();
			System.out.println("Prisjungta prie serverio " + SERVERIO_PAVADINIMAS + ":" + SERVERIO_PORTAS);
		} catch(IOException ioe) {
			System.out.println("Nepavyko susisiekti su serveriu!");
		}
	}
	public void sukurkLanga(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("JC Prisijungimas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(320, 360);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnVartotjas = new JButton("Vartotojas, noriu prisijungti");
		btnVartotjas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});
		btnVartotjas.setBounds(43, 223, 205, 30);
		contentPane.add(btnVartotjas);
		
		JButton btnSvecias = new JButton("Sve\u010Dias, nesiregistruosiu");
		btnSvecias.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				svecias();
			}
		});
		btnSvecias.setBounds(43, 134, 205, 33);
		contentPane.add(btnSvecias);
		
		JLabel lblNewLabel = new JLabel("<html>\r\n<b>Sveiki atvyk\u0119 \u012F JC!</b>\r\n<br><br>\r\nJ\u016Bs esate....\r\n</html>");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Times New Roman", Font.ITALIC, 16));
		lblNewLabel.setBounds(0, 11, 294, 112);
		contentPane.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Sve\u010Dias, bet noriu u\u017Esiregistruoti");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				registracija();
			}
		});
		btnNewButton.setBounds(43, 178, 205, 33);
		contentPane.add(btnNewButton);
	}
	private boolean susisiekSuServeriu(){
		try{
			Socket prieiga = new Socket(SERVERIO_PAVADINIMAS, SERVERIO_PORTAS);
			klientas = new Klientas(prieiga);
			klientas.gavejas = new Gavejas(klientas);
			klientas.siuntejas = new Siuntejas(klientas);
			klientas.gavejas.start();
			klientas.siuntejas.start();
			System.out.println("Prisjungta prie serverio " + SERVERIO_PAVADINIMAS + ":" + SERVERIO_PORTAS);
			return true;
		} catch(IOException ioe) {
			System.out.println("Nepavyko susisiekti su serveriu!");
			return false;
		}
		
	}


	public void login() {
		dispose();
		//Todo: Login veiksmas
	}
	public void registracija(){
		dispose();
		new Registracija();
	}
	
	public void svecias(){
		boolean pavyko = susisiekSuServeriu();
		if(pavyko){
			dispose();
			new SvecioPrisijungimas(klientas);
		} else {
			JOptionPane.showMessageDialog(null, "Nepavyko susisiekti su serveriu", "Klaida!", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JCPrisijungimas frame = new JCPrisijungimas();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
}
