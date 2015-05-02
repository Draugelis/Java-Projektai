package com.menotyou.JC;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 *Pagrindinis programos langas. Jame talpinami kambarių langai ir visas vartotojo grafinis meniu.
 */
public class KlientoLangas extends JFrame {

	private static final long serialVersionUID = 1L;
	
	/** Komponentas kuris talpina kambarius. */
	private JTabbedPane jtp;
	
	/** Langas skirtas pasirinkti teksto šriftą. */
	private SriftoPasirinkimas sriftoPasirinkimas;
	
	/** Pasirinktas šriftas. */
	private Font pasirinktasSriftas;
	
	/** Kambario kūrimo langas. */
	private KambarioKurimas kambarioKurimas;
	
	/** Prisijungimo prie kambario langas.*/
	private PrisijungimasPrieKambario prisijungimasPrieKambario;
	
	/** Klasė atsakinga už visas kliento operacijas ir programos funkcionalumą.*/
	private static NIOKlientas klientas;

	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmExit;
	private JMenu mnNustatymai;
	private JMenuItem mntmTekstoNustatymai;
	private JMenu mnKambariai;
	private JMenuItem mntmPridtiKambar;
	
	/** Masyvas kambarių pavadinimams talpinti. */
	private static String[] kambariuSarasas;
	
	/** Progamos šriftų pavadinimai. Pastaba: šriftai saugomi programos archyve .ttf tipo failuose.*/
	private static String[] sriftuPavadinimai = {
		"Amble-Bold.ttf", "Amble-BoldItalic.ttf", "Amble-Italic.ttf", "Amble-Light.ttf", "Amble-LightCondensed.ttf",
		"Amble-LightCondensedItalic.ttf", "Amble-LightItalic.ttf", "Amble-Regular.ttf", "Anonymous_Pro_B.ttf",
		"Anonymous_Pro_BI.ttf", "Anonymous_Pro_I.ttf", "Anonymous_Pro.ttf",
		};
	private JMenuItem mntmJungtisPrieKambario;
	
	/** Map tipo kintamasis talpinti šritų Font objektus, kurie susiejami per pavadinimus. */
	private static Map<String, Font> sriftuSaugykla = new ConcurrentHashMap<String, Font>(sriftuPavadinimai.length);
	static {
		for (String name : sriftuPavadinimai) {
			sriftuSaugykla.put(name, gaukSrifta(name));
		}
	}
	
	/** Numatytasis šriftas. */
	private final Font NUMATYTASIS_SRIFTAS = sriftuSaugykla.get("Amble-LightCondensed.ttf").deriveFont(15.0f);

	private JMenu mnApie;
	private JMenuItem mntmPagalba;
	private JMenuItem mntmApieProgram;

	/**
	 * Klasės KlientoLangas konstruktorius, sukuriantis naują kliento langą.
	 */
	public KlientoLangas() {
		sukurkLanga();
		pasirinktasSriftas = NUMATYTASIS_SRIFTAS;
	}

	/**
	 * Funkcija lango kūrimui.
	 */
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
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				klientas.siuskZinute("<Q>");
			}
		});

		mntmExit = new JMenuItem("I\u0161eiti");
		mnFile.add(mntmExit);

		mnNustatymai = new JMenu("Nustatymai");
		menuBar.add(mnNustatymai);

		mntmTekstoNustatymai = new JMenuItem("Teksto nustatymai");
		mntmTekstoNustatymai.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sriftoPasirinkimas = new SriftoPasirinkimas(KlientoLangas.this, pasirinktasSriftas);
				sriftoPasirinkimas.setVisible(true);
				if(pasirinktasSriftas != sriftoPasirinkimas.gaukPasirinktaSrifta()){
					pasirinktasSriftas = sriftoPasirinkimas.gaukPasirinktaSrifta();
					klientas.nustatykSriftus(pasirinktasSriftas);
				}
			}
		});
		mnNustatymai.add(mntmTekstoNustatymai);

		mnKambariai = new JMenu("Kambariai");
		menuBar.add(mnKambariai);

		mntmPridtiKambar = new JMenuItem("Pridėti kambarį");
		mntmPridtiKambar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kambarioKurimas = new KambarioKurimas(KlientoLangas.this, pasirinktasSriftas);
			}
		});
		mnKambariai.add(mntmPridtiKambar);

		mntmJungtisPrieKambario = new JMenuItem("Jungtis prie kambario");
		mntmJungtisPrieKambario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prisijungimasPrieKambario = new PrisijungimasPrieKambario(KlientoLangas.this);
				klientas.siuskZinute("<KS>");
			}
		});
		mnKambariai.add(mntmJungtisPrieKambario);
		
		mnApie = new JMenu("Apie");
		menuBar.add(mnApie);
		
		mntmPagalba = new JMenuItem("D.U.K");
		mntmPagalba.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new DUK();
			}
		});
		mnApie.add(mntmPagalba);
		
		mntmApieProgram = new JMenuItem("Apie programą");
		mntmApieProgram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ApiePrograma();
			}
		});
		mnApie.add(mntmApieProgram);

		jtp = new JTabbedPane();
		jtp.setBorder(new EmptyBorder(5, 5, 5, 5));
		jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		setContentPane(jtp);

	}
	
	/**
	 * Gražinamas kambario kūrimo lango objektas.
	 *
	 * @return KambarioKurimas objektas.
	 */
	public KambarioKurimas gaukKK(){
		return kambarioKurimas;
	}
	
	/**
	 * Gražinamas prisijungimo prie kambario lango objektas.
	 *
	 * @return PrisijungimasPrieKambario objektas.
	 */
	public PrisijungimasPrieKambario gaukPPk(){
		return prisijungimasPrieKambario;
	}
	
	/**
	 * Sukuria kambario interfeisą.
	 *
	 * @param pavadinimas -> kambario pavadinimas. 
	 * @param savininkas -> ar vartotjas yra kambario savininkas.
	 */
	public void sukurkKambarioInterfeisa(String pavadinimas, boolean savininkas) {
		KambarioInterfeisas k = new KambarioInterfeisas(jtp, klientas, pavadinimas, savininkas);
		k.nustatykIstorijosSrifta(pasirinktasSriftas);
		if (klientas.pridekKambari(pavadinimas, k)) {
			jtp.addTab(pavadinimas, k);
		} else {
			JOptionPane.showMessageDialog(null, "Toks kambarys jau egzistuoja!", "Klaida!", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Gražinkamas kliento objektas. 
	 *
	 * @return NIOKliento objektas.
	 */
	public NIOKlientas gaukKlienta() {
		return klientas;
	}

	/**
	 * Sukuriamas naujas kliento objektas ir priskiriamas kliento langui.
	 *
	 * @param sp -> SvecioPrisijungimas objektas;
	 */
	public void startKlientas(SvecioPrisijungimas sp) {
		try {
			klientas = new NIOKlientas(KlientoLangas.this, sp);
		} catch (IOException e) {
			System.out.println("Nepavyko paleisti kliento!");
			e.printStackTrace();
		}
		klientas.start();
	}
	
	/**
	 * Iš žinutės išrenkami kambarių pavadinimai.
	 * Jei yra iškviestas PrisijungimasPrieKambario langas, jame esantys kambariai yra atnaujinami.
	 *
	 * @param kambariai -> Žinutė su kambarių pavadinimais.
	 */
	public void nustatykKambariuSarasa(String kambariai){
		kambariuSarasas = kambariai.split("<K>|<END>");
		System.out.println("Kambariai(" + kambariuSarasas.length + "): ");
		if (prisijungimasPrieKambario != null) prisijungimasPrieKambario.nustatykKambarius(kambariuSarasas);
	}

	/**
	 * Gražinami klientoLango saugomi šriftai.
	 *
	 * @return Šriftus saugantis objektas sriftuSaugykla.
	 */
	public static Map<String, Font> gaukSriftus() {
		return sriftuSaugykla;
	}

	/**
	 * Gauk srfitu pavadinimus.
	 *
	 * @return the string[]
	 */
	public static String[] gaukSrfituPavadinimus() {
		return sriftuPavadinimai;
	}

	/**
	 * Gauk srifta.
	 *
	 * @return the font
	 */
	public Font gaukSrifta() {
		return pasirinktasSriftas;
	}
	
	/**
	 * Gauk kambarius.
	 *
	 * @return the string[]
	 */
	public static String[] gaukKambarius(){
		return kambariuSarasas;
	}

	/**
	 * Gauk srifta.
	 *
	 * @param pavadinimas the pavadinimas
	 * @return the font
	 */
	private static Font gaukSrifta(String pavadinimas) {
		Font font = null;
		if (sriftuSaugykla != null) {
			if ((font = sriftuSaugykla.get(pavadinimas)) != null) {
				return font;
			}
		}
		String fVardas = "/" + pavadinimas;
		try {
			InputStream is = SriftoPasirinkimas.class.getResourceAsStream(fVardas);
			font = Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(fVardas + " neužkrautas.  Naudojamas serif šriftas.");
			font = new Font("serif", Font.PLAIN, 24);
		}
		return font;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					KlientoLangas frame = new KlientoLangas();
					frame.setVisible(false);
					SvecioPrisijungimas svecias = new SvecioPrisijungimas(frame);
					svecias.setVisible(false);
					new Uzsklanda(svecias);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}