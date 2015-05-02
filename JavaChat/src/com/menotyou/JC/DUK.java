package com.menotyou.JC;

import java.awt.Component;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;

import org.eclipse.wb.swing.FocusTraversalOnArray;

/**
 * Klasė DUK. Langas skirtas dažnai užduodamiems klausimams 
 * ir atsakymams talpinti.
 */
public class DUK extends JFrame {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Sukuria nauja DUK langą
	 */
	public DUK() {
		setResizable(false);
		setTitle("D.U.K");
		setType(Type.UTILITY);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 39, 585, 276);
		getContentPane().add(scrollPane);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		
		JLabel lblklausimasKaipSisti = new JLabel("<html>\r\n<p><b>Klausimas: </b> Kaip siųsti žinutę?</p>\r\n<p><b>Atsakymas: </b>Pirma turite būti prisijungę prie kambario, tuomet lango apačioje bus laukelis (į kairę nuo mygtuko \"Siųsti\"), įveskite tekstą ir spauskite \"Siųsti\" arba klavišą ENTER.</p>\r\n</html>");
		
		JLabel lblklausimasKK = new JLabel("<html>\r\n<p><b>Klausimas: </b> Kaip sukurti kambarį?</p>\r\n<p><b>Atsakymas: </b>Kurti kambarį galima pasirinkus Kambariai->Pridėti kambarį.</p>\r\n</html>");
		
		JLabel lblklausimasPKK = new JLabel("<html>\r\n<p><b>Klausimas: </b> Kaip prisijungti prie jau sukurto kambario?</p>\r\n<p><b>Atsakymas: </b>Prisijungimą rasite Kambariai->Jungtis prie kambario.</p>\r\n</html>");
		
		JLabel lblklausimasTekstoNustatymas = new JLabel("<html>\r\n<p><b>Klausimas: </b> Ar galima keisti žinučiu teksto formatą?</p>\r\n<p><b>Atsakymas: </b>Taip. Nustatymai->Teksto nustatymai galite pasikeisti žinučių istorijos teksto dydį ir stilių.</p>\r\n</html>");
		
		JLabel lbliseitiIsKambario = new JLabel("<html>\r\n<p><b>Klausimas: </b> Kaip išeiti iš kambario?</p>\r\n<p><b>Atsakymas: </b>Lango apačioje yra mygtykas \"Palikti pok.\"</p>\r\n</html>");
		
		JLabel lblKambarioSav = new JLabel("<html>\r\n<p><b>Klausimas: </b> Ar kambario savininkas turi papildomų galimybių?</p>\r\n<p><b>Atsakymas: </b>Taip. Dešinėje esančiame vartotojų meniu savininkas gali dešinės pelės klavišu pasirinkti asmenį esantį kambaryje ir jį išspirti. Išspirtas vartotojas neturės teisės sugrįžti į kambarį.</p>\r\n</html>");
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblklausimasKK, 0, 0, Short.MAX_VALUE)
						.addComponent(lbliseitiIsKambario, GroupLayout.PREFERRED_SIZE, 558, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblKambarioSav, GroupLayout.PREFERRED_SIZE, 558, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblklausimasKaipSisti, GroupLayout.PREFERRED_SIZE, 558, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblklausimasPKK, GroupLayout.PREFERRED_SIZE, 558, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblklausimasTekstoNustatymas, GroupLayout.PREFERRED_SIZE, 558, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(lblklausimasKaipSisti, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lbliseitiIsKambario, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblKambarioSav, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblklausimasKK, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblklausimasPKK, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblklausimasTekstoNustatymas, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		panel.setLayout(gl_panel);
		
		JLabel lblPavadinimas = new JLabel("Dažnai užduodami klausimai");
		lblPavadinimas.setBounds(10, 5, 257, 23);
		lblPavadinimas.setFont(new Font("Verdana", Font.PLAIN, 18));
		getContentPane().add(lblPavadinimas);
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{getContentPane(), scrollPane, panel, lblklausimasKaipSisti, lblPavadinimas}));
	}
}
