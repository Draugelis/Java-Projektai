package com.menotyou.JC;

import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.Charset;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class KlientoLangas extends JFrame {

//	private final Font NUMATYTASIS_SRIFTAS = new Font();
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField zinutesLaukelis;
	private JTextArea Istorija;
	private DefaultCaret caret;
	private Klientas klientas;
	private Frame langas;
	private SriftoPasirinkimas fc;
	private Font pasirinktasSriftas;

	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;
	private JMenu mnNustatymai;
	private JMenuItem mntmTekstoNustatymai;
	private JMenuItem mntmVartotojoNustatymai;

	public KlientoLangas(String vardas, Klientas klientas) {
		setTitle("JC klientas");
		sukurkLanga();
		setTitle("JavaChat Client - " + vardas);
		this.klientas = klientas;
		langas = this;
		klientas.susiekSuKlientu(this);
	}

	private void sukurkLanga() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 550);
		setLocationRelativeTo(null);

		menuBar = new JMenuBar();
		menuBar.setFont(new Font("Bookman Old Style", Font.PLAIN, 15));
		setJMenuBar(menuBar);

		mnFile = new JMenu("Meniu");
		menuBar.add(mnFile);

		mntmOnlineUsers = new JMenuItem("Prisijung\u0119 vartotojai");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				onlineUsers.setVisible(true);
			}
		});
		mnFile.add(mntmOnlineUsers);
		

		mntmExit = new JMenuItem("I\u0161eiti");
		mnFile.add(mntmExit);
		
		mnNustatymai = new JMenu("Nustatymai");
		menuBar.add(mnNustatymai);
		
		mntmTekstoNustatymai = new JMenuItem("Teksto nustatymai");
		mntmTekstoNustatymai.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc = new SriftoPasirinkimas(langas, Istorija.getFont());
				fc.setVisible(true);
				pasirinktasSriftas = fc.gaukPasirinktaSrifta();
				Istorija.setFont(pasirinktasSriftas);
			}
		});
		mnNustatymai.add(mntmTekstoNustatymai);
		
		mntmVartotojoNustatymai = new JMenuItem("Vartotojo nustatymai");
		mnNustatymai.add(mntmVartotojoNustatymai);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 23, 740, 30, 7 };
		gbl_contentPane.rowHeights = new int[] { 10, 530, 10 };
		contentPane.setLayout(gbl_contentPane);

		Istorija = new JTextArea();
		Istorija.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		Istorija.setEditable(false);
		caret = (DefaultCaret) Istorija.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scroll = new JScrollPane(Istorija);
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 5, 5, 5);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 3;
		scrollConstraints.gridheight = 2;
		scrollConstraints.weightx = 1;
		scrollConstraints.weighty = 1;
		contentPane.add(scroll, scrollConstraints);

		zinutesLaukelis = new JTextField();
		zinutesLaukelis.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		zinutesLaukelis.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					siusti(zinutesLaukelis.getText());
				}
			}
		});
		GridBagConstraints gbc_zinutesLaukelis = new GridBagConstraints();
		gbc_zinutesLaukelis.insets = new Insets(0, 5, 5, 5);
		gbc_zinutesLaukelis.fill = GridBagConstraints.HORIZONTAL;
		gbc_zinutesLaukelis.gridx = 0;
		gbc_zinutesLaukelis.gridy = 2;
		gbc_zinutesLaukelis.gridwidth = 2;
		gbc_zinutesLaukelis.weightx = 1;
		contentPane.add(zinutesLaukelis, gbc_zinutesLaukelis);
		zinutesLaukelis.setColumns(10);

		JButton mygtukasSiusti = new JButton("Si\u0173sti");
		mygtukasSiusti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				siusti(zinutesLaukelis.getText());
			}
		});
		GridBagConstraints gbc_mygtukasSiusti = new GridBagConstraints();
		gbc_mygtukasSiusti.insets = new Insets(0, 0, 5, 5);
		gbc_mygtukasSiusti.gridx = 2;
		gbc_mygtukasSiusti.gridy = 2;
		gbc_mygtukasSiusti.weightx = 0;
		gbc_mygtukasSiusti.weighty = 0;
		contentPane.add(mygtukasSiusti, gbc_mygtukasSiusti);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				klientas.atsijunk();
			}
		});
		setVisible(true);

		zinutesLaukelis.requestFocusInWindow();
	}
	private JPanel sukurkKambarioInterfeisa(String pavadinimas, ){
		
	}
	public void papildykIstorija(String zinute){
		Istorija.append(zinute + "\n");
	}
	
	public void siusti(String zinute){
		if(!zinute.isEmpty()){
			klientas.siuntejas.siuskZinute(zinute);
			zinutesLaukelis.setText("");
		}
	}




}