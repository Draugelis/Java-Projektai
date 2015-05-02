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

// TODO: Auto-generated Javadoc
/**
 * The Class JCPrisijungimas.
 */
public class JCPrisijungimas extends JFrame {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The content pane. */
	private JPanel contentPane;

	/** The serverio pavadinimas. */
	private String SERVERIO_PAVADINIMAS = "localhost";
	
	/** The serverio portas. */
	private int SERVERIO_PORTAS = 8192;
	
	/** The skaityti. */
	private BufferedReader skaityti;
	
	/** The rasyti. */
	private PrintWriter rasyti;

	/**
	 * Instantiates a new JC prisijungimas.
	 */
	public JCPrisijungimas() {
		sukurkLanga();
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
		btnVartotjas.setBounds(43, 178, 205, 30);
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
		
	}


	/**
	 * Login.
	 */
	public void login() {
		dispose();
		//Todo: Login veiksmas
	}
	
	/**
	 * Svecias.
	 */
	public void svecias(){
		dispose();
		
	}
	

	
}
