package com.menotyou.JC;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

// TODO: Auto-generated Javadoc
/**
 * The Class KambarioInterfeisas.
 */
public class KambarioInterfeisas extends JPanel{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant DATOS_FORMA. */
	private static final SimpleDateFormat DATOS_FORMA = new SimpleDateFormat("HH:mm:ss");
	
	/** The jtp. */
	private final JTabbedPane jtp;
	
	/** The klientas. */
	private final NIOKlientas klientas;
	
	/** The savininkas. */
	private final boolean savininkas;
	
	/** The pavadinimas. */
	private String pavadinimas;
	
	/** The mygtukas siusti. */
	private JButton mygtukasSiusti;
	
	/** The caret. */
	private Caret caret;
	
	/** The zinutes laukelis. */
	private JTextField zinutesLaukelis;
	
	/** The Istorija. */
	private JTextArea Istorija;
	
	/** The issokantis langelis. */
	private JPopupMenu issokantisLangelis;
	
	/** The vartotojo kontekstinis meniu. */
	private JPopupMenu vartotojoKontekstinisMeniu;
	
	/** The prisijunge vartotojai. */
	private List<String> prisijungeVartotojai = new ArrayList<String>();
	
	/** The vartotoju lentele. */
	private JTable vartotojuLentele;
	
	/** The menu isspirti. */
	private JMenuItem menuIsspirti;
	
	/** The lenteles modelis. */
	private DefaultTableModel lentelesModelis;
	
	/** The btn ieiti. */
	private JButton btnIeiti;
	
	/** The vartotoju meniu. */
	private JScrollPane vartotojuMeniu;

	/**
	 * Instantiates a new kambario interfeisas.
	 *
	 * @param jtp the jtp
	 * @param klientas the klientas
	 * @param pavadinimas the pavadinimas
	 * @param savininkas the savininkas
	 */
	public KambarioInterfeisas(final JTabbedPane jtp, final NIOKlientas klientas, final String pavadinimas, final boolean savininkas) {
		if (jtp == null) {
			throw new NullPointerException("TabbedPane is null");
		}
		this.jtp = jtp;
		this.klientas = klientas;
		this.pavadinimas = pavadinimas;
		this.savininkas = savininkas;
		setOpaque(false);
		setSize(700, 450);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 580, 10, 100 };
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
		Istorija.setCaret(caret);
		JScrollPane langelis = new JScrollPane(Istorija);
		GridBagConstraints gbc_langelis = new GridBagConstraints();
		gbc_langelis.gridwidth = 2;
		gbc_langelis.weightx = 0.5;
		gbc_langelis.weighty = 0.5;
		gbc_langelis.insets = new Insets(7, 0, 5, 5);
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
		lentelesModelis = new DefaultTableModel(new String[]{"Vartotojai"}, 0) {
			private static final long serialVersionUID = 7204177157604449599L;
			
		    public boolean isCellEditable(int row, int column) {
		       return false;
		    }
		};
		vartotojoKontekstinisMeniu = new JPopupMenu();
		vartotojoKontekstinisMeniu.setLabel("Vartotojo meniu");
		menuIsspirti = new JMenuItem("Išspirti");
		menuIsspirti.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Component c = (Component)e.getSource();
		        JPopupMenu popup = (JPopupMenu)c.getParent();
		        JTable lentele = (JTable)popup.getInvoker();
		        if(lentele.getValueAt(lentele.getSelectedRow(), 0).toString().equals(klientas.gaukVarda())){
		        	JOptionPane.showMessageDialog(null, "Savęs iš kambario išspirti negalima. Tam yra mygtukas 'Išeiti'.", "Klaida!", JOptionPane.INFORMATION_MESSAGE);
		        } else{
		        	klientas.siuskZinute("<KK>" + pavadinimas + "<V>" + lentele.getValueAt(lentele.getSelectedRow(), 0));
		        }
			}
		});
		if(!savininkas) menuIsspirti.setEnabled(false);
        vartotojoKontekstinisMeniu.add(menuIsspirti);
        
		vartotojuMeniu = new JScrollPane();
		GridBagConstraints gbc_vartotojuMeniu = new GridBagConstraints();
		gbc_vartotojuMeniu.fill = GridBagConstraints.BOTH;
		gbc_vartotojuMeniu.insets = new Insets(7, 0, 5, 5);
		gbc_vartotojuMeniu.gridx = 2;
		gbc_vartotojuMeniu.gridy = 0;
		add(vartotojuMeniu, gbc_vartotojuMeniu);
		vartotojuLentele = new JTable(lentelesModelis);
		vartotojuLentele.setBackground(Color.WHITE);
		vartotojuMeniu.setViewportView(vartotojuLentele);
		vartotojuLentele.setPreferredScrollableViewportSize(new Dimension(120, 410));
		vartotojuLentele.setRowSelectionAllowed(true);
		vartotojuLentele.addMouseListener( new MouseAdapter()
        {
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    JTable saltinis = (JTable)e.getSource();
                    int eilute = saltinis.rowAtPoint( e.getPoint() );
                    int stulpelis = saltinis.columnAtPoint( e.getPoint() );

                    if (! saltinis.isRowSelected(eilute))
                    	saltinis.changeSelection(eilute, stulpelis, false, false);

                    vartotojoKontekstinisMeniu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
		
		
		GridBagConstraints gbc_zinLauk = new GridBagConstraints();
		gbc_zinLauk.insets = new Insets(0, 0, 0, 5);
		gbc_zinLauk.fill = GridBagConstraints.HORIZONTAL;
		gbc_zinLauk.weightx = 0.5;
		gbc_zinLauk.gridx = 0;
		gbc_zinLauk.gridy = 1;
		add(zinutesLaukelis, gbc_zinLauk);
		zinutesLaukelis.setColumns(10);

		mygtukasSiusti = new JButton("Siųsti");
		mygtukasSiusti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				siustiZinute();
			}
		});
		GridBagConstraints gbc_btnSiusti = new GridBagConstraints();
		gbc_btnSiusti.insets = new Insets(0, 5, 0, 5);
		gbc_btnSiusti.gridx = 1;
		gbc_btnSiusti.gridy = 1;
		add(mygtukasSiusti, gbc_btnSiusti);
		
		btnIeiti = new JButton("Palikti pok.");
		btnIeiti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				klientas.panaikinkKambari(KambarioInterfeisas.this.pavadinimas);
				jtp.remove(KambarioInterfeisas.this); 
				repaint(); 
			}
		});
		GridBagConstraints gbc_btnIeiti = new GridBagConstraints();
		gbc_btnIeiti.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnIeiti.gridx = 2;
		gbc_btnIeiti.gridy = 1;
		add(btnIeiti, gbc_btnIeiti);

	}

	/**
	 * Nustatyk prisijungusius vartotojus.
	 *
	 * @param vartotojai the vartotojai
	 */
	public void nustatykPrisijungusiusVartotojus(String vartotojai) {
		String[] vartotojuSarasas = vartotojai.split("<T>|<END>");
		prisijungeVartotojai = new ArrayList<String>(vartotojuSarasas.length);
		prisijungeVartotojai.clear();
		for (int i = 0; i < vartotojuSarasas.length; i++){
			prisijungeVartotojai.add(vartotojuSarasas[i]);
			lentelesModelis.addRow(new Object[]{vartotojuSarasas[i]});
		}
		System.out.println("Prisijunge vartotojai(" + prisijungeVartotojai.size() + "):");
		for(String v : prisijungeVartotojai){
			System.out.println("   " + v);
		}
	}
	
	/**
	 * Nustatyk istorijos srifta.
	 *
	 * @param sriftas the sriftas
	 */
	public void nustatykIstorijosSrifta(Font sriftas){
		if(sriftas != null) Istorija.setFont(sriftas);
	}

	/**
	 * Pridek vartotoja.
	 *
	 * @param vardas the vardas
	 */
	public void pridekVartotoja(String vardas) {
		prisijungeVartotojai.add(vardas);
		lentelesModelis.addRow(new Object[]{vardas});
		spausdintiTeksta(vardas + " prisijungė prie pokalbio.");
	}
	
	/**
	 * Fokusuok zinutes laukeli.
	 */
	public void fokusuokZinutesLaukeli() {
	    zinutesLaukelis.requestFocusInWindow();
	}

	/**
	 * Pasalink vartotoja.
	 *
	 * @param vardas the vardas
	 * @param isspirtas the isspirtas
	 */
	public void pasalinkVartotoja(String vardas, boolean isspirtas) {
		prisijungeVartotojai.remove(vardas);
		for(int i = 0; i < lentelesModelis.getRowCount(); i++)
			if(lentelesModelis.getValueAt(i, 0).equals(vardas))
				lentelesModelis.removeRow(i);
		if(isspirtas) spausdintiTeksta(vardas + " buvo išspirtas.");
		else spausdintiTeksta(vardas + " paliko pokalbį.");
	}

	/**
	 * Spausdink zinute.
	 *
	 * @param zinute the zinute
	 * @param siuntejas the siuntejas
	 */
	public void spausdinkZinute(String zinute, String siuntejas) {
		Istorija.append("[" + DATOS_FORMA.format(new Date()) + "] " + (siuntejas == null ? "" : siuntejas + ": ") + zinute + "\n");
	}

	/**
	 * Spausdinti teksta.
	 *
	 * @param eilute the eilute
	 */
	public void spausdintiTeksta(String eilute) {
		Istorija.append(eilute + "\n");
	}

	/**
	 * Siusti zinute.
	 */
	private void siustiZinute() {
		if(zinutesLaukelis.getText().trim().isEmpty()){
			zinutesLaukelis.setText("");
			return;
		}
		String zinute = "<K>" + pavadinimas + "<Z>" + zinutesLaukelis.getText();
		zinutesLaukelis.setText("");
		klientas.siuskZinute(zinute);
	}


}
