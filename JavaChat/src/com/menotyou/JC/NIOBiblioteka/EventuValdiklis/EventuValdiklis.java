package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;
import java.io.IOException;
import java.sql.Date;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import com.menotyou.JC.NIOBiblioteka.IsimciuStebetojas;
import com.menotyou.JC.NIOBiblioteka.NIOAptarnavimas;

public class EventuValdiklis {
		
		private final NIOAptarnavimas m_aptarnavimas;
		private final Queue<UzdelstasVeiksmas> m_eile;
		private Thread m_procesas;
		
		public EventuValdiklis() throws IOException
		{
			m_aptarnavimas = new NIOAptarnavimas();
			m_eile = new PriorityBlockingQueue<UzdelstasVeiksmas>();
			m_procesas = null;
		}
		
		public void asinchroniskasPaleidimas(Runnable r){
			vykdytiVeliau(r, 0);
		}
		
		public UzdelstasVeiksmas vykdytiVeliau(Runnable r, long uzdelsimasMs){
			return eilesVeiksmas(r, uzdelsimasMs + System.currentTimeMillis());
		}
		
		private UzdelstasVeiksmas eilesVeiksmas(Runnable r, long laikas){
			UzdelstasVeiksmas veiksmas = new UzdelstasVeiksmas(r, laikas);
			m_eile.add(veiksmas);
			m_aptarnavimas.pabusk();
			return veiksmas;
		}
		public UzdelstasVeiksmas vykdytiNurodytuLaiku(Runnable r, Date data){
			return eilesVeiksmas(r, data.getTime());
		}
		
		public void nustatykPriziuretoja(IsimciuStebetojas stebetojas){
			gaukNIOAptarnavima().nustatykIsimciuPriziuretoja(stebetojas);
		}
		public long laikasIkiKitoVeiksmo(){
			UzdelstasVeiksmas veiksmas = m_eile.peek();
			return veiksmas == null ? Long.MAX_VALUE :veiksmas.gaukLaika();
		}
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
		public synchronized void sustabdyk(){
			if(m_procesas == null) throw new IllegalStateException("Procesas n�ra paleistas.");
			m_procesas = null;
			m_aptarnavimas.pabusk();
		}
		public synchronized void isjunk(){
			if(m_procesas == null) throw new IllegalStateException("Procesas n�ra paleistas.");
			m_aptarnavimas.isjunk();
			sustabdyk();
		}
		
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
		private void paleiskKitaVeiksma(){
			m_eile.poll().run();
		}
		
		public NIOAptarnavimas gaukNIOAptarnavima(){
			return m_aptarnavimas;
		}
		public Queue<UzdelstasVeiksmas> gaukEile(){
			return new PriorityQueue<UzdelstasVeiksmas>(m_eile);
		}
		public int gaukEilesIlgi(){
			return m_eile.size();
		}
}
