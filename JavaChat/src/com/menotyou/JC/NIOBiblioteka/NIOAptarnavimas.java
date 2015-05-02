package com.menotyou.JC.NIOBiblioteka;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// TODO: Auto-generated Javadoc
/**
 * The Class NIOAptarnavimas.
 */
public class NIOAptarnavimas {
	
	/** The Constant NUMATYTASIS_IO_BUFFERIO_DYDIS. */
	public final static int NUMATYTASIS_IO_BUFFERIO_DYDIS = 64 * 1024;
	
	/** The m_selektorius. */
	private final Selector m_selektorius;
	
	/** The m_vidine ivykiu eile. */
	private final Queue <Runnable> m_vidineIvykiuEile;
	
	/** The m_bendras buferis. */
	private ByteBuffer m_bendrasBuferis;
	
	/** The m_isimciu stebetojas. */
	private IsimciuStebetojas m_isimciuStebetojas;
	
	/**
	 * Instantiates a new NIO aptarnavimas.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NIOAptarnavimas() throws IOException{
		this(NUMATYTASIS_IO_BUFFERIO_DYDIS);
	}
	
	/**
	 * Instantiates a new NIO aptarnavimas.
	 *
	 * @param ioBuferioDydis the io buferio dydis
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NIOAptarnavimas(int ioBuferioDydis) throws IOException{
		m_selektorius = Selector.open();
		m_vidineIvykiuEile = new ConcurrentLinkedQueue<Runnable>();
		m_isimciuStebetojas = IsimciuStebetojas.NUMATYTASIS;
		nustatykBuferioDydi(ioBuferioDydis);
	}
	
	/**
	 * Pasirink blokuodamas.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized void pasirinkBlokuodamas() throws IOException{
		
		vykdykEile();
		if(m_selektorius.select() > 0){
			apdorokPasirinktusRaktus();
		}
		vykdykEile();
	}
	
	/**
	 * Pasirink neblokuodamas.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized void pasirinkNeblokuodamas() throws IOException{
		vykdykEile();
		if(m_selektorius.selectNow() > 0){
			apdorokPasirinktusRaktus();
		}
		vykdykEile();
	}
	
	/**
	 * Pasirink blokuodamas.
	 *
	 * @param pauzesLaikas the pauzes laikas
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized void pasirinkBlokuodamas(long pauzesLaikas) throws IOException{
		vykdykEile();
		if(m_selektorius.select(pauzesLaikas) > 0){
			apdorokPasirinktusRaktus();
		}
		vykdykEile();
	}
	
	/**
	 * Vykdyk eile.
	 */
	private void vykdykEile(){
        Runnable ivykis;
        while ((ivykis = m_vidineIvykiuEile.poll()) != null){
	        try{
	        	ivykis.run();
	        }catch (Throwable t){
	            ispekApieIsimti(t);
	        }
        }
    }
	
	/**
	 * Nustatyk buferio dydi.
	 *
	 * @param naujasBuferioDysis the naujas buferio dysis
	 */
	public void nustatykBuferioDydi(int naujasBuferioDysis){
		 if (naujasBuferioDysis < 256) throw new IllegalArgumentException("Buferis negali buti ma�esnins nei 256 bitai");
		 m_bendrasBuferis = ByteBuffer.allocate(naujasBuferioDysis);
	}
	
	/**
	 * Gauk buferio dydi.
	 *
	 * @return the int
	 */
	public int gaukBuferioDydi(){
		return m_bendrasBuferis.capacity();
	}
	
	/**
	 * Gauk bendra buferi.
	 *
	 * @return the byte buffer
	 */
	public ByteBuffer gaukBendraBuferi(){
		return m_bendrasBuferis;
	}
	
	/**
	 * Sukurk sasaja.
	 *
	 * @param kurejas the kurejas
	 * @param portas the portas
	 * @return the NIO sasaja
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NIOSasaja sukurkSasaja(String kurejas, int portas) throws IOException{
		return sukurkSasaja(InetAddress.getByName(kurejas), portas);
	}
	
	/**
	 * Sukurk sasaja.
	 *
	 * @param inetAdresas the inet adresas
	 * @param portas the portas
	 * @return the NIO sasaja
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NIOSasaja sukurkSasaja(InetAddress inetAdresas, int portas) throws IOException{
		SocketChannel kanalas = SocketChannel.open();
		kanalas.configureBlocking(false);
		InetSocketAddress adresas =new InetSocketAddress(inetAdresas, portas);
		kanalas.connect(adresas);
		return registruokSasajosKanala(kanalas, adresas);
	}
	
	/**
	 * Sukurk serverio sasaja.
	 *
	 * @param portas the portas
	 * @return the NIO serverio sasaja
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NIOServerioSasaja sukurkServerioSasaja(int portas) throws IOException{
		return sukurkServerioSasaja(portas, -1);
	}
	
	/**
	 * Sukurk serverio sasaja.
	 *
	 * @param portas the portas
	 * @param limitas the limitas
	 * @return the NIO serverio sasaja
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NIOServerioSasaja sukurkServerioSasaja(int portas, int limitas) throws IOException{
		return sukurkServerioSasaja(new InetSocketAddress(portas), limitas);
	}
	
	/**
	 * Sukurk serverio sasaja.
	 *
	 * @param adresas the adresas
	 * @param limitas the limitas
	 * @return the NIO serverio sasaja
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NIOServerioSasaja sukurkServerioSasaja(InetSocketAddress adresas, int limitas) throws IOException{
		ServerSocketChannel kanalas = ServerSocketChannel.open();
		kanalas.socket().setReuseAddress(true);
		kanalas.socket().bind(adresas, limitas);
		kanalas.configureBlocking(false);
		ServerioSasajosKanaloValdiklis kanaloValdiklis = new ServerioSasajosKanaloValdiklis(this, kanalas, adresas);
		pridekIEile(new KanaloRegistravimoIvykis(kanaloValdiklis));
		return kanaloValdiklis;
	}
	
	/**
	 * Registruok sasajos kanala.
	 *
	 * @param kanalas the kanalas
	 * @param adresas the adresas
	 * @return the NIO sasaja
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	NIOSasaja registruokSasajosKanala(SocketChannel kanalas, InetSocketAddress adresas) throws IOException{
		kanalas.configureBlocking(false);
		SasajosKanaloValdiklis kanaloValdiklis = new SasajosKanaloValdiklis(this, kanalas, adresas);
		pridekIEile(new KanaloRegistravimoIvykis(kanaloValdiklis));
		return kanaloValdiklis;
	}
	
	/**
	 * Apdorok pasirinktus raktus.
	 */
	private void apdorokPasirinktusRaktus(){
		Iterator<SelectionKey> it = m_selektorius.selectedKeys().iterator();
		while(it.hasNext()){
			SelectionKey raktas = it.next();
			it.remove();
			try{
				apdorokRakta(raktas);
			} catch (Throwable t){
				ispekApieIsimti(t);
			}
		}
	}
	
	/**
	 * Apdorok rakta.
	 *
	 * @param raktas the raktas
	 */
	private void apdorokRakta(SelectionKey raktas){
		KanaloValdiklis valdiklis = (KanaloValdiklis) raktas.attachment();
		try{
			if(raktas.isReadable()){
				valdiklis.paruostasSkaitymui();
			}
			if(raktas.isWritable()){
				valdiklis.paruostasRasymui();
			}
			if(raktas.isAcceptable()){
				valdiklis.paruostasPriemimui();
			}
			if(raktas.isConnectable()){
				valdiklis.paruostasSujungimui();
			}
		} catch (CancelledKeyException e){
			System.out.println("Raktas atšauktas");
			valdiklis.uzdaryk(e);
		}
	}
	
	/**
	 * Isjunk.
	 */
	public void isjunk(){
		if(!atidarytas()) return;
		pridekIEile(new IsjungimoIvykis());
	}
	
	/**
	 * Atidarytas.
	 *
	 * @return true, if successful
	 */
	public boolean atidarytas(){
		return m_selektorius.isOpen();
	}
	
	/**
	 * Pridek i eile.
	 *
	 * @param ivykis the ivykis
	 */
	public void pridekIEile(Runnable ivykis){
		m_vidineIvykiuEile.add(ivykis);
		pabusk();
	}
	
	/**
	 * Gauk eile.
	 *
	 * @return the queue
	 */
	public Queue<Runnable> gaukEile(){
		return new LinkedList<Runnable>(m_vidineIvykiuEile);
	}
	
	/**
	 * Pabusk.
	 */
	public void pabusk(){
		m_selektorius.wakeup();
	}
	
	/**
	 * Nustatyk isimciu priziuretoja.
	 *
	 * @param isimciuStebetojas the isimciu stebetojas
	 */
	public void nustatykIsimciuPriziuretoja(IsimciuStebetojas isimciuStebetojas){
		final IsimciuStebetojas naujasIsimciuStebetojas = isimciuStebetojas == null ? IsimciuStebetojas.NUMATYTASIS : isimciuStebetojas;
		pridekIEile(new Runnable(){
			public void run(){
				m_isimciuStebetojas = naujasIsimciuStebetojas;
			}
		});
	}
	
	/**
	 * Ispek apie isimti.
	 *
	 * @param t the t
	 */
	public void ispekApieIsimti(Throwable t){
		try{
			m_isimciuStebetojas.ispekApieIsimti(t);
		} catch(Exception e){
			System.err.println("Nepavyko �ra�yti �ios i�imties � i�im�i� steb�toj�:");
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * The Class KanaloRegistravimoIvykis.
	 */
	private class KanaloRegistravimoIvykis implements Runnable{

		/** The m_kanalo valdiklis. */
		private final KanaloValdiklis m_kanaloValdiklis;
		
		/**
		 * Instantiates a new kanalo registravimo ivykis.
		 *
		 * @param valdiklis the valdiklis
		 */
		private KanaloRegistravimoIvykis(KanaloValdiklis valdiklis){
			m_kanaloValdiklis = valdiklis;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try{
				System.out.println("Registruojamas naujas kanalas adresu: " + m_kanaloValdiklis.gaukAdresa());
				SelectionKey raktas = m_kanaloValdiklis.gaukKanala().register(m_selektorius, m_kanaloValdiklis.gaukKanala().validOps());
				m_kanaloValdiklis.nustatykRakta(raktas);
				raktas.attach(m_kanaloValdiklis);
			} catch (Exception e){
				System.out.println("Nepavyko užregistruoti " + m_kanaloValdiklis.gaukAdresa());
				m_kanaloValdiklis.uzdaryk(e);
			}	
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString(){
			return "Registruojamas [" + m_kanaloValdiklis + "]";
		}
	}
	
	/**
	 * The Class IsjungimoIvykis.
	 */
	private class IsjungimoIvykis implements Runnable{
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run(){
			System.out.println("Isjungiama... Geros dienos");
			if(!atidarytas()) return;
			for(SelectionKey raktas : m_selektorius.keys()){
				try{
					NIOIrankiai.tyliaiAtsaukRakta(raktas);
					((KanaloValdiklis) raktas.attachment()).uzdaryk();
				} catch (Exception e){
					
				}
			}
			try{
				m_selektorius.close();
			} catch (IOException e){
				
			}
		}
	}
}
