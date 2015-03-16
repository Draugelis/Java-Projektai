package com.menotyou.JC.NIOBiblioteka;

public interface IsimciuStebetojas {

	IsimciuStebetojas NUMATYTASIS = new IsimciuStebetojas(){
		public void ispekApieIsimti(Throwable e){
			e.printStackTrace();
		}
	};
	
	void ispekApieIsimti(Throwable e);
}
