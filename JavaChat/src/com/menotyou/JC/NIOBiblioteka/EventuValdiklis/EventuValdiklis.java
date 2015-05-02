package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;
import java.io.IOException;
import java.sql.Date;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import com.menotyou.JC.NIOBiblioteka.IsimciuStebetojas;
import com.menotyou.JC.NIOBiblioteka.NIOAptarnavimas;

// TODO: Auto-generated Javadoc
/**
 * The Class EventuValdiklis.
 */
public class EventuValdiklis {
		
		/** The m_aptarnavimas. */
		private final NIOAptarnavimas m_aptarnavimas;
		
		/** The m_eile. */
		private final Queue<UzdelstasVeiksmas> m_eile;
		
		/** The m_procesas. */
		private Thread m_procesas;
		
		/**
		 * Instantiates a new eventu valdiklis.
		 *
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public EventuValdiklis() throws IOException
		{
			m_aptarnavimas = new NIOAptarnavimas();
			m_eile = new PriorityBlockingQueue<UzdelstasVeiksmas>();
			m_procesas = null;
		}
		
		/**
		 * Asinchroniskas paleidimas.
		 *
		 * @param r the r
		 */
		public void asinchroniskasPaleidimas(Runnable r){
			vykdytiVeliau(r, 0);
		}
		
		/**
		 * Vykdyti veliau.
		 *
		 * @param r the r
		 * @param uzdelsimasMs the uzdelsimas ms
		 * @return the uzdelstas veiksmas
		 */
		public UzdelstasVeiksmas vykdytiVeliau(Runnable r, long uzdelsimasMs){
			return eilesVeiksmas(r, uzdelsimasMs + System.currentTimeMillis());
		}
		
		/**
		 * Eiles veiksmas.
		 *
		 * @param r the r
		 * @param laikas the laikas
		 * @return the uzdelstas veiksmas
		 */
		private UzdelstasVeiksmas eilesVeiksmas(Runnable r, long laikas){
			UzdelstasVeiksmas veiksmas = new UzdelstasVeiksmas(r, laikas);
			m_eile.add(veiksmas);
			m_aptarnavimas.pabusk();
			return veiksmas;
		}
		
		/**
		 * Vykdyti nurodytu laiku.
		 *
		 * @param r the r
		 * @param data the data
		 * @return the uzdelstas veiksmas
		 */
		public UzdelstasVeiksmas vykdytiNurodytuLaiku(Runnable r, Date data){
			return eilesVeiksmas(r, data.getTime());
		}
		
		/**
		 * Nustatyk priziuretoja.
		 *
		 * @param stebetojas the stebetojas
		 */
		public void nustatykPriziuretoja(IsimciuStebetojas stebetojas){
			gaukNIOAptarnavima().nustatykIsimciuPriziuretoja(stebetojas);
		}
		
		/**
		 * Laikas iki kito veiksmo.
		 *
		 * @return the long
		 */
		public long laikasIkiKitoVeiksmo(){
			UzdelstasVeiksmas veiksmas = m_eile.peek();
			return veiksmas == null ? Long.MAX_VALUE :veiksmas.gaukLaika();
		}
		
		/**
		 * Start.
		 */
		public synchronized void start(){
			if(m_procesas != null) throw new IllegalStateException("Procesas jau paleistas.");
			if(!m_aptarnavimas.atidarytas()) throw new IllegalStateException("Procesas buvo i�jungtas.");
			m_procesas = new Thread(){
				public void run(){
					while(m_procesas == this){
						try{
							pasirink();
						}
						catch(Throwable e){
							if(m_procesas == this) gaukNIOAptarnavima().ispekApieIsimti(e);
						}
					}
				}
				
			};
			m_procesas.start();
		}
		
		/**
		 * Sustabdyk.
		 */
		public synchronized void sustabdyk(){
			if(m_procesas == null) throw new IllegalStateException("Procesas n�ra paleistas.");
			m_procesas = null;
			m_aptarnavimas.pabusk();
		}
		
		/**
		 * Isjunk.
		 */
		public synchronized void isjunk(){
			if(m_procesas == null) throw new IllegalStateException("Procesas n�ra paleistas.");
			m_aptarnavimas.isjunk();
			sustabdyk();
		}
		
		/**
		 * Pasirink.
		 *
		 * @throws Throwable the throwable
		 */
		private void pasirink() throws Throwable{
			while(laikasIkiKitoVeiksmo() <= System.currentTimeMillis()){
				try{
					paleiskKitaVeiksma();
				} catch (Throwable t){
					gaukNIOAptarnavima().ispekApieIsimti(t);
				}
			}
			if(laikasIkiKitoVeiksmo() == Long.MAX_VALUE){
				m_aptarnavimas.pasirinkBlokuodamas();
			} else{
				long uzdelsimas = laikasIkiKitoVeiksmo() - System.currentTimeMillis();
				m_aptarnavimas.pasirinkBlokuodamas(Math.max(1, uzdelsimas));
			}
		}
		
		/**
		 * Paleisk kita veiksma.
		 */
		private void paleiskKitaVeiksma(){
			m_eile.poll().run();
		}
		
		/**
		 * Gauk nio aptarnavima.
		 *
		 * @return the NIO aptarnavimas
		 */
		public NIOAptarnavimas gaukNIOAptarnavima(){
			return m_aptarnavimas;
		}
		
		/**
		 * Gauk eile.
		 *
		 * @return the queue
		 */
		public Queue<UzdelstasVeiksmas> gaukEile(){
			return new PriorityQueue<UzdelstasVeiksmas>(m_eile);
		}
		
		/**
		 * Gauk eiles ilgi.
		 *
		 * @return the int
		 */
		public int gaukEilesIlgi(){
			return m_eile.size();
		}
}
