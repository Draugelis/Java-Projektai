package com.menotyou.JC;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KambarioInterfeisas extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final SimpleDateFormat DATOS_FORMA = new SimpleDateFormat("HH:mm:ss");
	private final JTabbedPane jtp;
	private String pavadinimas;
	private final NIOKlientas klientas;
	private JButton mygtukasSiusti;
	private Caret caret;
	private JTextField zinutesLaukelis;
	private JTextArea Istorija;
	private JPopupMenu issokantisLangelis;
	private List<String> prisijungeVartotojai = new ArrayList<String>();

	public KambarioInterfeisas(final JTabbedPane jtp, final NIOKlientas klientas, String pavadinimas) {
		if (jtp == null) {
			throw new NullPointerException("TabbedPane is null");
		}
		this.jtp = jtp;
		this.klientas = klientas;
		this.pavadinimas = pavadinimas;
		setOpaque(false);
		setSize(700, 450);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 680, 10, 10 };
		gridBagLayout.rowHeights = new int[] { 430, 20 };
		setLayout(gridBagLayout);

		issokantisLangelis = new JPopupMenu();
		JMenuItem uzdarymas = new JMenuItem("Uždaryti");
		uzdarymas.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				klientas.panaikinkKambari(KambarioInterfeisas.this.pavadinimas);
				jtp.remove(KambarioInterfeisas.this); 
				repaint(); 
			}
		});
		issokantisLangelis.add(uzdarymas);
		JMenuItem beveikVisuUzdarymas = new JMenuItem("Uždaryti visus kitus");
		beveikVisuUzdarymas.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int i = jtp.getSelectedIndex(); 
				int count = jtp.getTabCount(); 
				for (int j = count-1 ; j >= 0 ; j--) { 
					if (j!=i) {
						klientas.panaikinkKambari(jtp.getTitleAt(i));
						jtp.remove(j) ; 
					}
				} 
				repaint() ;
			}
		});
		issokantisLangelis.add(beveikVisuUzdarymas);
		this.setComponentPopupMenu(issokantisLangelis);

		Istorija = new JTextArea();
		Istorija.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		Istorija.setEditable(false);
		caret = (DefaultCaret) Istorija.getCaret();
		((DefaultCaret) caret).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane langelis = new JScrollPane(Istorija);
		GridBagConstraints gbc_langelis = new GridBagConstraints();
		gbc_langelis.gridwidth = 3;
		gbc_langelis.weightx = 0.5;
		gbc_langelis.weighty = 0.5;
		gbc_langelis.insets = new Insets(7, 0, 0, 5);
		gbc_langelis.fill = GridBagConstraints.BOTH;
		gbc_langelis.gridx = 0;
		gbc_langelis.gridy = 0;
		add(langelis, gbc_langelis);

		zinutesLaukelis = new JTextField();
		zinutesLaukelis.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					siustiZinute();
				}
			}
		});
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.weightx = 0.5;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		add(zinutesLaukelis, gbc_textField);
		zinutesLaukelis.setColumns(10);

		mygtukasSiusti = new JButton("Siųsti");
		mygtukasSiusti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				siustiZinute();
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridwidth = 2;
		gbc_btnNewButton.insets = new Insets(0, 5, 0, 0);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 1;
		add(mygtukasSiusti, gbc_btnNewButton);

	}

	public void nustatykPrisijungusiusVartotojus(String vartotojai) {
		String[] vartotojuSarasas = vartotojai.split("<T>|<END>");
		prisijungeVartotojai = new ArrayList<String>(vartotojuSarasas.length);
		prisijungeVartotojai.clear();
		for (int i = 0; i < vartotojuSarasas.length; i++)
			prisijungeVartotojai.add(vartotojuSarasas[i]);
		System.out.println("Prisijunge vartotojai(" + prisijungeVartotojai.size() + "):");
		for(String v : prisijungeVartotojai){
			System.out.println("   " + v);
		}
	}
	public void nustatykIstorijosSrifta(Font sriftas){
		if(sriftas != null) Istorija.setFont(sriftas);
	}

	public void pridekVartotoja(String vardas) {
		prisijungeVartotojai.add(vardas);
		spausdintiTeksta(vardas + " prisijungė prie pokalbio.");
	}
	public void fokusuokZinutesLaukeli() {
	    zinutesLaukelis.requestFocusInWindow();
	}

	public void pasalinkVartotoja(String vardas) {
		prisijungeVartotojai.remove(vardas);
		spausdintiTeksta(vardas + " paliko pokalbį.");
	}

	public void spausdinkZinute(String zinute, String siuntejas) {
		Istorija.append("[" + DATOS_FORMA.format(new Date()) + "] " + (siuntejas == null ? "" : siuntejas + ": ") + zinute + "\n");
	}

	public void spausdintiTeksta(String eilute) {
		Istorija.append(eilute + "\n");
	}

	private void siustiZinute() {
		if(zinutesLaukelis.getText().trim().isEmpty()){
			zinutesLaukelis.setText("");
			return;
		}
		String zinute = "<K>" + pavadinimas + "<Z>" + zinutesLaukelis.getText();
		System.out.println(pavadinimas + " Siunčia žinutę: " + zinute);
		zinutesLaukelis.setText("");
		klientas.siuskZinute(zinute);
	}
}
