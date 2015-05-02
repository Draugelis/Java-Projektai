package com.menotyou.JC.NIOBiblioteka;

// TODO: Auto-generated Javadoc
/**
 * The Interface IsimciuStebetojas.
 */
public interface IsimciuStebetojas {

	/** The numatytasis. */
	IsimciuStebetojas NUMATYTASIS = new IsimciuStebetojas(){
		public void ispekApieIsimti(Throwable e){
			e.printStackTrace();
		}
	};
	
	/**
	 * Ispek apie isimti.
	 *
	 * @param e the e
	 */
	void ispekApieIsimti(Throwable e);
}
