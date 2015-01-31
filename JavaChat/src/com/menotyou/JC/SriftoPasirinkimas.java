package com.menotyou.JC;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;


public class SriftoPasirinkimas extends JDialog {

  protected Font pasirinktasSriftas;
  protected String pasirinktoSriftoPav;
  protected int pasirinktoSriftoDydis;
  protected int pasirinktoSriftoStilius;
  protected boolean yraParyskintas, yraPasviras;


  protected String demoTekstas = " [22:15:41] Demo prisijungë \n[22:15:44] Demo: Labas \n[22:16:15] Demo: Ðiandien graþus oras! ";

  protected String sriftoPavadinimai[];
  
  protected JComboBox pavadinimai;
  protected JComboBox dydziai;

  Checkbox paryskintas, pasviras;

  protected String sriftuDydziai[] = { "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18","19","20","21","22",
      "23", "24", "30"};

  protected JTextArea demo;


public SriftoPasirinkimas(Frame f, final Font dabartinis) {
    super(f, "Font Chooser", true);
    setTitle("Teksto nustatymai");

    Container cp = getContentPane();

    Panel top = new Panel();
    top.setLayout(new FlowLayout());

    pavadinimai = new JComboBox();
    top.add(pavadinimai);

    sriftoPavadinimai = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    pasirinktoSriftoPav = dabartinis.getFontName();
    for (int i = 0; i < sriftoPavadinimai.length; i++)
    	pavadinimai.addItem(sriftoPavadinimai[i]);
    pavadinimai.setSelectedItem(pasirinktoSriftoPav);

    dydziai = new JComboBox();
    top.add(dydziai);
    
    
    pasirinktoSriftoDydis = dabartinis.getSize();
    for (int i = 0; i < sriftuDydziai.length; i++){
      dydziai.addItem(sriftuDydziai[i]);
    }
    dydziai.setSelectedItem(pasirinktoSriftoDydis);

    cp.add(top, BorderLayout.NORTH);
    pasirinktoSriftoStilius = dabartinis.getStyle();
    
    switch(pasirinktoSriftoStilius){
    case(Font.PLAIN):
        yraParyskintas = false;
    	yraPasviras = false;
    	break;
    case(Font.BOLD):
    	yraParyskintas = true;
    	yraPasviras = false;
    	break;
    case(Font.ITALIC):
    	yraParyskintas = false;
    	yraPasviras = true;
    	break;
    case(Font.BOLD + Font.ITALIC):
    	yraParyskintas = true;
		yraPasviras = true;
		break;
    }
    Panel attrs = new Panel();
    top.add(attrs);
    attrs.setLayout(new GridLayout(0, 1));
    attrs.add(paryskintas = new Checkbox("Bold", yraParyskintas));
    attrs.add(pasviras = new Checkbox("Italic", yraPasviras));

    demo = new JTextArea();
    demo.setAlignmentY(demo.CENTER_ALIGNMENT);
    demo.setText(demoTekstas);
    demo.setEditable(false);
    demo.setSize(300, 100);
    cp.add(demo, BorderLayout.CENTER);

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

    JButton canButton = new JButton("Atsaukti");
    bot.add(canButton);
    canButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	pasirinktasSriftas = dabartinis;
        dispose();
        setVisible(false);
      }
    });

    cp.add(bot, BorderLayout.SOUTH);

    demoPerziura();

    pack();
    setLocationRelativeTo(null);
  }

  protected void demoPerziura() {
    pasirinktoSriftoPav = (String) pavadinimai.getSelectedItem();
    pasirinktoSriftoDydis = Integer.parseInt((String) dydziai.getSelectedItem());
    yraParyskintas = paryskintas.getState();
    yraPasviras = pasviras.getState();
    
    pasirinktoSriftoStilius = Font.PLAIN;
    if (yraParyskintas)
    	pasirinktoSriftoStilius = Font.BOLD;
    else if (yraPasviras)
    	pasirinktoSriftoStilius = Font.ITALIC;
    else if(yraParyskintas && yraPasviras)
    	pasirinktoSriftoStilius = Font.BOLD + Font.ITALIC; 
    pasirinktasSriftas = new Font(pasirinktoSriftoPav, pasirinktoSriftoStilius, pasirinktoSriftoDydis);
    demo.setFont(pasirinktasSriftas);
    
    pack();
  }

  public String gaukPasirinktoSriftoPav() {
    return pasirinktoSriftoPav;
  }

  public int gaukPasirinktoSriftoDydi() {
    return pasirinktoSriftoDydis;
  }
  public int gaukPasirinktoSriftoStiliu(){
	  return pasirinktoSriftoStilius;
  }

  public Font gaukPasirinktaSrifta() {
    return pasirinktasSriftas;
  }

}
