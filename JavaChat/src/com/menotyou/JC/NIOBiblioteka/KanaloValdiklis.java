package com.menotyou.JC.NIOBiblioteka;

import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

// TODO: Auto-generated Javadoc
/**
 * The Class KanaloValdiklis.
 */
public abstract class KanaloValdiklis implements NIOAbstraktiSasaja {

	/** The m_aptarnavimas. */
	private final NIOAptarnavimas m_aptarnavimas;
	
	/** The m_ip. */
	private final String m_ip;
	
	/** The m_adresas. */
	private final InetSocketAddress m_adresas;
	
	/** The m_portas. */
	private final int m_portas;
	
	/** The m_kanalas. */
	private final SelectableChannel m_kanalas;
	
	/** The m_atidarytas. */
	private volatile boolean m_atidarytas;
	
	/** The m_raktas. */
	private volatile SelectionKey m_raktas;
	
	/** The m_dominancios operacijos. */
	private volatile int m_dominanciosOperacijos;
	
	/** The m_stebetojas priskirtas. */
	private boolean m_stebetojasPriskirtas;
	
	/** The m_zyme. */
	private Object m_zyme;

	/**
	 * Instantiates a new kanalo valdiklis.
	 *
	 * @param aptarnavimas the aptarnavimas
	 * @param kanalas the kanalas
	 * @param adresas the adresas
	 */
	protected KanaloValdiklis(NIOAptarnavimas aptarnavimas, SelectableChannel kanalas, InetSocketAddress adresas) {
		m_kanalas = kanalas;
		m_aptarnavimas = aptarnavimas;
		m_atidarytas = true;
		m_raktas = null;
		m_dominanciosOperacijos = 0;
		m_adresas = adresas;
		m_ip = adresas.getAddress().getHostAddress();
		m_portas = adresas.getPort();
		m_zyme = null;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOAbstraktiSasaja#uzdaryk()
	 */
	public void uzdaryk() {
		uzdaryk(null);
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOAbstraktiSasaja#gaukAdresa()
	 */
	public InetSocketAddress gaukAdresa() {
		return m_adresas;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOAbstraktiSasaja#atidarytas()
	 */
	public boolean atidarytas() {
		return m_atidarytas;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOAbstraktiSasaja#gaukIp()
	 */
	public String gaukIp() {
		return m_ip;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOAbstraktiSasaja#gaukPorta()
	 */
	public int gaukPorta() {
		return m_portas;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOAbstraktiSasaja#gaukZyme()
	 */
	public Object gaukZyme() {
		return m_zyme;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOAbstraktiSasaja#nustatykZyme(java.lang.Object)
	 */
	public void nustatykZyme(Object zyme) {
		m_zyme = zyme;
	}

	/**
	 * Gauk nio aptarnavima.
	 *
	 * @return the NIO aptarnavimas
	 */
	protected NIOAptarnavimas gaukNIOAptarnavima() {
		return m_aptarnavimas;
	}

	/**
	 * Pazymek kad stebetojas priskirtas.
	 */
	protected void pazymekKadStebetojasPriskirtas() {
		System.out.println("Zymima kad stebetojas priskirtas.");
		synchronized (this) {
			if (m_stebetojasPriskirtas) throw new IllegalStateException("Steb�tojas jau priskirtas");
			m_stebetojasPriskirtas = true;
		}
	}

	/**
	 * Paruostas skaitymui.
	 */
	void paruostasSkaitymui() {
		throw new UnsupportedOperationException(getClass() + " nepalaiko skaitymo");
	}

	/**
	 * Paruostas rasymui.
	 */
	void paruostasRasymui() {
		throw new UnsupportedOperationException(getClass() + " nepalaiko ra�ymo");
	}

	/**
	 * Paruostas priemimui.
	 */
	void paruostasPriemimui() {
		throw new UnsupportedOperationException(getClass() + " nepalaiko pri�mimo");
	}

	/**
	 * Paruostas sujungimui.
	 */
	void paruostasSujungimui() {
		throw new UnsupportedOperationException(getClass() + " nepalaiko sujungimo");
	}

	/**
	 * Gauk kanala.
	 *
	 * @return the selectable channel
	 */
	protected SelectableChannel gaukKanala() {
		return m_kanalas;
	}

	/**
	 * Nustatyk rakta.
	 *
	 * @param raktas the raktas
	 */
	void nustatykRakta(SelectionKey raktas) {
		if (m_raktas != null) throw new IllegalStateException("Bandyta priskirti rakta dukart");
		m_raktas = raktas;
		if (!atidarytas()) {
			NIOIrankiai.tyliaiAtsaukRakta(m_raktas);
			return;
		}
		raktasPriskirtas();
		sinchronizuokRaktoDominanciasOperacijas();
	}

	/**
	 * Gauk rakta.
	 *
	 * @return the selection key
	 */
	protected SelectionKey gaukRakta() {
		return m_raktas;
	}

	/**
	 * Raktas priskirtas.
	 */
	abstract void raktasPriskirtas();

	/**
	 * Uzdaryk.
	 *
	 * @param isimtis the isimtis
	 */
	protected void uzdaryk(Exception isimtis) {
		if (atidarytas()) {
			gaukNIOAptarnavima().pridekIEile(new UzdarymoIvykis(this, isimtis));
		}
	}

	/**
	 * Sinchronizuok rakto dominancias operacijas.
	 */
	private void sinchronizuokRaktoDominanciasOperacijas(){
		if(m_raktas != null){
			try{
				int senosOperacijos = m_raktas.interestOps();
				if((m_dominanciosOperacijos & SelectionKey.OP_CONNECT) != 0){
					m_raktas.interestOps(SelectionKey.OP_CONNECT);
				}else{
					m_raktas.interestOps(m_dominanciosOperacijos);
				}
				if(m_raktas.interestOps() != senosOperacijos){
					m_aptarnavimas.pabusk();
				}
			} catch(CancelledKeyException e){
				
			}
		}
	}
	
	/**
	 * Panaikink susidomejima.
	 *
	 * @param susidomejimas the susidomejimas
	 */
	protected void panaikinkSusidomejima(int susidomejimas){
		m_dominanciosOperacijos &= ~susidomejimas;
		sinchronizuokRaktoDominanciasOperacijas();
	}
	
	/**
	 * Pridek susidomejima.
	 *
	 * @param susidomejimas the susidomejimas
	 */
	protected void pridekSusidomejima(int susidomejimas){
		m_dominanciosOperacijos |= susidomejimas;
		sinchronizuokRaktoDominanciasOperacijas();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return m_ip + ":" + m_portas;
	}
	
	/**
	 * Issijunk.
	 *
	 * @param e the e
	 */
	protected abstract void issijunk(Exception e);
	
	/**
	 * The Class UzdarymoIvykis.
	 */
	private static class UzdarymoIvykis implements Runnable{
		
		/** The m_valdiklis. */
		private final KanaloValdiklis m_valdiklis;
		
		/** The m_isimtis. */
		private final Exception m_isimtis;
		
		/**
		 * Instantiates a new uzdarymo ivykis.
		 *
		 * @param valdiklis the valdiklis
		 * @param e the e
		 */
		private UzdarymoIvykis(KanaloValdiklis valdiklis, Exception e){
			m_valdiklis = valdiklis;
			m_isimtis = e;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run(){
			if(m_valdiklis.atidarytas()){
				m_valdiklis.m_atidarytas = false;
				NIOIrankiai.tyliaiUzdaryRaktaIrKanala(m_valdiklis.gaukRakta(), m_valdiklis.gaukKanala());
				m_valdiklis.issijunk(m_isimtis);
			}
		}
	}
}
