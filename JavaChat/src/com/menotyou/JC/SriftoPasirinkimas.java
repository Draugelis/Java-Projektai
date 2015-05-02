package com.menotyou.JC;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.swing.DropMode;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class SriftoPasirinkimas.
 */
public class SriftoPasirinkimas extends JDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1686982470802696604L;

	/** The m_pasirinktas sriftas. */
	protected Font m_pasirinktasSriftas;
	
	/** The m_pasirinkto srifto pav. */
	protected String m_pasirinktoSriftoPav;
	
	/** The m_pasirinkto srifto dydis. */
	protected float m_pasirinktoSriftoDydis;

	/** The demo tekstas. */
	protected String demoTekstas = "[22:15:41] Demo prisijungė \n[22:15:44] Demo: Labas \n[22:16:15] Demo: Šiandien gražus oras! Niekio nuostabesnio nesu matęs!";

	/** The m_pavadinimai. */
	protected JComboBox<String> m_pavadinimai;
	
	/** The m_dydziai. */
	protected JComboBox<String> m_dydziai;

	/** The sriftu dydziai. */
	protected String sriftuDydziai[] = { "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "30" };

	/** The demo. */
	protected JTextArea demo;

	/**
	 * Instantiates a new srifto pasirinkimas.
	 *
	 * @param f the f
	 * @param dabartinis the dabartinis
	 */
	public SriftoPasirinkimas(Frame f, final Font dabartinis) {
		super(f, "Teksto nustatymai", true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				m_pasirinktasSriftas = dabartinis;
				dispose();
				setVisible(false);
			}
		});
		setTitle("Teksto nustatymai");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(500, 500);
		Container cp = getContentPane();

		Panel top = new Panel();
		top.setLayout(new FlowLayout());

		m_pavadinimai = new JComboBox<String>();
		top.add(m_pavadinimai);

		m_pasirinktoSriftoPav = dabartinis.getFontName();
		System.out.println("dabartinis.getFontName() = " + m_pasirinktoSriftoPav);
		int indeksas = 0;
		String[] m_sriftuPavadinimai = KlientoLangas.gaukSrfituPavadinimus();
		for (int i = 0; i < m_sriftuPavadinimai.length; i++){
			m_pavadinimai.addItem(m_sriftuPavadinimai[i].substring(0, m_sriftuPavadinimai[i].length() - 4));
			if(m_sriftuPavadinimai[i].equals(m_pasirinktoSriftoPav + ".ttf"))
				indeksas = i;
		}
		m_pavadinimai.setSelectedIndex(indeksas);
		System.out.println("Selected item: " + m_pavadinimai.getSelectedItem());

		m_dydziai = new JComboBox<String>();
		top.add(m_dydziai);

		m_pasirinktoSriftoDydis = dabartinis.getSize();
		for (int i = 0; i < sriftuDydziai.length; i++)
			m_dydziai.addItem(sriftuDydziai[i]);
		m_dydziai.setSelectedItem(Integer.toString(((int) m_pasirinktoSriftoDydis)));

		cp.add(top, BorderLayout.NORTH);
		Panel attrs = new Panel();
		top.add(attrs);
		attrs.setLayout(new GridLayout(0, 1));

		demo = new JTextArea();
		demo.setLineWrap(true);
		demo.setFont(dabartinis);
		demo.setAlignmentY(demo.CENTER_ALIGNMENT);
		demo.setText(demoTekstas);
		demo.setEditable(false);
		demo.setSize(300, 100);
		JScrollPane langelis = new JScrollPane(demo);
		cp.add(langelis, BorderLayout.CENTER);

		Panel bot = new Panel();

		JButton okButton = new JButton("OK");
		bot.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				demoPerziura();
				dispose();
				setVisible(false);
			}
		});

		JButton pvButton = new JButton("Bandyti");
		bot.add(pvButton);
		pvButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				demoPerziura();
			}
		});

		JButton canButton = new JButton("Atšaukti");
		bot.add(canButton);
		canButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_pasirinktasSriftas = dabartinis;
				dispose();
				setVisible(false);
			}
		});

		cp.add(bot, BorderLayout.SOUTH);

		demoPerziura();

		pack();
		setLocationRelativeTo(null);
	}
	
	/**
	 * Nustatyk srifta.
	 *
	 * @param sriftas the sriftas
	 */
	public void nustatykSrifta(Font sriftas){
		m_pasirinktasSriftas = sriftas;
	}

	/**
	 * Demo perziura.
	 */
	protected void demoPerziura() {
		m_pasirinktoSriftoPav = (String) m_pavadinimai.getSelectedItem();
		m_pasirinktoSriftoDydis = Float.parseFloat((String) m_dydziai.getSelectedItem());

		System.out.println("Pasirinko srifto pavadinimas: " + m_pasirinktoSriftoPav);
		System.out.println("Pasirinko srifto dydis: " + m_pasirinktoSriftoDydis);

		m_pasirinktasSriftas = KlientoLangas.gaukSriftus().get(m_pasirinktoSriftoPav + ".ttf").deriveFont((float) m_pasirinktoSriftoDydis);
		demo.setFont(m_pasirinktasSriftas);

		pack();
	}

	/**
	 * Gauk pasirinkto srifto pav.
	 *
	 * @return the string
	 */
	public String gaukPasirinktoSriftoPav() {
		return m_pasirinktoSriftoPav;
	}

	/**
	 * Gauk pasirinkto srifto dydi.
	 *
	 * @return the float
	 */
	public float gaukPasirinktoSriftoDydi() {
		return m_pasirinktoSriftoDydis;
	}

	/**
	 * Gauk pasirinkta srifta.
	 *
	 * @return the font
	 */
	public Font gaukPasirinktaSrifta() {
		return m_pasirinktasSriftas;
	}

}
