package com.menotyou.JC.NIOBiblioteka;

/**
 * Valdiklis(interface) kuris atsakingas už išimčių registravimą.
 * Šiuo metu jis visas iššimtis išveda į ekraną, tokiu būdu galima gan paprastai
 * visas išimtis nukreipti į failą.
 */
public interface IsimciuStebetojas {

    IsimciuStebetojas NUMATYTASIS = new IsimciuStebetojas() {
        public void ispekApieIsimti(Throwable e) {
            e.printStackTrace();
        }
    };

    void ispekApieIsimti(Throwable e);
}
