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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class KlientoLangas extends JFrame {

	
	private static final long serialVersionUID = 1L;
	private JTabbedPane jtp;
	private SriftoPasirinkimas fc;
	private Font pasirinktasSriftas;
	private KambarioKurimas kk;
	private PrisijungimasPrieKambario ppk;
	private static NIOKlientas klientas;

	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;
	private JMenu mnNustatymai;
	private JMenuItem mntmTekstoNustatymai;
	private JMenuItem mntmVartotojoNustatymai;
	private JMenu mnKambariai;
	private JMenuItem mntmPridtiKambar;
	private static String[] kambariuSarasas;
	private static String[] sriftuPavadinimai = {
		"Amble-Bold.ttf", "Amble-BoldItalic.ttf", "Amble-Italic.ttf", "Amble-Light.ttf", "Amble-LightCondensed.ttf",
		"Amble-LightCondensedItalic.ttf", "Amble-LightItalic.ttf", "Amble-Regular.ttf", "Anonymous_Pro_B.ttf",
		"Anonymous_Pro_BI.ttf", "Anonymous_Pro_I.ttf", "Anonymous_Pro.ttf",
		};
	private JMenuItem mntmJungtisPrieKambario;
	private static Map<String, Font> sriftuSaugykla = new ConcurrentHashMap<String, Font>(sriftuPavadinimai.length);
	static {
		for (String name : sriftuPavadinimai) {
			sriftuSaugykla.put(name, gaukSrifta(name));
		}
	}
	private final Font NUMATYTASIS_SRIFTAS = sriftuSaugykla.get("Amble-LightCondensed.ttf").deriveFont(15.0f);

	public KlientoLangas() {
		sukurkLanga();
		pasirinktasSriftas = NUMATYTASIS_SRIFTAS;
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
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				klientas.siuskZinute("<Q>");
			}
		});

		mntmOnlineUsers = new JMenuItem("Prisijung\u0119 vartotojai");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//onlineUsers.setVisible(true);
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
				fc = new SriftoPasirinkimas(KlientoLangas.this, pasirinktasSriftas);
				fc.setVisible(true);
				if(pasirinktasSriftas != fc.gaukPasirinktaSrifta()){
					pasirinktasSriftas = fc.gaukPasirinktaSrifta();
					klientas.nustatykSriftus(pasirinktasSriftas);
				}
			}
		});
		mnNustatymai.add(mntmTekstoNustatymai);

		mntmVartotojoNustatymai = new JMenuItem("Vartotojo nustatymai");
		mnNustatymai.add(mntmVartotojoNustatymai);

		mnKambariai = new JMenu("Kambariai");
		menuBar.add(mnKambariai);

		mntmPridtiKambar = new JMenuItem("Pridėti kambarį");
		mntmPridtiKambar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kk = new KambarioKurimas(KlientoLangas.this, pasirinktasSriftas);
			}
		});
		mnKambariai.add(mntmPridtiKambar);

		mntmJungtisPrieKambario = new JMenuItem("Jungtis prie kambario");
		mntmJungtisPrieKambario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ppk = new PrisijungimasPrieKambario(KlientoLangas.this);
				klientas.siuskZinute("<KS>");
			}
		});
		mnKambariai.add(mntmJungtisPrieKambario);

		jtp = new JTabbedPane();
		jtp.setBorder(new EmptyBorder(5, 5, 5, 5));
		jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		setContentPane(jtp);

	}
	public KambarioKurimas gaukKK(){
		return kk;
	}
	public PrisijungimasPrieKambario gaukPPk(){
		return ppk;
	}
	public void sukurkKambarioInterfeisa(String pavadinimas) {
		System.out.println("Kuriamas kambarys pavadinimu:" + pavadinimas);
		KambarioInterfeisas k = new KambarioInterfeisas(jtp, klientas, pavadinimas);
		k.nustatykIstorijosSrifta(pasirinktasSriftas);
		if (klientas.pridekKambari(pavadinimas, k)) {
			jtp.addTab(pavadinimas, k);
		} else {
			JOptionPane.showMessageDialog(null, "Toks kambarys jau egzistuoja!", "Klaida!", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	public NIOKlientas gaukKlienta() {
		return klientas;
	}

	public void startKlientas(SvecioPrisijungimas sp) {
		try {
			klientas = new NIOKlientas(KlientoLangas.this, sp);
		} catch (IOException e) {
			System.out.println("Nepavyko paleisti kliento!");
			e.printStackTrace();
		}
		klientas.start();
	}
	public void nustatykKambariuSarasa(String kambariai){
		kambariuSarasas = kambariai.split("<K>|<END>");
		System.out.println("Kambariai(" + kambariuSarasas.length + "): ");
		for(String k : kambariuSarasas){
			System.out.println("   " + k);
		}
		if (ppk != null) ppk.nustatykKambarius(kambariuSarasas);
	}

	public static Map<String, Font> gaukSriftus() {
		return sriftuSaugykla;
	}

	public static String[] gaukSrfituPavadinimus() {
		return sriftuPavadinimai;
	}

	public Font gaukSrifta() {
		return pasirinktasSriftas;
	}
	public static String[] gaukKambarius(){
		return kambariuSarasas;
	}

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

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					KlientoLangas frame = new KlientoLangas();
					frame.setVisible(false);
					SvecioPrisijungimas svecias = new SvecioPrisijungimas(frame);
					svecias.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}