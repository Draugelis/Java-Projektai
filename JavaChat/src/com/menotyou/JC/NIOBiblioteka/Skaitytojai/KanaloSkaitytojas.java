package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.menotyou.JC.NIOBiblioteka.NIOAptarnavimas;
import com.menotyou.JC.NIOBiblioteka.NIOIrankiai;

// TODO: Auto-generated Javadoc
/**
 * The Class KanaloSkaitytojas.
 */
public class KanaloSkaitytojas {
	
	/** The m_aptarnavimas. */
	private final NIOAptarnavimas m_aptarnavimas;
	
	/** The m_ankstesni bitai. */
	private ByteBuffer m_ankstesniBitai;
	
	/** The m_nuskaityti bitai. */
	private long m_nuskaitytiBitai;
	
	/**
	 * Instantiates a new kanalo skaitytojas.
	 *
	 * @param aptarnavimas the aptarnavimas
	 */
	public KanaloSkaitytojas(NIOAptarnavimas aptarnavimas){
		m_aptarnavimas = aptarnavimas;
		m_nuskaitytiBitai = 0;
	}
	
	/**
	 * Skaityk.
	 *
	 * @param kanalas the kanalas
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int skaityk(SocketChannel kanalas) throws IOException{
		ByteBuffer buferis = gaukBuferi();
		
		buferis.clear();
		if(m_ankstesniBitai != null){
			buferis.position(m_ankstesniBitai.remaining());
		}
		int nuskaityta = kanalas.read(buferis);
		if(nuskaityta < 0) throw new EOFException("Bufferis nuskait� -1");
		if(!buferis.hasRemaining()) throw new BufferOverflowException();
		
		m_nuskaitytiBitai += nuskaityta;
		if(nuskaityta == 0) return 0;
		if(m_ankstesniBitai != null){
			int pozicija = buferis.position();
			buferis.position(0);
			buferis.put(m_ankstesniBitai);
			buferis.position(pozicija);
			m_ankstesniBitai = null;
		}
		buferis.flip();
		
		return nuskaityta;
	}
	
	/**
	 * Supakuok.
	 */
	public void supakuok(){
		ByteBuffer buferis = gaukBuferi();
		if(buferis.remaining() > 0){
			m_ankstesniBitai = NIOIrankiai.kopijuok(buferis);
		}
	}
	
	/**
	 * Gauk nuskaitytus bitus.
	 *
	 * @return the long
	 */
	public long gaukNuskaitytusBitus(){
		return m_nuskaitytiBitai;
	}
	
	/**
	 * Gauk buferi.
	 *
	 * @return the byte buffer
	 */
	public ByteBuffer gaukBuferi(){
		return m_aptarnavimas.gaukBendraBuferi();
	}
	
}
