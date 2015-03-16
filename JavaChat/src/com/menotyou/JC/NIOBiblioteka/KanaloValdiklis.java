package com.menotyou.JC.NIOBiblioteka;

import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public abstract class KanaloValdiklis implements NIOAbstraktiSasaja {

	private final NIOAptarnavimas m_aptarnavimas;
	private final String m_ip;
	private final InetSocketAddress m_adresas;
	private final int m_portas;
	private final SelectableChannel m_kanalas;
	private volatile boolean m_atidarytas;
	private volatile SelectionKey m_raktas;
	private volatile int m_dominanciosOperacijos;
	private boolean m_stebetojasPriskirtas;
	private Object m_zyme;

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

	public void uzdaryk() {
		uzdaryk(null);
	}

	public InetSocketAddress gaukAdresa() {
		return m_adresas;
	}

	public boolean atidarytas() {
		return m_atidarytas;
	}

	public String gaukIp() {
		return m_ip;
	}

	public int gaukPorta() {
		return m_portas;
	}

	public Object gaukZyme() {
		return m_zyme;
	}

	public void nustatykZyme(Object zyme) {
		m_zyme = zyme;
	}

	protected NIOAptarnavimas gaukNIOAptarnavima() {
		return m_aptarnavimas;
	}

	protected void pazymekKadStebetojasPriskirtas() {
		System.out.println("Zymima kad stebetojas priskirtas.");
		synchronized (this) {
			if (m_stebetojasPriskirtas) throw new IllegalStateException("Steb�tojas jau priskirtas");
			m_stebetojasPriskirtas = true;
		}
	}

	void paruostasSkaitymui() {
		throw new UnsupportedOperationException(getClass() + " nepalaiko skaitymo");
	}

	void paruostasRasymui() {
		throw new UnsupportedOperationException(getClass() + " nepalaiko ra�ymo");
	}

	void paruostasPriemimui() {
		throw new UnsupportedOperationException(getClass() + " nepalaiko pri�mimo");
	}

	void paruostasSujungimui() {
		throw new UnsupportedOperationException(getClass() + " nepalaiko sujungimo");
	}

	protected SelectableChannel gaukKanala() {
		return m_kanalas;
	}

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

	protected SelectionKey gaukRakta() {
		return m_raktas;
	}

	abstract void raktasPriskirtas();

	protected void uzdaryk(Exception isimtis) {
		if (atidarytas()) {
			gaukNIOAptarnavima().pridekIEile(new UzdarymoIvykis(this, isimtis));
		}
	}

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
	
	protected void panaikinkSusidomejima(int susidomejimas){
		m_dominanciosOperacijos &= ~susidomejimas;
		sinchronizuokRaktoDominanciasOperacijas();
	}
	
	protected void pridekSusidomejima(int susidomejimas){
		m_dominanciosOperacijos |= susidomejimas;
		sinchronizuokRaktoDominanciasOperacijas();
	}
	
	public String toString(){
		return m_ip + ":" + m_portas;
	}
	
	protected abstract void issijunk(Exception e);
	
	private static class UzdarymoIvykis implements Runnable{
		private final KanaloValdiklis m_valdiklis;
		private final Exception m_isimtis;
		
		private UzdarymoIvykis(KanaloValdiklis valdiklis, Exception e){
			m_valdiklis = valdiklis;
			m_isimtis = e;
		}
		public void run(){
			if(m_valdiklis.atidarytas()){
				m_valdiklis.m_atidarytas = false;
				NIOIrankiai.tyliaiUzdaryRaktaIrKanala(m_valdiklis.gaukRakta(), m_valdiklis.gaukKanala());
				m_valdiklis.issijunk(m_isimtis);
			}
		}
	}
}
