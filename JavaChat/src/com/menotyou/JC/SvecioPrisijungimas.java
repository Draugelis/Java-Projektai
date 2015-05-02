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
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class SvecioPrisijungimas.
 */
public class SvecioPrisijungimas extends JFrame {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8952323351211994022L;
	
	/** The content pane. */
	private JPanel contentPane;
	
	/** The txt vardas. */
	private JTextField txtVardas;
	
	/** The btn prisijungti. */
	private JButton btnPrisijungti;
	
	/** The lbl vardas. */
	private JLabel lblVardas;
	
	/** The kl. */
	private KlientoLangas kl;
	
	/** The pswd laukelis. */
	private JPasswordField pswdLaukelis;
	
	/** The progress bar. */
	private JProgressBar progressBar;
	
	/** The Krovimosi_tekstas. */
	private JLabel Krovimosi_tekstas;

	
	/**
	 * Instantiates a new svecio prisijungimas.
	 *
	 * @param kl the kl
	 */
	public SvecioPrisijungimas(KlientoLangas kl) {
		sukurkLanga();
		this.kl = kl;
	}
	
	/**
	 * Sukurk langa.
	 */
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
		txtVardas.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					Prisijungimas();
				}
			}
		});
		txtVardas.setBackground(new Color(255, 255, 255));
		txtVardas.setBounds(78, 77, 111, 30);
		contentPane.add(txtVardas);
		txtVardas.setColumns(10);
		
		lblVardas = new JLabel("Vardas");
		lblVardas.setBounds(105, 43, 47, 30);
		contentPane.add(lblVardas);
		
		btnPrisijungti = new JButton("Prisijungti");
		btnPrisijungti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Prisijungimas();
			}
		});
		btnPrisijungti.setBounds(88, 191, 89, 23);
		contentPane.add(btnPrisijungti);
		
		pswdLaukelis = new JPasswordField();
		pswdLaukelis.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					Prisijungimas();
				}
			}
		});
		pswdLaukelis.setBounds(78, 152, 111, 28);
		contentPane.add(pswdLaukelis);
		
		JLabel lblSlaptaodis = new JLabel("Slaptažodis");
		lblSlaptaodis.setBounds(99, 118, 64, 23);
		contentPane.add(lblSlaptaodis);
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setBounds(0, 308, 304, 14);
		progressBar.setVisible(false);
		contentPane.add(progressBar);
		
		Krovimosi_tekstas = new JLabel("Siunčiama serveriui..");
		Krovimosi_tekstas.setHorizontalAlignment(SwingConstants.CENTER);
		Krovimosi_tekstas.setBounds(35, 283, 219, 14);
		Krovimosi_tekstas.setVisible(false);
		contentPane.add(Krovimosi_tekstas);
	}
	
	/**
	 * Klaida.
	 *
	 * @param klaida the klaida
	 */
	public void klaida(String klaida){
		JOptionPane.showMessageDialog(null, klaida, "Klaida!", JOptionPane.INFORMATION_MESSAGE);
		KeistiKrovimoTeksta("Siunčiama serveriui..", 0);
		Krovimosi_tekstas.setVisible(false);
		progressBar.setVisible(false);
		btnPrisijungti.setEnabled(true);
	}
	
	/**
	 * Prisijungimo uzbaigimas.
	 *
	 * @param vardas the vardas
	 */
	public void PrisijungimoUzbaigimas(String vardas){
		System.out.println("Prisijungta!");
		kl.setTitle("JC klientas - " + vardas);
		kl.setVisible(true);
		this.dispose();
	}
	
	/**
	 * Keisti krovimo teksta.
	 *
	 * @param tekstas the tekstas
	 * @param n the n
	 */
	public void KeistiKrovimoTeksta(String tekstas, int n){
		Krovimosi_tekstas.setText(tekstas);
		progressBar.setValue(n);
	}
	
	/**
	 * Prisijungimas.
	 */
	public void Prisijungimas(){
		String vardas = txtVardas.getText().trim();
		String slaptazodis = (new String(pswdLaukelis.getPassword())).trim();
		if(!vardas.isEmpty() && !slaptazodis.isEmpty()){
			kl.startKlientas(SvecioPrisijungimas.this);
			NIOKlientas klientas = kl.gaukKlienta();
			if(klientas != null) {
				btnPrisijungti.setEnabled(false);
				Krovimosi_tekstas.setVisible(true);
				progressBar.setVisible(true);
				klientas.pradekAutentifikacija(vardas, slaptazodis);
				KeistiKrovimoTeksta("Autentifikuojama...", 23);
			}
			else{
				klaida("Nepavyko susisiekti su serveriu!");
				System.out.println("Klientas == null");
			}
		} else {
			klaida("Neužpildyti visi laikeliai!");
		}
	}
}
