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

public class NIOAptarnavimas {
	public final static int NUMATYTASIS_IO_BUFFERIO_DYDIS = 64 * 1024;
	
	private final Selector m_selektorius;
	private final Queue <Runnable> m_vidineIvykiuEile;
	private ByteBuffer m_bendrasBuferis;
	private IsimciuStebetojas m_isimciuStebetojas;
	
	public NIOAptarnavimas() throws IOException{
		this(NUMATYTASIS_IO_BUFFERIO_DYDIS);
	}
	public NIOAptarnavimas(int ioBuferioDydis) throws IOException{
		m_selektorius = Selector.open();
		m_vidineIvykiuEile = new ConcurrentLinkedQueue<Runnable>();
		m_isimciuStebetojas = IsimciuStebetojas.NUMATYTASIS;
		nustatykBuferioDydi(ioBuferioDydis);
	}
	public synchronized void pasirinkBlokuodamas() throws IOException{
		
		vykdykEile();
		if(m_selektorius.select() > 0){
			apdorokPasirinktusRaktus();
		}
		vykdykEile();
	}
	public synchronized void pasirinkNeblokuodamas() throws IOException{
		vykdykEile();
		if(m_selektorius.selectNow() > 0){
			apdorokPasirinktusRaktus();
		}
		vykdykEile();
	}
	public synchronized void pasirinkBlokuodamas(long pauzesLaikas) throws IOException{
		vykdykEile();
		if(m_selektorius.select(pauzesLaikas) > 0){
			apdorokPasirinktusRaktus();
		}
		vykdykEile();
	}
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
	public void nustatykBuferioDydi(int naujasBuferioDysis){
		 if (naujasBuferioDysis < 256) throw new IllegalArgumentException("Buferis negali buti ma�esnins nei 256 bitai");
		 m_bendrasBuferis = ByteBuffer.allocate(naujasBuferioDysis);
	}
	public int gaukBuferioDydi(){
		return m_bendrasBuferis.capacity();
	}
	public ByteBuffer gaukBendraBuferi(){
		return m_bendrasBuferis;
	}
	public NIOSasaja sukurkSasaja(String kurejas, int portas) throws IOException{
		return sukurkSasaja(InetAddress.getByName(kurejas), portas);
	}
	public NIOSasaja sukurkSasaja(InetAddress inetAdresas, int portas) throws IOException{
		SocketChannel kanalas = SocketChannel.open();
		kanalas.configureBlocking(false);
		InetSocketAddress adresas =new InetSocketAddress(inetAdresas, portas);
		kanalas.connect(adresas);
		return registruokSasajosKanala(kanalas, adresas);
	}
	public NIOServerioSasaja sukurkServerioSasaja(int portas) throws IOException{
		return sukurkServerioSasaja(portas, -1);
	}
	
	public NIOServerioSasaja sukurkServerioSasaja(int portas, int limitas) throws IOException{
		return sukurkServerioSasaja(new InetSocketAddress(portas), limitas);
	}
	public NIOServerioSasaja sukurkServerioSasaja(InetSocketAddress adresas, int limitas) throws IOException{
		ServerSocketChannel kanalas = ServerSocketChannel.open();
		kanalas.socket().setReuseAddress(true);
		kanalas.socket().bind(adresas, limitas);
		kanalas.configureBlocking(false);
		ServerioSasajosKanaloValdiklis kanaloValdiklis = new ServerioSasajosKanaloValdiklis(this, kanalas, adresas);
		pridekIEile(new KanaloRegistravimoIvykis(kanaloValdiklis));
		return kanaloValdiklis;
	}
	NIOSasaja registruokSasajosKanala(SocketChannel kanalas, InetSocketAddress adresas) throws IOException{
		kanalas.configureBlocking(false);
		SasajosKanaloValdiklis kanaloValdiklis = new SasajosKanaloValdiklis(this, kanalas, adresas);
		pridekIEile(new KanaloRegistravimoIvykis(kanaloValdiklis));
		return kanaloValdiklis;
	}
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
	public void isjunk(){
		if(!atidarytas()) return;
		pridekIEile(new IsjungimoIvykis());
	}
	public boolean atidarytas(){
		return m_selektorius.isOpen();
	}
	public void pridekIEile(Runnable ivykis){
		m_vidineIvykiuEile.add(ivykis);
		pabusk();
	}
	public Queue<Runnable> gaukEile(){
		return new LinkedList<Runnable>(m_vidineIvykiuEile);
	}
	public void pabusk(){
		m_selektorius.wakeup();
	}
	public void nustatykIsimciuPriziuretoja(IsimciuStebetojas isimciuStebetojas){
		final IsimciuStebetojas naujasIsimciuStebetojas = isimciuStebetojas == null ? IsimciuStebetojas.NUMATYTASIS : isimciuStebetojas;
		pridekIEile(new Runnable(){
			public void run(){
				m_isimciuStebetojas = naujasIsimciuStebetojas;
			}
		});
	}
	
	public void ispekApieIsimti(Throwable t){
		try{
			m_isimciuStebetojas.ispekApieIsimti(t);
		} catch(Exception e){
			System.err.println("Nepavyko �ra�yti �ios i�imties � i�im�i� steb�toj�:");
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	private class KanaloRegistravimoIvykis implements Runnable{

		private final KanaloValdiklis m_kanaloValdiklis;
		
		private KanaloRegistravimoIvykis(KanaloValdiklis valdiklis){
			m_kanaloValdiklis = valdiklis;
		}
		
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
		
		@Override
		public String toString(){
			return "Registruojamas [" + m_kanaloValdiklis + "]";
		}
	}
	private class IsjungimoIvykis implements Runnable{
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
